# Performance benchmarks

VoxLib is a convenience library first. Its core geometry still uses Minecraft's
`VoxelShape` implementation. The benchmarks in this project identify narrow
cases where VoxLib can avoid repeated work; they do not establish that VoxLib
is generally faster than vanilla.

## Running the suite

Use Java 17 on an otherwise idle machine:

```shell
./gradlew jmh
```

JMH writes human-readable output and JSON data to:

- `build/reports/jmh/human.txt`
- `build/reports/jmh/results.json`

To run the unit tests, benchmark suite, and acceptance thresholds together:

```shell
./gradlew performanceCheck
```

Neither task is part of `build`, `check`, or normal pull-request CI. Benchmark
results vary with the JVM, CPU governor, temperature, and competing processes.

## Method

The committed configuration uses JMH 1.37 on a Java 17 toolchain with:

- average-time mode in nanoseconds
- three 500 ms warmup iterations
- five 500 ms measurement iterations
- three independent forks
- the JMH GC profiler for allocation measurements

The suite covers primitive cuboids, overlapping and disjoint binary unions,
cold and warm multi-unions, direct left-fold and balanced union strategies,
all rotations and flips, every common-shape factory family, primitive-factory
first calls, cache contention, legacy versus specialized cache-key allocation,
and simplification workloads from 8 through 256 boxes.

The standalone creation baseline calls `VoxelShapes.cuboid` with coordinates
divided by 16. This is the implementation of `Block.createCuboidShape` in
Minecraft 1.19.4, but avoids initializing unrelated block registries in the
standalone JMH process.

## Acceptance thresholds

`verifyPerformance` checks representative results:

- primitive cuboids are no more than 10% slower than vanilla-equivalent code
- a fresh 8-box binary union is no more than 10% slower than vanilla
- a fresh 32-shape multi-union is no more than 10% slower than vanilla
- repeated 8-box binary unions are at least 2x faster than recomputation
- repeated 32-box right rotations are at least 2x faster than the uncached legacy implementation
- repeated table and chair factories are at least 2x faster than reconstruction
- uncached unions and transformations do not allocate more than their baselines
- specialized warm cache paths allocate at most half as much as legacy list-based keys
- 32-, 64-, and 256-box simplification does not regress against the legacy algorithm

Allocation gates use the GC profiler's normalized bytes-per-operation metric.
All allocation rates remain available in the JSON report and should be reviewed
alongside latency because they can be sensitive to JIT escape analysis.

## Design outcomes

- Binary unions use two-touch admission. A new pair runs vanilla directly;
  reuse admits it to the bounded cache, and a same-thread last-hit key avoids
  repeated key allocation while Caffeine still enforces expiry and records hits.
- Transformations use specialized identity keys without allocating a source list.
- Multi-unions avoid filtering and `subList` allocations and use balanced,
  index-range combination for larger collections.
- `CommonShapes` stores each requested finite parameter combination in a
  thread-safe indexed cache.
- Simplification uses the lower-overhead scan below 96 boxes, a deterministic
  priority queue from 96 through 256 boxes, and the lower-memory scan above 256.

## Representative results

The latest three-fork acceptance run was collected on Linux with an AMD Ryzen
7 7735HS and Amazon Corretto 17.0.20. Values are average time:

| Workload | Baseline | VoxLib | Observation |
|---|---:|---:|---|
| Fresh 8-box binary union | 4,254 ns | 4,340 ns | 1.020x vanilla; inside the cold gate |
| Fresh 32-shape multi-union | 594,799 ns | 119,117 ns | balanced union is 5.0x faster |
| Repeated 8-box binary union | 4,160 ns | 57.0 ns | cached call is 73x faster |
| Repeated 32-box right rotation | 96,842 ns | 54.1 ns | cached call is about 1,790x faster |
| Repeated default table factory | 58,356 ns | 2.12 ns | indexed memoization hit |
| Repeated default chair factory | 39,630 ns | 2.14 ns | indexed memoization hit |
| Simplify 32 boxes to 8 | 67.9 µs | 66.4 µs | selected scan does not regress |
| Simplify 64 boxes to 8 | 500 µs | 495 µs | selected scan does not regress |
| Simplify 256 boxes to 8 | 36.3 ms | 23.9 ms | deterministic queue is 1.52x faster |

The isolated 32-box uncached rotation core measured 97.6 µs and 243,329 B/op
for the legacy implementation versus 96.6 µs and 243,081 B/op for the new
implementation. On cache hits, specialized union keys reduced normalized
allocation from 64 B/op to effectively zero; transformation keys reduced it
from 64 B/op to 24 B/op.

Always rerun `performanceCheck` on the target environment and profile real
Minecraft call patterns before drawing gameplay-level conclusions.
