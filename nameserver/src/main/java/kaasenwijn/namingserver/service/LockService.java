package kaasenwijn.namingserver.service;

import kaasenwijn.namingserver.model.FileLock;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LockService {

    private static final long LEASE_DURATION_MS = 60_000; // 60 seconds

    private final Map<String, FileLock> locks = new ConcurrentHashMap<>();

    public boolean acquireLock(FileLock request) {
        synchronized (locks) {
            FileLock currentLock = locks.get(request.getFileName());

            if (currentLock == null || isExpired(currentLock)) {
                request.setExpiresAt(Instant.now().plusMillis(LEASE_DURATION_MS));
                locks.put(request.getFileName(), request);
                return true;
            } else if (currentLock.getNodeId() == request.getNodeId()) {
                // Re-entrant lock
                currentLock.setExpiresAt(Instant.now().plusMillis(LEASE_DURATION_MS));
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean releaseLock(FileLock request) {
        synchronized (locks) {
            FileLock currentLock = locks.get(request.getFileName());
            if (currentLock != null && currentLock.getNodeId() == request.getNodeId()) {
                locks.remove(request.getFileName());
                return true;
            }
            return false;
        }
    }

    public boolean renewLock(FileLock request) {
        synchronized (locks) {
            FileLock currentLock = locks.get(request.getFileName());
            if (currentLock != null && currentLock.getNodeId() == request.getNodeId()) {
                currentLock.setExpiresAt(Instant.now().plusMillis(LEASE_DURATION_MS));
                return true;
            }
            return false;
        }
    }

    private boolean isExpired(FileLock lock) {
        return lock.getExpiresAt() == null || lock.getExpiresAt().isBefore(Instant.now());
    }

    public void releaseAllLocksByNode(int nodeId) {
        synchronized (locks) {
            locks.entrySet().removeIf(entry -> entry.getValue().getNodeId() == nodeId);
            System.out.printf("All locks released for the fallen node %d%n", nodeId);
        }
    }

}
