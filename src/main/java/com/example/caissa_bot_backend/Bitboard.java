package com.example.caissa_bot_backend;

import java.util.ArrayList;
import java.util.Random;

import com.example.caissa_bot_backend.move_gen.AttacksGen;
import com.example.caissa_bot_backend.move_gen.MagicBitboards;

enum PE {
    WP, WN, WB, WR, WQ, WK, BP, BN, BB, BR, BQ, BK
}

public class Bitboard {
    public static final char[] GRAPHIC = { '♙', '♘', '♗', '♖', '♕', '♔', '♟', '♞', '♝', '♜', '♛', '♚', '.' };
    public static final String[] PIECES_STRINGS = { "WP", "WN", "WB", "WR", "WQ", "WK", "BP", "BN", "BB", "BR", "BQ",
            "BK" };
    public static final long RANK_4 = 0x000000FF00000000L;
    public static final long RANK_5 = 0x00000000FF000000L;

    // Bitboard gamestate
    // ----------------------------------------------------------------------

    public long[] pieces = new long[13];
    public long whiteOccupancy, blackOccupancy, occupancy, emptyOccupancy;

    public boolean canShortCastleWhite;
    public boolean canLongCastleWhite;
    public boolean canShortCastleBlack;
    public boolean canLongCastleBlack;

    public int enPassantSquare;

    private boolean isGameOver;

    // ----------------------------------------------------------------------

    static {
        AttacksGen.initAttacks();
        MagicBitboards.init();
    }

