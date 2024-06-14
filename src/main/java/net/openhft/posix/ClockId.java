package net.openhft.posix;

/**
 * This enum represents the different clock IDs used in clock operations.
 * It defines the various clocks that can be used for timing and synchronization purposes.
 */
public enum ClockId {
    // The system-wide real-time clock
    CLOCK_REALTIME(0),

    // The monotonic clock, which cannot be set and represents monotonic time since some unspecified starting point
    CLOCK_MONOTONIC(1),

    // The clock measuring the CPU time consumed by the process
    CLOCK_PROCESS_CPUTIME_ID(2),

    // The clock measuring the CPU time consumed by the thread
    CLOCK_THREAD_CPUTIME_ID(3),

    // The raw monotonic clock, without NTP adjustments
    CLOCK_MONOTONIC_RAW(4),

    // The system-wide real-time clock, but faster and less accurate
    CLOCK_REALTIME_COARSE(5),

    // The coarse monotonic clock, but faster and less accurate
    CLOCK_MONOTONIC_COARSE(6),

    // The monotonic clock that includes time spent in suspend
    CLOCK_BOOTTIME(7),

    // The system-wide real-time clock used to set alarms
    CLOCK_REALTIME_ALARM(8),

    // The boot-time clock used to set alarms
    CLOCK_BOOTTIME_ALARM(9),

    // The SGI cycle counter
    CLOCK_SGI_CYCLE(10);

    // The integer value representing the clock ID
    private final int value;

    /**
     * Constructor for ClockId.
     *
     * @param value The integer value representing the clock ID
     */
    ClockId(int value) {
        this.value = value;
    }

    /**
     * This method is a getter for the value instance variable.
     * It returns the current integer value of this ClockId object.
     *
     * @return The current integer value of this ClockId object
     */
    public int value() {
        return value;
    }
}
