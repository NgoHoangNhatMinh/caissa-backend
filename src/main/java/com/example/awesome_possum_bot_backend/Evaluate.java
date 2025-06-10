package com.example.awesome_possum_bot_backend;

public class Evaluate {
        public static final int CHECKMATE_SCORE = 100_000;
        public static final int DRAW_SCORE = 0;
        public static final int[] PIECE_VALUES = { 100, 320, 330, 500, 900, 200_000 };

        public static final int[] PAWN_SQUARES_VALUES = {
                        0, 0, 0, 0, 0, 0, 0, 0,
                        50, 50, 50, 50, 50, 50, 50, 50,
                        10, 10, 20, 30, 30, 20, 10, 10,
                        5, 5, 10, 25, 25, 10, 5, 5,
                        0, 0, 0, 20, 20, 0, 0, 0,
                        5, -5, -10, 0, 0, -10, -5, 5,
                        5, 10, 10, -20, -20, 10, 10, 5,
                        0, 0, 0, 0, 0, 0, 0, 0 };

        public static final int[] KNIGHT_SQUARES_VALUES = {
                        -50, -40, -30, -30, -30, -30, -40, -50,
                        -40, -20, 0, 0, 0, 0, -20, -40,
                        -30, 0, 10, 15, 15, 10, 0, -30,
                        -30, 5, 15, 20, 20, 15, 5, -30,
                        -30, 0, 15, 20, 20, 15, 0, -30,
                        -30, 5, 10, 15, 15, 10, 5, -30,
                        -40, -20, 0, 5, 5, 0, -20, -40,
                        -50, -40, -30, -30, -30, -30, -40, -50,
        };

        public static final int[] BISHOP_SQUARES_VALUES = {
                        -20, -10, -10, -10, -10, -10, -10, -20,
                        -10, 0, 0, 0, 0, 0, 0, -10,
                        -10, 0, 5, 10, 10, 5, 0, -10,
                        -10, 5, 5, 10, 10, 5, 5, -10,
                        -10, 0, 10, 10, 10, 10, 0, -10,
                        -10, 10, 10, 10, 10, 10, 10, -10,
                        -10, 5, 0, 0, 0, 0, 5, -10,
                        -20, -10, -10, -10, -10, -10, -10, -20,
        };

        public static final int[] ROOK_SQUARES_VALUES = {
                        0, 0, 0, 0, 0, 0, 0, 0,
                        5, 10, 10, 10, 10, 10, 10, 5,
                        -5, 0, 0, 0, 0, 0, 0, -5,
                        -5, 0, 0, 0, 0, 0, 0, -5,
                        -5, 0, 0, 0, 0, 0, 0, -5,
                        -5, 0, 0, 0, 0, 0, 0, -5,
                        -5, 0, 0, 0, 0, 0, 0, -5,
                        0, 0, 0, 5, 5, 0, 0, 0
        };

        public static final int[] QUEEN_SQUARES_VALUES = {
                        -20, -10, -10, -5, -5, -10, -10, -20,
                        -10, 0, 0, 0, 0, 0, 0, -10,
                        -10, 0, 5, 5, 5, 5, 0, -10,
                        -5, 0, 5, 5, 5, 5, 0, -5,
                        0, 0, 5, 5, 5, 5, 0, -5,
                        -10, 5, 5, 5, 5, 5, 0, -10,
                        -10, 0, 5, 0, 0, 0, 0, -10,
                        -20, -10, -10, -5, -5, -10, -10, -20
        };

        public static final int[] KING_SQUARES_MIDDLE_GAME_VALUES = {
                        -30, -40, -40, -50, -50, -40, -40, -30,
                        -30, -40, -40, -50, -50, -40, -40, -30,
                        -30, -40, -40, -50, -50, -40, -40, -30,
                        -30, -40, -40, -50, -50, -40, -40, -30,
                        -20, -30, -30, -40, -40, -30, -30, -20,
                        -10, -20, -20, -20, -20, -20, -20, -10,
                        20, 20, 0, 0, 0, 0, 20, 20,
                        20, 30, 10, 0, 0, 10, 30, 20
        };

        public static final int[] KING_SQUARES_END_GAME_VALUES = {
                        -50, -40, -30, -20, -20, -30, -40, -50,
                        -30, -20, -10, 0, 0, -10, -20, -30,
                        -30, -10, 20, 30, 30, 20, -10, -30,
                        -30, -10, 30, 40, 40, 30, -10, -30,
                        -30, -10, 30, 40, 40, 30, -10, -30,
                        -30, -10, 20, 30, 30, 20, -10, -30,
                        -30, -30, 0, 0, 0, 0, -30, -30,
                        -50, -30, -30, -30, -30, -30, -30, -50
        };

        // TODO: adjust king's values
        public static final int[][] PIECE_SQUARES_TABLE = { PAWN_SQUARES_VALUES, KNIGHT_SQUARES_VALUES,
                        BISHOP_SQUARES_VALUES, ROOK_SQUARES_VALUES, QUEEN_SQUARES_VALUES,
                        KING_SQUARES_MIDDLE_GAME_VALUES };

        public static int evaluate(Board board) {
                int who2Move = board.isWhiteToMove() ? 1 : -1;
                if (board.isCheckmate())
                        return -CHECKMATE_SCORE;
                if (board.isStalemate())
                        return DRAW_SCORE;
                if (board.isThreefoldRepetition())
                        return DRAW_SCORE;
                if (board.isFiftyMove())
                        return DRAW_SCORE;
                if (board.isInsufficientMaterial())
                        return DRAW_SCORE;

                int score = 0;
                for (int i = 0; i <= 5; i++) {
                        // Count material
                        long whitePiece = board.getBitboard().getPieces()[i];
                        long blackPiece = board.getBitboard().getPieces()[i + 6];

                        int numWhitePieces = Long.bitCount(whitePiece);
                        int numBlackPieces = Long.bitCount(blackPiece);

                        int materialScore = (numWhitePieces - numBlackPieces) * PIECE_VALUES[i];

                        // Evaluate piece value based on position
                        int positionalScore = 0;
                        while (whitePiece != 0) {
                                int sq = Long.numberOfTrailingZeros(whitePiece);
                                positionalScore += PIECE_SQUARES_TABLE[i][sq];
                                whitePiece &= whitePiece - 1;
                        }
                        while (blackPiece != 0) {
                                int sq = Long.numberOfTrailingZeros(blackPiece);
                                int mirroredSq = sq ^ 56;
                                positionalScore -= PIECE_SQUARES_TABLE[i][mirroredSq];
                                blackPiece &= blackPiece - 1;
                        }

                        score += who2Move * (materialScore + positionalScore);
                }
                return score;
        }
}