    public void init() {
        init("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public void init(String fen) {
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

        updateOccupancy();

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
    }

    // Update occupancy after a move and save the hash of the position
    public void updateOccupancy() {
        whiteOccupancy = pieces[0] | pieces[1] | pieces[2] | pieces[3] | pieces[4] | pieces[5];
        blackOccupancy = pieces[6] | pieces[7] | pieces[8] | pieces[9] | pieces[10] | pieces[11];
        occupancy = whiteOccupancy | blackOccupancy;
        emptyOccupancy = ~occupancy;
        pieces[12] = emptyOccupancy;
    }

    public Bitboard copy() {
        Bitboard newBB = new Bitboard();
        for (int i = 0; i < pieces.length; i++) {
            newBB.pieces[i] = pieces[i];
        }
        newBB.whiteOccupancy = whiteOccupancy;
        newBB.blackOccupancy = blackOccupancy;
        newBB.occupancy = occupancy;
        newBB.emptyOccupancy = emptyOccupancy;
        newBB.canShortCastleWhite = canShortCastleWhite;
        newBB.canLongCastleWhite = canLongCastleWhite;
        newBB.canShortCastleBlack = canShortCastleBlack;
        newBB.canLongCastleBlack = canLongCastleBlack;
        newBB.enPassantSquare = enPassantSquare;
        newBB.isGameOver = isGameOver;
        return newBB;
    }

    public void removePiece(int piece, int from) {
        pieces[piece] &= ~(1L << from);
    }

    public void addPiece(int piece, int to) {
        pieces[piece] |= (1L << to);
    }

    public int getPieceAt(int to) {
        if (to < 0 || to > 63)
            return -1;
        for (int i = 0; i < pieces.length - 1; i++) {
            if ((pieces[i] & (1L << to)) != 0) {
                return i;
            }
        }
        return -1;
    }

    public void shortCastle(boolean isWhite) {
        int king = isWhite ? 5 : 11;
        int rook = isWhite ? 3 : 9;
        int kingFrom = isWhite ? 60 : 4;
        int kingTo = isWhite ? 62 : 6;
        int rookFrom = isWhite ? 63 : 7;
        int rookTo = isWhite ? 61 : 5;

        pieces[king] &= ~(1L << kingFrom);
        pieces[rook] &= ~(1L << rookFrom);

        pieces[king] |= (1L << kingTo);
        pieces[rook] |= (1L << rookTo);

        disableCastle(isWhite);

        updateOccupancy();
    }

    public void longCastle(boolean isWhite) {
        int king = isWhite ? 5 : 11;
        int rook = isWhite ? 3 : 9;
        int kingFrom = isWhite ? 60 : 4;
        int kingTo = isWhite ? 58 : 2;
        int rookFrom = isWhite ? 56 : 0;
        int rookTo = isWhite ? 59 : 3;

        pieces[king] &= ~(1L << kingFrom);
        pieces[rook] &= ~(1L << rookFrom);

        pieces[king] |= (1L << kingTo);
        pieces[rook] |= (1L << rookTo);

        disableCastle(isWhite);

        updateOccupancy();
    }

    public void disableCastle(boolean isWhite) {
        if (isWhite) {
            canShortCastleWhite = false;
            canLongCastleWhite = false;
        } else {
            canShortCastleBlack = false;
            canLongCastleBlack = false;
        }
    }

    public long wSinglePushTargets() {
        return (pieces[0] >>> 8) & emptyOccupancy;
    }

    public long wDblPushTargets() {
        long singlePushs = wSinglePushTargets();
        return (singlePushs >>> 8) & emptyOccupancy & RANK_4;
    }

    public long bSinglePushTargets() {
        return (pieces[6] << 8) & emptyOccupancy;
    }

    public long bDblPushTargets() {
        long singlePushs = bSinglePushTargets();
        return (singlePushs << 8) & emptyOccupancy & RANK_5;
    }

    public boolean isKingInCheck(boolean isWhite) {
        int piece = isWhite ? 5 : 11;
        long king = pieces[piece];
        int from = Long.numberOfTrailingZeros(king);

        // Check all opponent pieces if they can attack king square
        long opponentPawns = isWhite ? pieces[6] : pieces[0];
        long opponentKnights = isWhite ? pieces[7] : pieces[1];
        long opponentBishops = isWhite ? pieces[8] : pieces[2];
        long opponentRooks = isWhite ? pieces[9] : pieces[3];
        long opponentQueens = isWhite ? pieces[10] : pieces[4];
        long opponentKing = isWhite ? pieces[11] : pieces[5];

        // putting the piece at the king square and attack out to opponent pieces
        long pawnAttacks = isWhite ? AttacksGen.whitePawnAttacks[from] : AttacksGen.blackPawnAttacks[from];
        if ((pawnAttacks & opponentPawns) != 0)
            return true;

        if ((AttacksGen.knightAttacks[from] & opponentKnights) != 0)
            return true;

        // Check for bishop and queen's diagional
        long bishopBlockers = occupancy & MagicBitboards.bishopRelevantOccupancy[from];
        long bishopAttacks = MagicBitboards.computeBishopAttacks(from, bishopBlockers);
        if ((bishopAttacks & (opponentBishops | opponentQueens)) != 0)
            return true;

        // Check for rook and queen's vertical/horizontal movements
        long rookBlockers = occupancy & MagicBitboards.rookRelevantOccupancy[from];
        long rookAttacks = MagicBitboards.computeRookAttacks(from, rookBlockers);
        if ((rookAttacks & (opponentRooks | opponentQueens)) != 0)
            return true;

        if ((AttacksGen.kingAttacks[from] & opponentKing) != 0)
            return true;

        return false;
    }

    public boolean isKingInCheck(boolean isWhite, int from) {
        int king = isWhite ? 5 : 11;
        long original = pieces[king];
        pieces[king] = 1L << from;
        boolean isChecked = isKingInCheck(isWhite);
        pieces[king] = original;
        return isChecked;
    }

    public boolean isInsufficientMaterial() {
        if ((pieces[0] | pieces[6]) == 0) { // No pawns
            int whitePieces = Long.bitCount(pieces[1] | pieces[2] | pieces[3] | pieces[4] | pieces[5]);
            int blackPieces = Long.bitCount(pieces[7] | pieces[8] | pieces[9] | pieces[10] | pieces[11]);
            if (whitePieces <= 1 && blackPieces <= 1) {
                return true; // Only kings or one minor piece each
            }
            if (Long.bitCount(pieces[2]) == 1 && Long.bitCount(pieces[8]) == 1) {
                // Only one bishop each on opposite colors
                return Long.numberOfTrailingZeros(pieces[2]) % 2 != Long.numberOfTrailingZeros(pieces[8]) % 2;
            }
        }
        return false;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public long[] getPieces() {
        return pieces;
    }

    public boolean isOccuppied(int sq) {
        return getPieceAt(sq) != -1;
    }

    @Override
    public String toString() {
        String board = "";
        for (int i = 0; i < 8; i++) {
            board += (8 - i) + "   ";
            for (int j = 0; j < 8; j++) {
                int offset = i * 8 + j;
                for (int k = 0; k < 13; k++) {
                    if ((pieces[k] & (1L << offset)) != 0) {
                        board += GRAPHIC[k] + " ";
                        break;
                    }
                }
            }
            board += "\n";
        }
        board += "\n    a b c d e f g h ";
        return board;
    }
}
