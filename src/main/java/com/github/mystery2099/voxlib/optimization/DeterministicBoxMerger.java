package com.github.mystery2099.voxlib.optimization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import net.minecraft.util.math.Box;

final class DeterministicBoxMerger {
    private static final int POSITION_BITS = 16;
    private static final int POSITION_MASK = (1 << POSITION_BITS) - 1;
    private static final int MAX_ORDERED_POSITION_CAPACITY =
        1 << (Integer.SIZE - 1 - POSITION_BITS);
    private static final Comparator<MergeCandidate> CANDIDATE_ORDER = Comparator
        .comparingDouble((MergeCandidate candidate) -> candidate.distance)
        .thenComparingInt(candidate -> candidate.positionPair);

    private DeterministicBoxMerger() {
    }

    static List<Box> mergeClosest(List<Box> boxes, int maxBoxes) {
        int boxCount = boxes.size();
        int positionCapacity = boxCount * 2 - maxBoxes;
        if (positionCapacity > MAX_ORDERED_POSITION_CAPACITY) {
            throw new IllegalArgumentException("Box positions exceed packed candidate capacity");
        }
        Box[] boxesByPosition = new Box[positionCapacity];
        boolean[] activePositions = new boolean[positionCapacity];
        for (int position = 0; position < boxCount; position++) {
            boxesByPosition[position] = boxes.get(position);
            activePositions[position] = true;
        }

        PriorityQueue<MergeCandidate> candidates = new PriorityQueue<>(
            maxCandidateCount(boxCount, maxBoxes),
            CANDIDATE_ORDER
        );
        for (int firstPosition = 0; firstPosition < boxCount - 1; firstPosition++) {
            for (
                int secondPosition = firstPosition + 1;
                secondPosition < boxCount;
                secondPosition++
            ) {
                candidates.add(candidate(boxesByPosition, firstPosition, secondPosition));
            }
        }

        int activeCount = boxCount;
        int nextPosition = boxCount;
        while (activeCount > maxBoxes) {
            MergeCandidate closest = pollActiveCandidate(candidates, activePositions);
            int firstPosition = closest.firstPosition();
            int secondPosition = closest.secondPosition();
            activePositions[firstPosition] = false;
            activePositions[secondPosition] = false;

            int mergedPosition = nextPosition++;
            boxesByPosition[mergedPosition] = encompass(
                boxesByPosition[firstPosition],
                boxesByPosition[secondPosition]
            );
            for (int position = 0; position < mergedPosition; position++) {
                if (activePositions[position]) {
                    candidates.add(candidate(boxesByPosition, position, mergedPosition));
                }
            }
            activePositions[mergedPosition] = true;
            activeCount--;
        }

        List<Box> activeBoxes = new ArrayList<>(activeCount);
        for (int position = 0; position < nextPosition; position++) {
            if (activePositions[position]) {
                activeBoxes.add(boxesByPosition[position]);
            }
        }
        return activeBoxes;
    }

    private static MergeCandidate pollActiveCandidate(
        PriorityQueue<MergeCandidate> candidates,
        boolean[] activePositions
    ) {
        while (true) {
            MergeCandidate candidate = candidates.remove();
            if (
                activePositions[candidate.firstPosition()]
                    && activePositions[candidate.secondPosition()]
            ) {
                return candidate;
            }
        }
    }

    private static MergeCandidate candidate(
        Box[] boxesByPosition,
        int firstPosition,
        int secondPosition
    ) {
        return new MergeCandidate(
            (firstPosition << POSITION_BITS) | secondPosition,
            distance(
                boxesByPosition[firstPosition],
                boxesByPosition[secondPosition]
            )
        );
    }

    private static int maxCandidateCount(int boxCount, int maxBoxes) {
        int initialCandidates = boxCount * (boxCount - 1) / 2;
        int mergeCount = boxCount - maxBoxes;
        int addedCandidates = mergeCount * (boxCount + maxBoxes - 3) / 2;
        return initialCandidates + addedCandidates;
    }

    private static double distance(Box first, Box second) {
        double dx = Math.max(
            0.0,
            Math.max(first.minX - second.maxX, second.minX - first.maxX)
        );
        double dy = Math.max(
            0.0,
            Math.max(first.minY - second.maxY, second.minY - first.maxY)
        );
        double dz = Math.max(
            0.0,
            Math.max(first.minZ - second.maxZ, second.minZ - first.maxZ)
        );
        return dx * dx + dy * dy + dz * dz;
    }

    private static Box encompass(Box first, Box second) {
        return new Box(
            Math.min(first.minX, second.minX),
            Math.min(first.minY, second.minY),
            Math.min(first.minZ, second.minZ),
            Math.max(first.maxX, second.maxX),
            Math.max(first.maxY, second.maxY),
            Math.max(first.maxZ, second.maxZ)
        );
    }

    private static final class MergeCandidate {
        final int positionPair;
        final double distance;

        MergeCandidate(int positionPair, double distance) {
            this.positionPair = positionPair;
            this.distance = distance;
        }

        int firstPosition() {
            return positionPair >>> POSITION_BITS;
        }

        int secondPosition() {
            return positionPair & POSITION_MASK;
        }
    }
}
