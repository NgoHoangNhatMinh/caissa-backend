package com.example.caissa_bot_backend.board_representation;

import com.example.caissa_bot_backend.move_gen.AttacksGen;
import com.example.caissa_bot_backend.move_gen.MagicBitboards;
import com.example.caissa_bot_backend.utils.FenParser;

enum PE {
    WP, WN, WB, WR, WQ, WK, BP, BN, BB, BR, BQ, BK
}

public class Bitboard {
    public static final char[] GRAPHIC = { '♙', '♘', '♗', '♖', '♕', '♔', '♟', '♞', '♝', '♜', '♛', '♚', '.' };
    public static final String[] PIECES_STRINGS = { "WP", "WN", "WB", "WR", "WQ", "WK", "BP", "BN", "BB", "BR", "BQ",
            "BK" };

    public long[] pieces = new long[13];
    public long whiteOccupancy, blackOccupancy, occupancy, emptyOccupancy;

    public boolean canShortCastleWhite;
    public boolean canLongCastleWhite;
    public boolean canShortCastleBlack;
    public boolean canLongCastleBlack;

    public int enPassantSquare;

    // Metadata
    private boolean isGameOver;

    public void init() {
        init("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public void init(String fen) {
        FenParser parser = new FenParser(fen);

        pieces = parser.pieces;
        updateOccupancy();

        canShortCastleWhite = parser.canShortCastleWhite;
        canLongCastleWhite = parser.canLongCastleWhite;
        canShortCastleBlack = parser.canShortCastleBlack;
        canLongCastleBlack = parser.canLongCastleBlack;

        enPassantSquare = parser.enPassantSquare;
    }

    // make a move and return a boolean to indicate if the 50-move count is reset;
    // if the move cause move count to reset, return true
    public boolean makeMove(Move move, boolean isWhite) {
        boolean moveCountReset = false;

        if (move.isShortCastling) {
            shortCastle(isWhite);
            enPassantSquare = -1;
        } else if (move.isLongCastling) {
            longCastle(isWhite);
            enPassantSquare = -1;
        } else {
            int from = move.from;
            int to = move.to;
            int piece = move.piece;

            if (piece == 0 || piece == 6)
                moveCountReset = true;

            removePiece(piece, from);

            // handle capture
            int captureSquare = move.capturedSquare;
            int capturedPiece = getPieceAt(captureSquare);
            if (capturedPiece != -1) {
                removePiece(capturedPiece, captureSquare);
                moveCountReset = true;
            }

            // handle promotion
            if (move.promotionPiece != -1)
                addPiece(move.promotionPiece, to);
            else
                addPiece(piece, to);

            // Handle castling's rights
            if (piece == 3) {
                if (from == 63)
                    canShortCastleWhite = false;
                else if (from == 56)
                    canLongCastleWhite = false;
            } else if (piece == 9) {
                if (from == 7)
                    canShortCastleBlack = false;
                else if (from == 0)
                    canLongCastleBlack = false;
            } else if (piece == 5 || piece == 11)
                disableCastle(isWhite);

            if (capturedPiece == 3) {
                if (to == 63)
                    canShortCastleWhite = false;
                else if (to == 56)
                    canLongCastleWhite = false;
            } else if (capturedPiece == 9) {
                if (to == 7)
                    canShortCastleBlack = false;
                else if (to == 0)
                    canLongCastleBlack = false;
            }

            // Check for en passant
            if (piece == 0 || piece == 6) {
                if (Math.abs(from - to) == 16) { // double move
                    enPassantSquare = isWhite ? from - 8 : from + 8; // square behind the double pawn push
                } else {
                    enPassantSquare = -1;
                }
            } else {
                enPassantSquare = -1;
            }
        }

        updateOccupancy();
        return moveCountReset;
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
