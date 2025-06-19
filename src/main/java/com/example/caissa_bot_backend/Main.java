package com.example.caissa_bot_backend;

class Main {
    public static void main(String... args) {
        Board board = new Board();
        // board.init();
        board.init();

        if (args.length > 0) {
            try {
                board.isWhiteBot = args[0].toLowerCase() == "white";
                board.isBlackBot = !board.isWhiteBot;
            } catch (NumberFormatException e) {
                System.out.println("Invalid color argument, using default no engine.");
            }
        }
        board.run();
    }
}