package com.example.caissa_bot_backend.move_gen;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

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

            for (int j = 0; j < bishopBlockers[i].length; j++) {
                bishopAttacks[i][j] = computeBishopAttacks(i, bishopBlockers[i][j]);
            }

            for (int j = 0; j < rookBlockers[i].length; j++) {
                rookAttacks[i][j] = computeRookAttacks(i, rookBlockers[i][j]);
            }

        }

        File rookFile = new File("rook_magics.txt");
        File bishopFile = new File("bishop_magics.txt");

        if (bishopFile.exists()) {
            bishopMagic = loadMagics("bishop_magics.txt", 64);
        } else {
            for (int i = 0; i < 64; i++) {
                bishopMagic[i] = findBishopMagic(i);
            }
            saveMagics("bishop_magics.txt", bishopMagic);
        }

        if (rookFile.exists()) {
            rookMagic = loadMagics("rook_magics.txt", 64);
        } else {
            for (int i = 0; i < 64; i++) {
                rookMagic[i] = findRookMagic(i);
            }
            saveMagics("rook_magics.txt", rookMagic);
        }

        for (int i = 0; i < 64; i++) {
            for (long blocker : rookBlockers[i]) {
                int index = (int) ((blocker * rookMagic[i]) >>> (64 - Long.bitCount(rookRelevantOccupancy[i])));
                rookAttacks[i][index] = computeRookAttacks(i, blocker);
            }
            for (long blocker : bishopBlockers[i]) {
                int index = (int) ((blocker * bishopMagic[i]) >>> (64 - Long.bitCount(bishopRelevantOccupancy[i])));
                bishopAttacks[i][index] = computeBishopAttacks(i, blocker);
            }
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
        int numBits = Long.bitCount(bishopRelevantOccupancy[square]);
        int shift = 64 - numBits;
        long[] blockers = rookBlockers[square];
        Map<Integer, Long> seen = new HashMap<>();

        while (true) {
            long magic = randomMagic(rand);
            if (!isMagicCandidate(magic))
                continue;
            seen.clear();
            boolean fail = false;

            for (long blocker : blockers) {
                int index = (int) ((blocker * magic) >>> shift);
                long attack = computeRookAttacks(square, blocker);
                if (seen.containsKey(index) && seen.get(index) != attack) {
                    fail = true;
                    break;
                }
                seen.put(index, attack);
            }

            if (!fail) {
                System.out.println("Found rook magic for square " + square + ": " + magic);
                return magic;
            }
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
        int shift = 64 - numBits;
        long[] blockers = bishopBlockers[square];
        Map<Integer, Long> seen = new HashMap<>();

        while (true) {
            long magic = randomMagic(rand);
            if (!isMagicCandidate(magic))
                continue;
            seen.clear();
            boolean fail = false;

            for (long blocker : blockers) {
                int index = (int) ((blocker * magic) >>> shift);
                long attack = computeBishopAttacks(square, blocker);
                if (seen.containsKey(index) && seen.get(index) != attack) {
                    fail = true;
                    break;
                }
                seen.put(index, attack);
            }

            if (!fail) {
                System.out.println("Found bishop magic for square " + square + ": " + magic);
                return magic;
            }
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
        return (rand.nextLong() & rand.nextLong() & rand.nextLong());
    }

    private static boolean isMagicCandidate(long magic) {
        // Filter out too dense candidates
        return Long.bitCount(magic & 0xFF00000000000000L) >= 6;
    }

    private static void saveMagics(String filename, long[] magics) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (long magic : magics) {
                writer.println(magic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long[] loadMagics(String filename, int size) {
        long[] magics = new long[size];
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            for (int i = 0; i < size; i++) {
                String line = reader.readLine();
                if (line == null)
                    throw new IOException("Not enough lines in magic file");
                magics[i] = Long.parseLong(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return magics;
    }
}
