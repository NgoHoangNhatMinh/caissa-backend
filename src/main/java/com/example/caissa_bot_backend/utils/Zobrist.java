package com.example.caissa_bot_backend.utils;

import java.util.Random;

import com.example.caissa_bot_backend.Bitboard;

public class Zobrist {
    private long[][] zobristPiece = new long[12][64];
    private long[] zobristCastle = new long[16];
    private long[] zobristEnPassant = new long[8];
    private long zobristWhiteToMove;

    public Zobrist() {
        Random random = new Random();

        for (int i = 0; i < zobristPiece.length; i++) {
            for (int j = 0; j < zobristPiece[i].length; j++) {
                zobristPiece[i][j] = random.nextLong();
            }
        }

        for (int i = 0; i < zobristCastle.length; i++) {
            zobristCastle[i] = random.nextLong();
        }

        for (int i = 0; i < zobristEnPassant.length; i++) {
            zobristEnPassant[i] = random.nextLong();
        }

        zobristWhiteToMove = random.nextLong();
    }

    public long zobristHash(Bitboard bitboard, boolean isWhite) {
        long hash = 0L;
        for (int i = 0; i < 12; i++) {
            long piece = bitboard.pieces[i];
            while (piece != 0) {
                int sq = Long.numberOfTrailingZeros(piece);
                hash ^= zobristPiece[i][sq];
                piece &= piece - 1;
            }
        }

        int castle = 0;
        if (bitboard.canShortCastleWhite)
            castle |= 1;
        if (bitboard.canLongCastleWhite)
            castle |= 2;
        if (bitboard.canShortCastleBlack)
            castle |= 4;
        if (bitboard.canLongCastleBlack)
            castle |= 8;
        hash ^= zobristCastle[castle];

        if (bitboard.enPassantSquare != -1) {
            hash ^= zobristEnPassant[bitboard.enPassantSquare % 8]; // Only the file is needed for the hash
        }

        if (!isWhite) {
            hash ^= zobristWhiteToMove;
        }

        return hash;
    }
}
