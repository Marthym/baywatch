package fr.ght1pc9kc.baywatch.opml.domain;

public final class OpmlExecption extends RuntimeException {
    public OpmlExecption(String message) {
        super(message);
    }

    public OpmlExecption(String message, Throwable cause) {
        super(message, cause);
    }
}
