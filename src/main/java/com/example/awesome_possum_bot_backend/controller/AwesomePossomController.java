package com.example.awesome_possum_bot_backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.awesome_possum_bot_backend.Board;
import com.example.awesome_possum_bot_backend.Engine;
import com.example.awesome_possum_bot_backend.Move;

@RestController
@CrossOrigin(origins = "*")
public class AwesomePossomController {
    private Board board = new Board();

    public AwesomePossomController() {
        board.init();
    }

    @GetMapping("/board")
    public Board getBoard() {
        return board;
    }

    @PostMapping("/reset")
    public Board resetBoard() {
        board.init();
        return board;
    }

    @PostMapping("/move")
    public Board playerMove(@RequestBody Move move) {
        board.makeMove(move);
        return board;
    }

    @GetMapping("/engine_move")
    public Board engineMove() {
        board.makeMove(Engine.generateBestMove(board, 0));
        return board;
    }

    @PostMapping("/best")
    public Move getBesMove(@RequestBody Map<String, Object> body) {
        String fen = (String) body.get("fen");
        Integer depth = body.get("depth") != null ? ((Number) body.get("depth")).intValue() : 3; 
        return Engine.generateBestMove(fen, depth);
    }
}
