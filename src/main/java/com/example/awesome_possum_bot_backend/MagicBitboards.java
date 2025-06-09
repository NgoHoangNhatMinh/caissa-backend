package com.example.awesome_possum_bot_backend;

import java.util.Random;
import java.util.Arrays;

public class MagicBitboards {
    public static long[] bishopRelevantOccupancy = new long[64];
    public static long[] rookRelevantOccupancy = new long[64];
    public static long[][] rookBlockers = new long[64][4096];
    public static long[][] bishopBlockers = new long[64][512];
    public static long[][] rookAttacks = new long[64][4096];
    public static long[][] bishopAttacks = new long[64][512];
    public static long[] rookMagic = new long[64];
    public static long[] bishopMagic = new long[64];

    static {
        for (int i = 0; i < 64; i++) {
            bishopRelevantOccupancy[i] = generateBishopRelevantOccupancy(i);
            rookRelevantOccupancy[i] = generateRookRelevantOccupancy(i);
            rookBlockers[i] = generateBlockerPermutations(rookRelevantOccupancy[i]);
            bishopBlockers[i] = generateBlockerPermutations(bishopRelevantOccupancy[i]);

            for (int j = 0; j < rookBlockers[i].length; j++) {
                rookAttacks[i][j] = computeRookAttacks(i, rookBlockers[i][j]);
            }
            rookMagic[i] = findRookMagic(i);

            for (int j = 0; j < bishopBlockers[i].length; j++) {
                bishopAttacks[i][j] = computeBishopAttacks(i, bishopBlockers[i][j]);
            }
            bishopMagic[i] = findBishopMagic(i);
        }
    }

    public static void init() {

    }

    private static long generateRookRelevantOccupancy(int i) {
        long relevantOccupancy = 0L;
        int r = i / 8;
        int c = i % 8;
        for (int j = c + 1; j < 7; j++)
            relevantOccupancy |= 1L << (8 * r + j);
        for (int j = c - 1; j > 0; j--)
            relevantOccupancy |= 1L << (8 * r + j);
        for (int j = r + 1; j < 7; j++)
            relevantOccupancy |= 1L << (8 * j + c);
        for (int j = r - 1; j > 0; j--)
            relevantOccupancy |= 1L << (8 * j + c);
        return relevantOccupancy;
    }

    public static long computeRookAttacks(int square, long blockers) {
        long attacks = 0L;
        int r = square / 8, c = square % 8;
        for (int i = r + 1; i < 8; i++) {
            int target = 8 * i + c;
            attacks |= 1L << target;
            if ((blockers & (1L << target)) != 0)
                break;
        }
        for (int i = r - 1; i >= 0; i--) {
            int target = 8 * i + c;
            attacks |= 1L << target;
            if ((blockers & (1L << target)) != 0)
                break;
        }
        for (int i = c + 1; i < 8; i++) {
            int target = 8 * r + i;
            attacks |= 1L << target;
            if ((blockers & (1L << target)) != 0)
                break;
        }
        for (int i = c - 1; i >= 0; i--) {
            int target = 8 * r + i;
            attacks |= 1L << target;
            if ((blockers & (1L << target)) != 0)
                break;
        }
        return attacks;
    }

    private static long findRookMagic(int square) {
        Random rand = new Random();
        int numBits = Long.bitCount(rookRelevantOccupancy[square]);
        long[] blockers = rookBlockers[square];
        long[] usedAttacks = new long[1 << numBits];

        // Keep generating new magic number until we find a good one
        while (true) {
            long magic = randomMagic(rand);
            Arrays.fill(usedAttacks, 0);
            boolean fail = false;
            boolean[] used = new boolean[1 << numBits];

            // Loop through all blockers to check index
            for (int i = 0; i < blockers.length; i++) {
                int index = (int) ((blockers[i] * magic) >>> (64 - numBits));
                if (!used[index]) {
                    usedAttacks[index] = rookAttacks[square][i];
                    used[index] = true;
                } else if (usedAttacks[index] != rookAttacks[square][i]) {
                    fail = true;
                    break;
                }
            }

            if (!fail)
                return magic;
        }
    }

