package de.happybavarian07.adminpanel.utils;

public class TPSMeter {
    public int TICK_COUNT = 0;
    public long[] TICKS = new long[600];
    public long LAST_TICK = 0L;

    public double getTPS() {
        return getTPS(100);
    }

    public double getTPS(int ticks) {
        if (TICK_COUNT < ticks) {
            return 20.0D;
        }
        int target = (TICK_COUNT - 1 - ticks) % TICKS.length;
        long elapsed = System.currentTimeMillis() - TICKS[target];

        return ticks / (elapsed / 1000.0D);
    }

    public long getElapsed(int tickID) {
        long time = TICKS[(tickID % TICKS.length)];
        return System.currentTimeMillis() - time;
    }

    public void run() {
        TICKS[(TICK_COUNT % TICKS.length)] = System.currentTimeMillis();

        TICK_COUNT += 1;
    }
}