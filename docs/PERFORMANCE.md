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

Neither task is part of `build`, `check`, or normal pull-request CI. Loom may
still compile the JMH source set while preparing the remapped artifact, which
catches benchmark compilation errors. The benchmark classes and dependencies
are not packaged or published. Benchmark results vary with the JVM, CPU
governor, temperature, and competing processes.

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
and simplification workloads from 8 through 256 boxes. The cubic legacy scan
is limited to 8 through 64 boxes; the 256-box benchmarks compare the selected
implementation with a stable object-queue implementation instead.

The standalone creation baseline calls `VoxelShapes.cuboid` with coordinates
divided by 16. This is the implementation of `Block.createCuboidShape` in
Minecraft 1.19.4, but avoids initializing unrelated block registries in the
standalone JMH process.

Cold primitive-factory and rotation benchmarks use `Level.Invocation` setup to
clear memoized entries or rebuild source shapes. This defeats identity caching,
but the additional JMH setup and timestamping can be significant beside a very
small benchmark operation and may dilute cold-path ratios, including the 1.10x
rotation gate. These results should be interpreted as guarded comparisons, not
precise standalone operation costs.

## Acceptance thresholds

`verifyPerformance` checks representative results:

- primitive cuboids are no more than 10% slower than vanilla-equivalent code
- fresh 1- and 8-box binary unions are no more than 10% slower than vanilla
- a fresh 32-shape multi-union is no more than 10% slower than vanilla
- a fresh complex rotation is no more than 10% slower than the uncached legacy path
- repeated 8-box binary unions are at least 2x faster than recomputation
- repeated 32-box right rotations are at least 2x faster than the uncached legacy implementation
- repeated table and chair factories are at least 2x faster than reconstruction
- uncached unions and transformations do not allocate more than their baselines
- specialized warm cache paths allocate at most half as much as legacy list-based keys
- cached transformations allocate at most 8 B/op
- 32- and 64-box simplification does not regress against the legacy scan
- 256-box simplification is no more than 5% slower than the object queue and
  uses at most 70% of its allocation, with an absolute ceiling of 2 MB/op

Allocation gates use the GC profiler's normalized bytes-per-operation metric.
All allocation rates remain available in the JSON report and should be reviewed
alongside latency because they can be sensitive to JIT escape analysis.

For the fractional-cuboid and legacy-simplifier parity checks, a mean outside
the limit is reported as inconclusive when the two JMH 99.9% confidence
intervals overlap. This prevents a noisy run from failing a parity gate without
statistical separation. The stricter cache, cold-operation, and allocation
gates remain direct mean comparisons.

## Design outcomes

- Binary unions use two-touch admission. A new pair runs vanilla directly;
  reuse within a small same-thread recent-pair ring admits it to the bounded
  cache, including short interleaved sequences, while Caffeine still enforces
  expiry and records hits.
- Transformations check the specialized identity key before entering Caffeine's
  mapping-function path, making repeated same-thread hits effectively
  allocation-free.
- `clearCache()` immediately drops the calling thread's last-hit state and uses
  generation invalidation to retire state on other threads when they next use it.
- Multi-unions avoid filtering and `subList` allocations and use balanced,
  index-range combination for larger collections.
- `CommonShapes` uses lazy, thread-safe indexed caches. Chairs without
  backrests are canonicalized by seat height because their validated
  `backrestHeight` does not affect geometry. This reduces the maximum number of
  finite factory slots from 501 to 321.
- Simplification uses the lower-overhead scan below 96 boxes, a compact
  deterministic queue from 96 through 256 boxes, and the lower-memory scan
  above 256. The selected queue pre-sizes its storage and packs each candidate's
  two positions into one integer while preserving the previous ordering.
- An array-based primitive heap reduced allocation further in experiments, but
  was 17–22% slower than the object queue and was therefore not retained.

## Representative results

The latest three-fork acceptance run was collected on Linux with an AMD Ryzen
7 7735HS and Amazon Corretto 17.0.20. Values are average time:

| Workload | Baseline | VoxLib | Observation |
|---|---:|---:|---|
| Integral cuboid creation | 47.85 ns | 49.07 ns | 1.025x vanilla-equivalent code |
| Fresh 1-box binary union | 272.77 ns | 299.42 ns | 1.098x vanilla; inside the cold gate |
| Fresh 8-box binary union | 4,408.95 ns | 4,312.30 ns | slightly faster than vanilla in this run |
| Fresh 32-shape multi-union | 589.32 µs | 115.55 µs | balanced union is 5.10x faster |
| Repeated 8-box binary union | 4,093.61 ns | 56.85 ns | cached call is 72x faster |
| Repeated 32-box right rotation | 98.18 µs | 54.28 ns | cached call is about 1,809x faster |
| Repeated default table factory | 56.54 µs | 2.11 ns | indexed memoization hit |
| Repeated default chair factory | 40.26 µs | 2.15 ns | indexed memoization hit |
| Simplify 32 boxes to 8 | 67.14 µs | 67.45 µs | confidence intervals overlap |
| Simplify 64 boxes to 8 | 500.73 µs | 495.23 µs | selected scan is slightly faster |
| Simplify 256 boxes to 8 | 23.55 ms | 22.22 ms | 5.6% faster than the object queue |

The isolated 256-to-8 compact queue measured 22.22 ms and 1,879,307 B/op,
compared with 23.55 ms and 2,950,590 B/op for the compact object-queue
baseline. The previous Kotlin object queue measured about 10.93 MB/op on the
same workload, so the selected implementation reduces transient allocation by
about 82.8% while preserving merge order and output geometry.

On cache hits, specialized union keys remain effectively allocation-free.
Specialized transformation lookup measured 0.0018 B/op, also effectively zero,
versus 77.33 B/op for the legacy list-based cached path.

The fractional-cuboid result in this full run was inconclusive: VoxLib measured
37.76 ns against 32.07 ns for the vanilla-equivalent baseline, but their 99.9%
confidence intervals overlapped. An isolated three-fork rerun measured 31.88 ns
for VoxLib and 37.99 ns for the baseline. This small creation benchmark is
especially sensitive to JIT escape analysis, so neither run supports a general
speed claim.

## Allocation versus retained memory

JMH's `B/op` metric measures bytes allocated while performing one operation.
It is useful for estimating garbage-collector pressure, but it does not measure
how much memory remains reachable after the operation.

- Simplifier queue storage is temporary. Its approximately 1.88 MB/op for the
  256-to-8 workload becomes collectible after simplification returns.
- The shared operation cache still retains at most 500 entries and expires them
  ten minutes after access. Entries use strong keys and values, so retained
  memory depends on the size and structure of the source and result shapes.
- Same-thread fast paths retain up to four recent binary operand pairs and the
  most recent transformation source. `clearCache()` removes the calling
  thread's references immediately; other threads discard stale state on their
  next operation.
- `CommonShapes` has at most 321 lazily populated slots. These finite,
  reusable factory results are retained for the life of the class loader.

These bounds describe library bookkeeping, not total Minecraft memory usage.
Use a heap profiler with a representative modpack and workload when retained
heap size matters.

Always rerun `performanceCheck` on the target environment and profile real
Minecraft call patterns before drawing gameplay-level conclusions.
