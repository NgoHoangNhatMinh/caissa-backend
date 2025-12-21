package com.example.caissa_bot_backend.move_gen;

import java.util.ArrayList;

import com.example.caissa_bot_backend.Bitboard;
import com.example.caissa_bot_backend.Move;

public class MoveGen {
    private static final long RANK_4 = 0x000000FF00000000L;
    private static final long RANK_5 = 0x00000000FF000000L;

    public static ArrayList<Move> generatePseudoLegalMoves(Bitboard bitboard, boolean isWhite) {
        // pseudoMoves have not accounted for king being checked after the moves are
        // made
        ArrayList<Move> pseudoMoves = new ArrayList<Move>();

        pseudoMoves.addAll(generatePawnMoves(bitboard, isWhite));
        pseudoMoves.addAll(generateKnightMoves(bitboard, isWhite));
        pseudoMoves.addAll(generateBishopMoves(bitboard, isWhite));
        pseudoMoves.addAll(generateRookMoves(bitboard, isWhite));
        pseudoMoves.addAll(generateQueenMoves(bitboard, isWhite));
        pseudoMoves.addAll(generateKingMoves(bitboard, isWhite));
        pseudoMoves.addAll(generateCastlingMoves(bitboard, isWhite));

        return pseudoMoves;
    }

    public static ArrayList<Move> generatePawnMoves(Bitboard bitboard, boolean isWhite) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int currPiece = isWhite ? 0 : 6;
        long singlePushs = isWhite ? wSinglePushTargets(bitboard) : bSinglePushTargets(bitboard);
        long doublePushs = isWhite ? wDblPushTargets(bitboard) : bDblPushTargets(bitboard);

        while (singlePushs != 0) {
            int to = Long.numberOfTrailingZeros(singlePushs);
            if (isWhite) {
                if (to / 8 == 0)
                    for (int i = 1; i < 5; i++)
                        possibleMoves.add(new Move(to + 8, to, currPiece, -1, false, i));
                else
                    possibleMoves.add(new Move(to + 8, to, currPiece, -1, false, -1));
            } else {
                if (to / 8 == 7)
                    for (int i = 7; i < 11; i++)
                        possibleMoves.add(new Move(to - 8, to, currPiece, -1, false, i));
                else
                    possibleMoves.add(new Move(to - 8, to, currPiece, -1, false, -1));
            }
            singlePushs &= singlePushs - 1;
        }

        while (doublePushs != 0) {
            int to = Long.numberOfTrailingZeros(doublePushs);
            if (isWhite) {
                possibleMoves.add(new Move(to + 16, to, 0, -1, false, -1));
            } else {
                possibleMoves.add(new Move(to - 16, to, 6, -1, false, -1));
            }
            doublePushs &= doublePushs - 1;
        }

        // En Passant
        long pawns = bitboard.pieces[currPiece];
        long opponentOccupancy = isWhite ? bitboard.blackOccupancy : bitboard.whiteOccupancy;

        while (pawns != 0) {
            int from = Long.numberOfTrailingZeros(pawns);

            int enPassantSquare = bitboard.enPassantSquare;
            if (enPassantSquare != -1) {
                // System.out.println("En passant square: " + enPassantSquare);
                if (isWhite && (from / 8 == 3) && Math.abs(enPassantSquare % 8 - from % 8) == 1) {
                    possibleMoves.add(new Move(from, enPassantSquare, 0, enPassantSquare + 8, true, -1));
                }
                if (!isWhite && (from / 8 == 4) && Math.abs(enPassantSquare % 8 - from % 8) == 1) {
                    possibleMoves.add(new Move(from, enPassantSquare, 6, enPassantSquare - 8, true, -1));
                }
            }

            // Capture
            long possible = isWhite ? AttacksGen.whitePawnAttacks[from] : AttacksGen.blackPawnAttacks[from];
            possible &= opponentOccupancy;

            while (possible != 0) {
                int to = Long.numberOfTrailingZeros(possible);
                if (isWhite) {
                    if (to / 8 == 0)
                        for (int i = 1; i < 5; i++)
                            possibleMoves.add(new Move(from, to, currPiece, to, false, i));
                    else
                        possibleMoves.add(new Move(from, to, currPiece, to, false, -1));
                } else {
                    if (to / 8 == 7)
                        for (int i = 7; i < 11; i++)
                            possibleMoves.add(new Move(from, to, currPiece, to, false, i));
                    else
                        possibleMoves.add(new Move(from, to, currPiece, to, false, -1));
                }
                // possibleMoves.add(new Move(from, to, currPiece));
                possible &= possible - 1;
            }
            pawns &= pawns - 1;
        }

