package fr.ght1pc9kc.baywatch.domain.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public final class Mutex {
    private static class Sync extends AbstractQueuedSynchronizer {
        Sync() { releaseShared(1); }
        boolean isSignalled() { return getState() != 0; }

        protected int tryAcquireShared(int ignore) {
            return compareAndSetState(1, 0)? 1 : -1;
        }

        protected boolean tryReleaseShared(int ignore) {
            setState(1);
            return true;
        }
    }

    private final Sync sync = new Sync();
    public boolean isLocked() { return !sync.isSignalled(); }
    public void unlock()         { sync.releaseShared(1); }
    public void lock()          { sync.acquireShared(1); }
    public boolean await(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }
}
