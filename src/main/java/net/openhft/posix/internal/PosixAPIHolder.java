package net.openhft.posix.internal;

import jnr.ffi.Platform;
import net.openhft.posix.PosixAPI;
import net.openhft.posix.internal.jnr.JNRPosixAPI;
import net.openhft.posix.internal.jnr.WinJNRPosixAPI;
import net.openhft.posix.internal.noop.NoOpPosixAPI;

/**
 * This class holds the instance of the {@link PosixAPI} to be used.
 * It loads the appropriate PosixAPI implementation based on the native platform.
 */
public class PosixAPIHolder {
    // The PosixAPI instance to be used
    public static PosixAPI POSIX_API;

    private PosixAPIHolder() {
        // utility
    }

    /**
     * Loads the appropriate PosixAPI implementation based on the native platform.
     * If the platform is Unix, it loads {@link JNRPosixAPI}, otherwise it loads {@link WinJNRPosixAPI}.
     * If an error occurs during loading, it falls back to {@link NoOpPosixAPI}.
     */
    public static void loadPosixApi() {
        if (POSIX_API != null)
            return;

        PosixAPI posixAPI;
        try {
            // Check if the native platform is Unix and load the appropriate API
            posixAPI = Platform.getNativePlatform().isUnix()
                    ? new JNRPosixAPI()
                    : new WinJNRPosixAPI();
        } catch (Throwable t) {
            // Fallback to NoOpPosixAPI if an error occurs
            posixAPI = new NoOpPosixAPI(t.toString());
        }
        POSIX_API = posixAPI;
    }

    /**
     * Sets the PosixAPI to a no-op implementation explicitly.
     */
    public static void useNoOpPosixApi() {
        POSIX_API = new NoOpPosixAPI("Explicitly disabled");
    }
}
