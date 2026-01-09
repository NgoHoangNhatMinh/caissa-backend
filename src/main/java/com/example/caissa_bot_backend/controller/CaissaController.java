package com.example.caissa_bot_backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.caissa_bot_backend.Engine;
import com.example.caissa_bot_backend.board_representation.Bitboard;
import com.example.caissa_bot_backend.board_representation.Move;

@RestController
@CrossOrigin(origins = "*")
public class CaissaController {
    private Bitboard board = new Bitboard();

    public CaissaController() {
        board.init();
    }

    @GetMapping("/board")
    public Bitboard getBoard() {
        return board;
    }

    @PostMapping("/reset")
    public Bitboard resetBoard() {
        board.init();
        return board;
    }

    @PostMapping("/move")
    public Bitboard playerMove(@RequestBody Move move) {
        board.makeMove(move);
        return board;
    }

    @GetMapping("/engine_move")
    public Bitboard engineMove() {
        Engine engine = new Engine(board, 0);
        board.makeMove(engine.generateBestMove());
        return board;
    }

    @PostMapping("/best")
    public Move getBesMove(@RequestBody Map<String, Object> body) {
        String fen = (String) body.get("fen");
        Integer depth = body.get("depth") != null ? ((Number) body.get("depth")).intValue() : 3;
        return Engine.generateBestMove(fen, depth);
    }
}
