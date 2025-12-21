package com.example.caissa_bot_backend.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import com.example.caissa_bot_backend.board_representation.Board;
import com.example.caissa_bot_backend.board_representation.Move;;

public class Search {
    public static HashMap<Long, TranspositionEntry> transpositionTable = new HashMap<>();

    public static int negaMax(Board board, int depth, int alpha, int beta) {
        long hash = board.zobristHash();
        TranspositionEntry entry = transpositionTable.get(hash);
        // Only return transposition with deeper search
        if (entry != null && entry.depth >= depth) {
            return entry.score;
        }

        if (depth == 0 || board.isGameOver()) {
            return Evaluate.evaluate(board);
        }

        int maxEval = Integer.MIN_VALUE;
        ArrayList<Move> legalMoves = board.generateLegalMoves();
        legalMoves.sort(new MoveComparator(board));
        for (Move move : legalMoves) {
            board.makeMove(move);
            int eval = -negaMax(board, depth - 1, -beta, -alpha);
            board.undoMove();

            maxEval = Math.max(maxEval, eval);
            alpha = Math.max(maxEval, alpha);
            if (alpha >= beta)
                break;
        }

        // Save eval to transposition table
        transpositionTable.put(hash, new TranspositionEntry(maxEval, depth));
        return maxEval;
    }

    private static class MoveComparator implements Comparator<Move> {
        private Board board;

        public MoveComparator(Board board) {
            this.board = board;
        }

        @Override
        public int compare(Move m1, Move m2) {
            // Prioritize capture
            if (m1.isCapture() && !m2.isCapture())
                return -1;
            if (!m1.isCapture() && m2.isCapture())
                return 1;

            // Prioritize promotion
            if (m1.isPromotion() && !m2.isPromotion())
                return -1;
            if (!m1.isPromotion() && m2.isPromotion())
                return 1;

            // Prioritize checks
            if (givesCheck(m1) && !givesCheck(m2))
                return -1;
            if (!givesCheck(m1) && givesCheck(m2))
                return 1;

            return 0;
        }

        private boolean givesCheck(Move move) {
            board.makeMove(move);
            boolean isCheck = board.isOpponentKingInCheck();
            board.undoMove();
            return isCheck;
        }
    }

    private static class TranspositionEntry {
        public int score;
        public int depth;

        public TranspositionEntry(int score, int depth) {
            this.score = score;
            this.depth = depth;
        }
    }
}
