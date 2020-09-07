package application;

public interface DownloadingCallback {
    void onDataReceived(int chunkId, Long totalBytesRead);
    void onDownloadingComplete(boolean isFileChunked);
    void onDownloadingCancelled();
    void onDownloadError();

    void setDataChunks(int dataChunksCount);
}
