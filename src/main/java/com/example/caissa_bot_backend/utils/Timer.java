package com.example.caissa_bot_backend.utils;

public class Timer {
    long total = 0;
    long start = 0;

    public void start() {
        start = System.nanoTime();
    }

    public void stop() {
        total += System.nanoTime() - start;
    }

    public void print(String label) {
        System.out.println(label + ": " + total / 1_000_000 + " ms");
    }
}
