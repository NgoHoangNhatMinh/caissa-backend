package com.example.awesome_possum_bot_backend;

public class OtherUtils {
    public static void print(long board) {
        // String boardStr = "";
        for (int i = 0; i < 8; i++) {
            // boardStr += (8 - i) + " ";
            System.out.print((8 - i) + "   ");
            for (int j = 0; j < 8; j++) {
                int offset = i * 8 + j;
                // System.out.println(1L << offset);
                if ((board & (1L << offset)) != 0) {
                    // board += GRAPHIC[k] + " ";
                    System.out.print(1 + " ");
                } else {
                    System.out.print(0 + " ");
                }
            }
            System.out.println();
            // board += "\n";
        }
        System.out.println("\n    a b c d e f g h ");
        System.out.println("\n    Long: " + Long.toUnsignedString(board) + "L\n");
        // return board;
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
