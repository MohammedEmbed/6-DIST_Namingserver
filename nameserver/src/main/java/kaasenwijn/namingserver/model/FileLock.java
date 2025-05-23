package kaasenwijn.namingserver.model;

import java.time.Instant;
import java.util.Objects;

public class FileLock {

    private String fileName;
    private int nodeId;
    private Instant expiresAt;

    public FileLock() {}

    public FileLock(String fileName, int nodeId, Instant expiresAt) {
        this.fileName = fileName;
        this.nodeId = nodeId;
        this.expiresAt = expiresAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileLock)) return false;
        FileLock fileLock = (FileLock) o;
        return nodeId == fileLock.nodeId &&
                Objects.equals(fileName, fileLock.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, nodeId);
    }

    @Override
    public String toString() {
        return "FileLock{" + "fileName='" + fileName + '\'' + ", nodeId=" + nodeId + ", expiresAt=" + expiresAt + '}';
    }
}
