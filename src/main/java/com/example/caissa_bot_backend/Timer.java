package com.example.caissa_bot_backend;

public class Timer {
    long total = 0;
    long start = 0;

    void start() {
        start = System.nanoTime();
    }

    void stop() {
        total += System.nanoTime() - start;
    }

    void print(String label) {
        System.out.println(label + ": " + total / 1_000_000 + " ms");
    }
}
