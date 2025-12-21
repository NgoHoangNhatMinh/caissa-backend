package com.example.caissa_bot_backend.utils;

import java.util.Random;

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
}
