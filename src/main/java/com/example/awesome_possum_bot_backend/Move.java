package com.example.awesome_possum_bot_backend;

import java.util.HashMap;

public class Move {
    HashMap<String, Integer> piecesMap = new HashMap<String, Integer>();
    public int from;
    public int to;
    public int piece;
    public int promotionPiece = -1;
    public boolean isWhite;
    public boolean isShortCastling = false;
    public boolean isLongCastling = false;
    public boolean isEnPassant = false;
    public int capturedSquare = -1;
    public String fromString;
    public String toString;

    // Default constructor for serialization
    public Move() {

    }

    public Move(String move, boolean isWhite) {
        piecesMap.put("WP", 0);
        piecesMap.put("WN", 1);
        piecesMap.put("WB", 2);
        piecesMap.put("WR", 3);
        piecesMap.put("WQ", 4);
        piecesMap.put("WK", 5);
        piecesMap.put("BP", 6);
        piecesMap.put("BN", 7);
        piecesMap.put("BB", 8);
        piecesMap.put("BR", 9);
        piecesMap.put("BQ", 10);
        piecesMap.put("BK", 11);

        isShortCastling = false;
        isLongCastling = false;

        if (move.equals("0-0")) {
            isShortCastling = true;
            from = isWhite ? 60 : 4;
            to = isWhite ? 62 : 6;
            piece = isWhite ? 5 : 11;
        } else if (move.equals("0-0-0")) {
            isLongCastling = true;
            from = isWhite ? 60 : 4;
            to = isWhite ? 58 : 2;
            piece = isWhite ? 5 : 11;
        } else if (move.length() == 4 || move.length() == 6) {
            piece = isWhite ? piecesMap.get("WP") : piecesMap.get("BP");
            from = toNum(move.substring(0, 2));
            to = toNum(move.substring(2, 4));
            if (isWhite && move.length() == 6) {
                switch (move.toUpperCase().charAt(5)) {
                    case 'N':
                        promotionPiece = piecesMap.get("WN");
                        break;
                    case 'B':
                        promotionPiece = piecesMap.get("WB");
                        break;
                    case 'R':
                        promotionPiece = piecesMap.get("WR");
                        break;
                    case 'Q':
                        promotionPiece = piecesMap.get("WQ");
                        break;
                    case 'K':
                        promotionPiece = piecesMap.get("WK");
                        break;
                    default:
                        break;
                }
            } else if (!isWhite && move.length() == 6) {
                switch (move.toUpperCase().charAt(5)) {
                    case 'N':
                        promotionPiece = piecesMap.get("BN");
                        break;
                    case 'B':
                        promotionPiece = piecesMap.get("BB");
                        break;
                    case 'R':
                        promotionPiece = piecesMap.get("BR");
                        break;
                    case 'Q':
                        promotionPiece = piecesMap.get("BQ");
                        break;
                    case 'K':
                        promotionPiece = piecesMap.get("BK");
                        break;
                    default:
                        break;
                }
            }
        } else if (move.length() == 5) {
            if (isWhite) {
                switch (move.toUpperCase().charAt(0)) {
                    case 'N':
                        piece = piecesMap.get("WN");
                        break;
                    case 'B':
                        piece = piecesMap.get("WB");
                        break;
                    case 'R':
                        piece = piecesMap.get("WR");
                        break;
                    case 'Q':
                        piece = piecesMap.get("WQ");
                        break;
                    case 'K':
                        piece = piecesMap.get("WK");
                        break;
                    default:
                        break;
                }
            } else {
                switch (move.toUpperCase().charAt(0)) {
                    case 'N':
                        piece = piecesMap.get("BN");
                        break;
                    case 'B':
                        piece = piecesMap.get("BB");
                        break;
                    case 'R':
                        piece = piecesMap.get("BR");
                        break;
                    case 'Q':
                        piece = piecesMap.get("BQ");
                        break;
                    case 'K':
                        piece = piecesMap.get("BK");
                        break;
                    default:
                        break;
                }
            }
            from = toNum(move.substring(1, 3));
            to = toNum(move.substring(3, 5));
        }

        this.isWhite = isWhite;
        this.fromString = toSquare(from);
        this.toString = toSquare(to);
    }

    public Move(int from, int to, int piece, int capturedSquare, boolean isEP, int promotionPiece) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedSquare = capturedSquare;
        this.isEnPassant = isEP;
        this.promotionPiece = promotionPiece;
        this.fromString = toSquare(from);
        this.toString = toSquare(to);
    }

    public static int toNum(String s) {
        char l = s.toLowerCase().charAt(0);
        char n = s.charAt(1);
        return (56 - (int) n) * 8 + (int) l - 97;
    }

    public static String toSquare(int s) {
        char row = (char) ('1' + (7 - s / 8));
        char col = (char) ('a' + s % 8);
        return "" + col + row;
    }

    public boolean isPromotion() {
        return promotionPiece != -1;
    }

    public boolean isCastling() {
        return isShortCastling || isLongCastling;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public boolean isCapture() {
        return capturedSquare != -1;
    }

    @Override
    public String toString() {
        if (isLongCastling)
            return "Long castle";
        if (isShortCastling)
            return "Short castle";
        return Bitboard.PIECES_STRINGS[piece] + " from " + toSquare(from) + " to " + toSquare(to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof Move m) {
            if (isLongCastling || isShortCastling)
                return this.isLongCastling == m.isLongCastling || this.isShortCastling == m.isShortCastling;
            return ((this.piece == m.piece) && (this.from == m.from) && (this.to == m.to))
                    && (this.promotionPiece == m.promotionPiece);
        }
        return false;
    }
}
