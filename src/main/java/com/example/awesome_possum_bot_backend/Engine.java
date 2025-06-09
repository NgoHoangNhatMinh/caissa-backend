package com.example.awesome_possum_bot_backend;

import java.util.ArrayList;

public class Engine {
    public static void main(String... args) {
        Board board = new Board();
        board.init();
        board.isWhiteBot = true;
        board.isBlackBot = true;
        int engineDepth = 5;
        if (args.length > 0) {
            try {
                engineDepth = Integer.parseInt(args[0]);
                board.engineDepth = engineDepth;
            } catch (NumberFormatException e) {
                System.out.println("Invalid depth argument, using default depth of 3.");
            }
        }
        System.out.println("\n");
        board.run();
    }

    public static Move generateBestMove(String fen, int depth) {
        Board board = new Board();
        board.init(fen);
        return generateBestMove(board, depth);
    }

    public static Move generateBestMove(Board board, int depth) {
        return rootNegaMax(board, depth);
    }

    private static Move rootNegaMax(Board board, int depth) {
        ArrayList<Move> legalMoves = board.generateLegalMoves();
        if (legalMoves.isEmpty())
            return null;

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move : legalMoves) {
            board.makeMove(move);
            int score = -Search.negaMax(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            board.undoMove();
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

}