        return possibleMoves;
    }

    private static long wSinglePushTargets(Bitboard bitboard) {
        return (bitboard.pieces[0] >>> 8) & bitboard.emptyOccupancy;
    }

    private static long wDblPushTargets(Bitboard bitboard) {
        long singlePushs = wSinglePushTargets(bitboard);
        return (singlePushs >>> 8) & bitboard.emptyOccupancy & RANK_4;
    }

    private static long bSinglePushTargets(Bitboard bitboard) {
        return (bitboard.pieces[6] << 8) & bitboard.emptyOccupancy;
    }

    private static long bDblPushTargets(Bitboard bitboard) {
        long singlePushs = bSinglePushTargets(bitboard);
        return (singlePushs << 8) & bitboard.emptyOccupancy & RANK_5;
    }

    private static ArrayList<Move> generateKnightMoves(Bitboard bitboard, boolean isWhite) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int currPiece = isWhite ? 1 : 7;
        long knights = bitboard.pieces[currPiece];
        long ownOccupancy = isWhite ? bitboard.whiteOccupancy : bitboard.blackOccupancy;

        // Get the leading 1s in the knights biboard
        while (knights != 0) {
            int from = Long.numberOfTrailingZeros(knights);
            long possible = AttacksGen.knightAttacks[from] & ~ownOccupancy;

            while (possible != 0) {
                int to = Long.numberOfTrailingZeros(possible);
                int capturedSquare = bitboard.isOccuppied(to) ? to : -1;
                possibleMoves.add(new Move(from, to, currPiece, capturedSquare, false, -1));

                possible &= possible - 1;
            }

            knights &= knights - 1;
        }

        return possibleMoves;
    }

    private static ArrayList<Move> generateBishopMoves(Bitboard bitboard, boolean isWhite) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int currPiece = isWhite ? 2 : 8;
        long bishops = bitboard.pieces[currPiece];
        long ownOccupancy = isWhite ? bitboard.whiteOccupancy : bitboard.blackOccupancy;

        while (bishops != 0) {
            int from = Long.numberOfTrailingZeros(bishops);

            long mask = MagicBitboards.bishopRelevantOccupancy[from];
            long blockers = bitboard.occupancy & mask;
            // Compute index for precomputed attack table:
            // https://www.chessprogramming.org/Magic_Bitboards
            int index = (int) ((blockers * MagicBitboards.bishopMagic[from]) >>> (64
                    - Long.bitCount(MagicBitboards.bishopRelevantOccupancy[from])));
            long attacks = MagicBitboards.bishopAttacks[from][index];
            // Long attacks = MagicBitboards.computeBishopAttacks(from, blockers);

            long possible = attacks & ~ownOccupancy;

            while (possible != 0) {
                int to = Long.numberOfTrailingZeros(possible);
                int capturedSquare = bitboard.isOccuppied(to) ? to : -1;
                possibleMoves.add(new Move(from, to, currPiece, capturedSquare, false, -1));
                possible &= possible - 1;
            }
            bishops &= bishops - 1;
        }

        return possibleMoves;
    }

    private static ArrayList<Move> generateRookMoves(Bitboard bitboard, boolean isWhite) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int currPiece = isWhite ? 3 : 9;
        long rooks = bitboard.pieces[currPiece];
        long ownOccupancy = isWhite ? bitboard.whiteOccupancy : bitboard.blackOccupancy;

        while (rooks != 0) {
            int from = Long.numberOfTrailingZeros(rooks);

            long mask = MagicBitboards.rookRelevantOccupancy[from];
            long blockers = bitboard.occupancy & mask;

            // Compute index for precomputed attack table:
            // https://www.chessprogramming.org/Magic_Bitboards
            int relevantBits = Long.bitCount(MagicBitboards.rookRelevantOccupancy[from]);
            int index = (int) ((blockers * MagicBitboards.rookMagic[from]) >>> (64
                    - relevantBits));
            long attacks = MagicBitboards.rookAttacks[from][index];
            // long attacks = MagicBitboards.computeRookAttacks(from, blockers);

            long possible = attacks & ~ownOccupancy;

            while (possible != 0) {
                int to = Long.numberOfTrailingZeros(possible);
                int capturedSquare = bitboard.isOccuppied(to) ? to : -1;
                possibleMoves.add(new Move(from, to, currPiece, capturedSquare, false, -1));
                possible &= possible - 1;
            }
            rooks &= rooks - 1;
        }

        return possibleMoves;
    }

    private static ArrayList<Move> generateQueenMoves(Bitboard bitboard, boolean isWhite) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int currPiece = isWhite ? 4 : 10;
        long queens = bitboard.pieces[currPiece];
        long ownOccupancy = isWhite ? bitboard.whiteOccupancy : bitboard.blackOccupancy;

        while (queens != 0) {
            int from = Long.numberOfTrailingZeros(queens);

            long rookBlockers = bitboard.occupancy & MagicBitboards.rookRelevantOccupancy[from];
            long bishopBlockers = bitboard.occupancy & MagicBitboards.bishopRelevantOccupancy[from];
            // long blockers = rookBlockers | bishopBlockers;
            // Compute index for precomputed attack table:
            // https://www.chessprogramming.org/Magic_Bitboards
            // int index = (int) ((blockers * MagicBitboards.queenMagic[from]) >>> (64
            // - Long.bitCount(MagicBitboards.queenRelevantOccupancy[from])));
            // long attacks = MagicBitboards.queenAttacks[from][index];

            int relevantBishopBits = Long.bitCount(MagicBitboards.bishopRelevantOccupancy[from]);
            int bishopIndex = (int) ((bishopBlockers * MagicBitboards.bishopMagic[from]) >>> (64
                    - relevantBishopBits));
            long bishopAttacks = MagicBitboards.bishopAttacks[from][bishopIndex];

            int relevantRookBits = Long.bitCount(MagicBitboards.rookRelevantOccupancy[from]);
            int index = (int) ((rookBlockers * MagicBitboards.rookMagic[from]) >>> (64
                    - relevantRookBits));
            long rookAttacks = MagicBitboards.rookAttacks[from][index];

            long attacks = bishopAttacks | rookAttacks;
            // long attacks = MagicBitboards.computeBishopAttacks(from, blockers)
            // | MagicBitboards.computeRookAttacks(from, blockers);
            long possible = attacks & ~ownOccupancy;

            while (possible != 0) {
                int to = Long.numberOfTrailingZeros(possible);
                int capturedSquare = bitboard.isOccuppied(to) ? to : -1;
                possibleMoves.add(new Move(from, to, currPiece, capturedSquare, false, -1));
                possible &= possible - 1;
            }
            queens &= queens - 1;
        }

        return possibleMoves;
    }

    private static ArrayList<Move> generateKingMoves(Bitboard bitboard, boolean isWhite) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int currPiece = isWhite ? 5 : 11;
        long kings = bitboard.pieces[currPiece];
        long ownOccupancy = isWhite ? bitboard.whiteOccupancy : bitboard.blackOccupancy;

        while (kings != 0) {
            int from = Long.numberOfTrailingZeros(kings);
            long possible = AttacksGen.kingAttacks[from] & ~ownOccupancy;

            while (possible != 0) {
                int to = Long.numberOfTrailingZeros(possible);
                int capturedSquare = bitboard.isOccuppied(to) ? to : -1;
                possibleMoves.add(new Move(from, to, currPiece, capturedSquare, false, -1));

                possible &= possible - 1;
            }

            kings &= kings - 1;
        }

        return possibleMoves;
    }

    private static ArrayList<Move> generateCastlingMoves(Bitboard bitboard, boolean isWhite) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        boolean canShortCastle = isWhite ? bitboard.canShortCastleWhite : bitboard.canShortCastleBlack;
        boolean canLongCastle = isWhite ? bitboard.canLongCastleWhite : bitboard.canLongCastleBlack;

        long occupancy = bitboard.occupancy;

        if (canShortCastle) {
            boolean empty = isWhite
                    ? ((occupancy & ((1L << 61) | (1L << 62))) == 0)
                    : ((occupancy & ((1L << 5) | (1L << 6))) == 0);
            boolean safe = !bitboard.isKingInCheck(isWhite)
                    && !bitboard.isKingInCheck(isWhite, isWhite ? 61 : 5)
                    && !bitboard.isKingInCheck(isWhite, isWhite ? 62 : 6);
            if (empty && safe) {
                possibleMoves.add(new Move("0-0", isWhite));
            }
        }
        if (canLongCastle) {
            boolean empty = isWhite
                    ? ((occupancy & ((1L << 59) | (1L << 58) | (1L << 57))) == 0)
                    : ((occupancy & ((1L << 3) | (1L << 2) | (1L << 1))) == 0);
            boolean safe = !bitboard.isKingInCheck(isWhite)
                    && !bitboard.isKingInCheck(isWhite, isWhite ? 59 : 3)
                    && !bitboard.isKingInCheck(isWhite, isWhite ? 58 : 2);
            if (empty && safe) {
                possibleMoves.add(new Move("0-0-0", isWhite));
            }
        }
        return possibleMoves;
    }
}
