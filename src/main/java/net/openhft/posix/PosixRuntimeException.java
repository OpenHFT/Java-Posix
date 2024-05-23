package net.openhft.posix;

public class PosixRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    public PosixRuntimeException(String message) {
        super(message);
    }

    public PosixRuntimeException(Throwable cause) {
        super(cause);
    }
}
