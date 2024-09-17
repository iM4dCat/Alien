package dev.luminous.api.utils.math;

public class Timer {

    public Timer() {
        reset();
    }
    private long time = -1L;

    public Timer reset() {
        time = System.nanoTime();
        return this;
    }

    public boolean tick(int tick) {
        return passedMs(tick * 50L);
    }
    public boolean passedTicks(int tick) {
        return passedMs(tick * 50L);
    }
    public boolean passedS(double s) {
        return passedMs(s * 1000);
    }

    public boolean passedMs(long ms) {
        return passedNS(convertToNS(ms));
    }

    public boolean passedMs(double ms) {
        return passedMs((long) ms);
    }
    public boolean passed(long ms) {
        return passedMs(ms);
    }
    public boolean passed(double ms) {
        return passedMs((long) ms);
    }
    public void setMs(long ms) {
        time = System.nanoTime() - convertToNS(ms);
    }

    public boolean passedNS(long ns) {
        return System.nanoTime() - time >= ns;
    }

    public long getPassedTimeMs() {
        return getMs(System.nanoTime() - time);
    }

    public long getMs(long time) {
        return time / 1000000L;
    }

    public long convertToNS(long time) {
        return time * 1000000L;
    }

}
