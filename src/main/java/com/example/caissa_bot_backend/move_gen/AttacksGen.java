package com.example.caissa_bot_backend.move_gen;

public class AttacksGen {
    // Pre-computed column masks
    public static final long H = 0x8080808080808080L;
    public static final long A = 0x0101010101010101L;
    public static final long GH = 0xC0C0C0C0C0C0C0C0L;
    public static final long AB = 0x0303030303030303L;

    // Pre-computed attacks
    public static long[] knightAttacks = new long[64];
    public static long[] kingAttacks = new long[64];
    public static long[] whitePawnAttacks = new long[64];
    public static long[] blackPawnAttacks = new long[64];

    // Pre-compute the loopup attacks table for knight and king
    static {
        for (int i = 0; i < 64; i++) {
            knightAttacks[i] = generateKnightAttack(i);
            kingAttacks[i] = generateKingAttack(i);
            whitePawnAttacks[i] = generateWhitePawnAttack(i);
            blackPawnAttacks[i] = generateBlackPawnAttack(i);
        }
    }

    // Generate the possible knight attacks on 1 of the 64 squares
    public static long generateKnightAttack(int square) {
        long init = 1L << square;
        long knightAttack = 0L;

        // Check if the attack doesn't wrap around to the opposite side of the board
        knightAttack |= (init >>> 6) & ~AB;
        knightAttack |= (init >>> 10) & ~GH;
        knightAttack |= (init >>> 17) & ~H;
        knightAttack |= (init >>> 15) & ~A;

        knightAttack |= (init << 6) & ~GH;
        knightAttack |= (init << 10) & ~AB;
        knightAttack |= (init << 17) & ~A;
        knightAttack |= (init << 15) & ~H;

        return knightAttack;
    }

    // Generate the possible king attacks on 1 of the 64 squares
    public static long generateKingAttack(int square) {
        long init = 1L << square;
        long kingAttack = 0L;

        // Check if the attack doesn't wrap around to the opposite side of the board
        kingAttack |= (init << 1) & ~A;
        kingAttack |= (init << 9) & ~A;
        kingAttack |= (init >>> 7) & ~A;

        kingAttack |= (init >>> 1) & ~H;
        kingAttack |= (init >>> 9) & ~H;
        kingAttack |= (init << 7) & ~H;

        kingAttack |= (init >>> 8) | (init << 8);

        return kingAttack;
    }

    public static long generateWhitePawnAttack(int square) {
        long init = 1L << square;
        long pawnAttack = 0L;

        pawnAttack |= (init >>> 7) & ~A;
        pawnAttack |= (init >>> 9) & ~H;

        return pawnAttack;
    }

    public static long generateBlackPawnAttack(int square) {
        long init = 1L << square;
        long pawnAttack = 0L;

        pawnAttack |= (init << 7) & ~H;
        pawnAttack |= (init << 9) & ~A;

        return pawnAttack;
    }
}
