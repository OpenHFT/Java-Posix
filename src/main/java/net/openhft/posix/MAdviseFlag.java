package net.openhft.posix;

/**
 * This enum represents the different flags for memory advice (madvise) operations.
 * It defines the advice to be given to the kernel about the intended usage pattern of memory.
 */
public enum MAdviseFlag {
    // No further special treatment
    MADV_NORMAL(0),

    // Expect random page references
    MADV_RANDOM(1),

    // Expect sequential page references
    MADV_SEQUENTIAL(2),

    // Will need these pages
    MADV_WILLNEED(3),

    // Don't need these pages
    MADV_DONTNEED(4),

    // Free pages only if memory pressure
    MADV_FREE(8),

    // Remove these pages and resources
    MADV_REMOVE(9),

    // Do not inherit across fork
    MADV_DONTFORK(10),

    // Do inherit across fork
    MADV_DOFORK(11),

    // KSM may merge identical pages
    MADV_MERGEABLE(12),

    // KSM may not merge identical pages
    MADV_UNMERGEABLE(13),

    // Worth backing with hugepages
    MADV_HUGEPAGE(14),

    // Not worth backing with hugepages
    MADV_NOHUGEPAGE(15),

    // Explicitly exclude from the core dump, overrides the coredump filter bits
    MADV_DONTDUMP(16),

    // Clear the MADV_DONTDUMP flag
    MADV_DODUMP(17),

    // Zero memory on fork, child only
    MADV_WIPEONFORK(18),

    // Undo MADV_WIPEONFORK
    MADV_KEEPONFORK(19);

    // The integer value representing the madvise flag
    final int value;

    /**
     * Constructor for MAdviseFlag.
     *
     * @param value The integer value representing the madvise flag
     */
    MAdviseFlag(int value) {
        this.value = value;
    }

    /**
     * This method is a getter for the value instance variable.
     * It returns the current integer value of this MAdviseFlag object.
     *
     * @return The current integer value of this MAdviseFlag object
     */
    public int value() {
        return value;
    }
}
