package com.jef.justenoughfakepixel.features.uptime;

/**
 * Tracks the single countdown timer set by /jeftimer.
 * The timer counts DOWN from the user-selected duration.
 */
public class UptimeManager {

    private static UptimeManager instance;

    /** System.currentTimeMillis() when the timer will expire. -1 = no timer set. */
    private long endTimeMs = -1;
    /** Total duration the timer was started with (ms). Used for % progress. */
    private long totalDurationMs = 0;
    /** Whether the expiry toast has already been shown for this run. */
    private boolean fired = false;

    private UptimeManager() {}

    public static UptimeManager getInstance() {
        if (instance == null) instance = new UptimeManager();
        return instance;
    }

    /** Start (or restart) the countdown. durationMs must be > 0. */
    public void start(long durationMs) {
        this.totalDurationMs = durationMs;
        this.endTimeMs = System.currentTimeMillis() + durationMs;
        this.fired = false;
    }

    /** Cancel the running timer. */
    public void cancel() {
        this.endTimeMs = -1;
        this.fired = false;
    }

    public boolean isRunning() {
        return endTimeMs > 0;
    }

    public long getRemainingMs() {
        if (endTimeMs < 0) return 0;
        return Math.max(0, endTimeMs - System.currentTimeMillis());
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    /** Returns true the FIRST time this is called after the timer reaches zero. */
    public boolean pollExpired() {
        if (!isRunning()) return false;
        if (fired) return false;
        if (getRemainingMs() == 0) {
            fired = true;
            return true;
        }
        return false;
    }
}
