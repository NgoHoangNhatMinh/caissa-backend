package com.example.caissa_bot_backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class Board {
    // Gamestate
    // -------------------------------------------------------------------------------------------------------

    private Bitboard bitboard;
    private boolean isWhite;
    private int halfMovesSinceReset = 0;
    private int fullMoves = 1;
    private Map<Long, Integer> zobristMap = new HashMap<>();

    // -------------------------------------------------------------------------------------------------------

    private Stack<Board> gameHistory = new Stack<>();

    // -------------------------------------------------------------------------------------------------------

    // For engine play
    // -------------------------------------------------------------------------------------------------------

    public boolean isWhiteBot = false;
    public boolean isBlackBot = false;
    public int engineDepth = 3;

    // -------------------------------------------------------------------------------------------------------

    public void init() {
        init("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public void init(String fen) {
        String[] parts = fen.trim().split("\\s+");
        if (parts.length > 6)
            throw new IllegalArgumentException("Invalid FEN string");

        bitboard = new Bitboard();
        bitboard.init(fen);

        // Color to move
        String color = parts[1];
        if (color.equals("w")) {
            isWhite = true;
        } else if (color.equals("b")) {
            isWhite = false;
        } else {
            throw new IllegalArgumentException("Invalid color in FEN string");
        }

        // Update halfMoves and fullMoves
        String halfMoveStr = parts[4];
        String fullMoveStr = parts[5];
        try {
            halfMovesSinceReset = Integer.parseInt(halfMoveStr);
            fullMoves = Integer.parseInt(fullMoveStr);
        } catch (NumberFormatException e) {
            System.out.println(
                    "Invalid half move count and full move count in FEN, defaulting to half move count = 100 and full move count = 1");
            fullMoves = 1;
            halfMovesSinceReset = 0;
        }
    }

    public void run() {
        OtherUtils.clearScreen();
        Scanner scanner = new Scanner(System.in);
        while (!bitboard.isGameOver()) {
            System.out.println(bitboard);

            // Mark the current position as visited by adding to zobrist map
            long hash = bitboard.zobristHash(isWhite);
            zobristMap.put(hash, zobristMap.getOrDefault(hash, 0) + 1);

            ArrayList<Move> legalMoves = generateLegalMoves();

            if (legalMoves.isEmpty()) {
                if (bitboard.isKingInCheck(isWhite))
                    System.out.println((isWhite ? "White" : "Black") + " is checkmated");
                else
                    System.out.println("The game is a draw by stalemate");
                break;
            }
            if (isFiftyMove()) {
                System.out.println("The game is a draw by 50-move rule");
                break;
            }
            if (isThreefoldRepetition()) {
                System.out.println("The game is a draw by threefold repetition");
                break;
            }
            if (isInsufficientMaterial()) {
                System.out.println("The game is a draw by insufficient material");
                break;
            }

            if (bitboard.isKingInCheck(isWhite)) {
                System.out.println((isWhite ? "White" : "Black") + " is in check");
            }

            // Inputting move
            System.out.println(fullMoves + ". " + (isWhite ? "White" : "Black") + " to move: ");

            Move selectedMove = null;
            if (isWhite && isWhiteBot || !isWhite && isBlackBot) {
                // Bot move generation
                // selectedMove = Engine.generateBestMove(legalMoves);
                selectedMove = Engine.generateBestMove(this, engineDepth);
                System.out.println("Bot chose: " + selectedMove);
            } else {
                String moveString = scanner.nextLine();
                Move move = new Move(moveString, isWhite);
                System.out.println(move);
                for (Move m : legalMoves) {
                    if (m.equals(new Move(moveString, isWhite))) {
                        selectedMove = m;
                    }
                }
            }

            // If legal move, board make move
            if (selectedMove != null) {
                makeMove(selectedMove);
            } else {
                System.out.println("This is not a legal move\n");
            }
        }
        scanner.close();
    }

    public Board copy() {
        Board newBoard = new Board();
        newBoard.bitboard = bitboard.copy();
        newBoard.isWhite = isWhite;
        newBoard.halfMovesSinceReset = halfMovesSinceReset;
        newBoard.fullMoves = fullMoves;
        for (Long key : zobristMap.keySet()) {
            newBoard.zobristMap.put(key, zobristMap.get(key));
        }

        newBoard.isWhiteBot = isWhiteBot;
        newBoard.isBlackBot = isBlackBot;
        return newBoard;
    }

    public boolean isLegalMove(Move move) {
        ArrayList<Move> legalMoves = generateLegalMoves();

        for (Move m : legalMoves) {
            if (m.equals(move))
                return true;
        }
        return false;
    }

    public ArrayList<Move> generatePseudoLegalMoves() {
        // pseudoMoves have not accounted for king being checked after the moves are
        // made
        ArrayList<Move> pseudoMoves = new ArrayList<Move>();

        pseudoMoves.addAll(bitboard.generatePawnMoves(isWhite));
        pseudoMoves.addAll(bitboard.generateKnightMoves(isWhite));
        pseudoMoves.addAll(bitboard.generateBishopMoves(isWhite));
        pseudoMoves.addAll(bitboard.generateRookMoves(isWhite));
        pseudoMoves.addAll(bitboard.generateQueenMoves(isWhite));
        pseudoMoves.addAll(bitboard.generateKingMoves(isWhite));
        pseudoMoves.addAll(bitboard.generateCastlingMoves(isWhite));

        return pseudoMoves;
    }

    public ArrayList<Move> generateLegalMoves() {
        ArrayList<Move> pseudoMoves = generatePseudoLegalMoves();
        ArrayList<Move> legalMoves = new ArrayList<>();

        for (Move move : pseudoMoves) {
            Board boardCopy = this.copy();
            boardCopy.makeMove(move);
            if (!boardCopy.bitboard.isKingInCheck(isWhite))
                legalMoves.add(move);
        }

        return legalMoves;
    }

    public void makeMove(Move move) {
        // Save current state for undo functionality
        gameHistory.push(this.copy());

        // Make move
        if (move.isShortCastling) {
            bitboard.shortCastle(isWhite);
            bitboard.enPassantSquare = -1;
        } else if (move.isLongCastling) {
            bitboard.longCastle(isWhite);
            bitboard.enPassantSquare = -1;
        } else {
            int from = move.from;
            int to = move.to;
            int piece = move.piece;

            if (piece == 0 || piece == 6)
                halfMovesSinceReset = 0;

            // remove piece from source
            bitboard.removePiece(piece, from);

            // handle capture
            int captureSquare = move.capturedSquare;
            int capturedPiece = bitboard.getPieceAt(captureSquare);
            if (capturedPiece != -1) {
                bitboard.removePiece(capturedPiece, captureSquare);
                halfMovesSinceReset = 0;
            }

            // handle promotion
            if (move.promotionPiece != -1)
                bitboard.addPiece(move.promotionPiece, to);
            else
                bitboard.addPiece(piece, to);

            // Handle castling's rights
            if (piece == 3) {
                if (from == 63)
                    bitboard.canShortCastleWhite = false;
                else if (from == 56)
                    bitboard.canLongCastleWhite = false;
            } else if (piece == 9) {
                if (from == 7)
                    bitboard.canShortCastleBlack = false;
                else if (from == 0)
                    bitboard.canLongCastleBlack = false;
            } else if (piece == 5 || piece == 11)
                bitboard.disableCastle(isWhite);

            if (capturedPiece == 3) {
                if (to == 63)
                    bitboard.canShortCastleWhite = false;
                else if (to == 56)
                    bitboard.canLongCastleWhite = false;
            } else if (capturedPiece == 9) {
                if (to == 7)
                    bitboard.canShortCastleBlack = false;
                else if (to == 0)
                    bitboard.canLongCastleBlack = false;
            }

            // Check for en passant
            if (piece == 0 || piece == 6) {
                if (Math.abs(from - to) == 16) { // double move
                    bitboard.enPassantSquare = isWhite ? from - 8 : from + 8; // square behind the double pawn push
                } else {
                    bitboard.enPassantSquare = -1;
                }
            } else {
                bitboard.enPassantSquare = -1;
            }
        }

        halfMovesSinceReset++;
        if (!isWhite)
            fullMoves++;
        bitboard.updateOccupancy();
        switchPlayer();
    }

    public void undoMove() {
        if (gameHistory.isEmpty()) {
            System.out.println("No moves to undo");
            return;
        }
        Board previousBoard = gameHistory.pop();
        this.bitboard = previousBoard.bitboard.copy();
        this.isWhite = previousBoard.isWhite;
        this.halfMovesSinceReset = previousBoard.halfMovesSinceReset;
        this.fullMoves = previousBoard.fullMoves;
        this.zobristMap = new HashMap<>(previousBoard.zobristMap);
        this.isWhiteBot = previousBoard.isWhiteBot;
        this.isBlackBot = previousBoard.isBlackBot;
        bitboard.updateOccupancy();
    }

    public boolean isThreefoldRepetition() {
        long hash = zobristHash();
        return zobristMap.getOrDefault(hash, 0) >= 3;
    }

    public boolean isCheck() {
        return bitboard.isKingInCheck(isWhite);
    }

    public boolean isOpponentKingInCheck() {
        return bitboard.isKingInCheck(!isWhite);
    }

    public boolean isCheckmate() {
        return isCheck() && generateLegalMoves().isEmpty();
    }

    public boolean isStalemate() {
        return !isCheck() && generateLegalMoves().isEmpty();
    }

    public void switchPlayer() {
        isWhite = !isWhite;
    }

    public void printBoard() {
        System.out.println(bitboard);
    }

    public int getEnPassantSquare() {
        return bitboard.enPassantSquare;
    }

    public Bitboard getBitboard() {
        return bitboard;
    }

    public boolean isWhiteToMove() {
        return isWhite;
    }

    public boolean isGameOver() {
        return isCheckmate() || isStalemate() || isThreefoldRepetition() || isFiftyMove() || isInsufficientMaterial();
    }

    public long zobristHash() {
        return bitboard.zobristHash(isWhite);
    }

    public boolean isFiftyMove() {
        return halfMovesSinceReset >= 100;
    }

    public boolean isInsufficientMaterial() {
        return bitboard.isInsufficientMaterial();
    }
}
