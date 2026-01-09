package com.example.caissa_bot_backend.utils;

import com.example.caissa_bot_backend.board_representation.Move;

public class FenParser {
    public long[] pieces = new long[13];
    public boolean isWhite, canShortCastleWhite, canShortCastleBlack, canLongCastleWhite, canLongCastleBlack;
    public int enPassantSquare;
    public int halfMovesSinceReset, fullMoves;

    public FenParser(String fen) {
        String[] parts = fen.trim().split("\\s+");
        if (parts.length > 6)
            throw new IllegalArgumentException("Invalid FEN string");

        // Piece placement
        String[] ranks = parts[0].split("/");
        for (int i = 0; i < 8; i++) {
            int file = 0;
            for (char c : ranks[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    file += Character.getNumericValue(c); // Skip empty squares
                } else {
                    int pos = i * 8 + file;
                    int piece = -1;
                    switch (c) {
                        case 'P':
                            piece = 0;
                            break;
                        case 'N':
                            piece = 1;
                            break;
                        case 'B':
                            piece = 2;
                            break;
                        case 'R':
                            piece = 3;
                            break;
                        case 'Q':
                            piece = 4;
                            break;
                        case 'K':
                            piece = 5;
                            break;
                        case 'p':
                            piece = 6;
                            break;
                        case 'n':
                            piece = 7;
                            break;
                        case 'b':
                            piece = 8;
                            break;
                        case 'r':
                            piece = 9;
                            break;
                        case 'q':
                            piece = 10;
                            break;
                        case 'k':
                            piece = 11;
                            break;
                    }
                    if (piece != -1) {
                        pieces[piece] |= (1L << pos);
                    }
                    file++;
                }
            }
        }

        String color = parts[1];
        isWhite = color.equals("w");

        String castling = parts[2];
        canShortCastleWhite = castling.contains("K");
        canLongCastleWhite = castling.contains("Q");
        canShortCastleBlack = castling.contains("k");
        canLongCastleBlack = castling.contains("q");

        String enPassant = parts[3];
        if (enPassant.equals("-")) {
            enPassantSquare = -1;
        } else {
            enPassantSquare = Move.toNum(enPassant);
            if (enPassantSquare < 0 || enPassantSquare > 63) {
                throw new IllegalArgumentException("Invalid en passant square in FEN string");
            }
        }

        String halfMovesStr = parts[4];
        String fullMoveStr = parts[5];
        try {
            halfMovesSinceReset = Integer.parseInt(halfMovesStr);
            fullMoves = Integer.parseInt(fullMoveStr);
        } catch (NumberFormatException e) {
            System.out.println(
                    "Invalid half move count or full move count in FEN, defaulting to half move count = 0 and full move count = 1");
            halfMovesSinceReset = 0;
            fullMoves = 1;
        }
    }
}
