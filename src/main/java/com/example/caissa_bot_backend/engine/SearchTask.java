package com.example.caissa_bot_backend.engine;

import java.util.concurrent.RecursiveTask;

import com.example.caissa_bot_backend.Board;

public class SearchTask extends RecursiveTask<Integer> {
    private Board board;
    private int depth, alpha, beta;

    public SearchTask(Board board, int depth, int alpha, int beta) {
        this.board = board;
        this.depth = depth;
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    protected Integer compute() {
        return Search.negaMax(board, depth, alpha, beta);
    }

}
