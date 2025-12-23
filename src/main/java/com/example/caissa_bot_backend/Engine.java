package com.example.caissa_bot_backend;

import java.util.ArrayList;
import java.util.List;

import com.example.caissa_bot_backend.board_representation.Board;
import com.example.caissa_bot_backend.board_representation.Move;
import com.example.caissa_bot_backend.engine.SearchTask;

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

        List<SearchTask> tasks = new ArrayList<>();

        for (Move move : legalMoves) {
            Board copy = board.copy();
            copy.makeMove(move);
            SearchTask task = new SearchTask(copy, depth - 1, Integer.MIN_VALUE,
                    Integer.MAX_VALUE);
            task.fork();
            tasks.add(task);
        }

        for (int i = 0; i < tasks.size(); i++) {
            int score = -tasks.get(i).join();
            if (score > bestScore) {
                bestScore = score;
                bestMove = legalMoves.get(i);
            }
        }

        return bestMove;
    }

}