    private static long generateBishopRelevantOccupancy(int i) {
        long relevantOccupancy = 0L;
        int r = i / 8;
        int c = i % 8;
        for (int j = r + 1, k = c + 1; j < 7 && k < 7; j++, k++)
            relevantOccupancy |= 1l << (8 * j + k);
        for (int j = r + 1, k = c - 1; j < 7 && k > 0; j++, k--)
            relevantOccupancy |= 1l << (8 * j + k);
        for (int j = r - 1, k = c + 1; j > 0 && k < 7; j--, k++)
            relevantOccupancy |= 1l << (8 * j + k);
        for (int j = r - 1, k = c - 1; j > 0 && k > 0; j--, k--)
            relevantOccupancy |= 1l << (8 * j + k);
        return relevantOccupancy;
    }

    public static long computeBishopAttacks(int square, long blockers) {
        long attacks = 0L;
        int r = square / 8, c = square % 8;
        for (int i = r + 1, j = c + 1; i < 8 && j < 8; i++, j++) {
            int target = 8 * i + j;
            attacks |= 1L << target;
            if ((blockers & (1L << target)) != 0)
                break;
        }
        for (int i = r + 1, j = c - 1; i < 8 && j >= 0; i++, j--) {
            int target = 8 * i + j;
            attacks |= 1L << target;
            if ((blockers & (1L << target)) != 0)
                break;
        }
        for (int i = r - 1, j = c + 1; i >= 0 && j < 8; i--, j++) {
            int target = 8 * i + j;
            attacks |= 1L << target;
            if ((blockers & (1L << target)) != 0)
                break;
        }
        for (int i = r - 1, j = c - 1; i >= 0 && j >= 0; i--, j--) {
            int target = 8 * i + j;
            attacks |= 1L << target;
            if ((blockers & (1L << target)) != 0)
                break;
        }
        return attacks;
    }

    private static long findBishopMagic(int square) {
        Random rand = new Random();
        int numBits = Long.bitCount(bishopRelevantOccupancy[square]);
        long[] blockers = bishopBlockers[square];
        long[] usedAttacks = new long[1 << numBits];

        // Keep generating new magic number until we find a good one
        while (true) {
            long magic = randomMagic(rand);
            Arrays.fill(usedAttacks, 0);
            boolean fail = false;
            boolean[] used = new boolean[1 << numBits];
            // Loop through all blockers to check index
            for (int i = 0; i < blockers.length; i++) {
                int index = (int) ((blockers[i] * magic) >>> (64 - numBits));
                if (!used[index]) {
                    usedAttacks[index] = bishopAttacks[square][i];
                    used[index] = true;
                } else if (usedAttacks[index] != bishopAttacks[square][i]) {
                    fail = true;
                    break;
                }
            }

            if (!fail)
                return magic;
        }
    }

    private static long[] generateBlockerPermutations(long mask) {
        int numBits = Long.bitCount(mask);
        int permutations = 1 << numBits;
        long[] blockers = new long[permutations];

        // Get the indices of all the set bits in the mask
        int[] bitPositions = new int[numBits];
        int idx = 0;
        for (int i = 0; i < 64; i++) {
            if ((mask & (1L << i)) != 0)
                bitPositions[idx++] = i;
        }

        for (int i = 0; i < permutations; i++) {
            // Each i's binary representation is a possible combination of bit set based on
            // the bitPosition
            long blocker = 0L;
            for (int j = 0; j < numBits; j++) {
                if ((i & (1 << j)) != 0) {
                    blocker |= (1L << bitPositions[j]);
                }
            }
            blockers[i] = blocker;
        }

        return blockers;
    }

    private static long randomMagic(Random rand) {
        // Generate a random 64-bit number with a few bits set
        return rand.nextLong() & rand.nextLong() & rand.nextLong();
    }
}