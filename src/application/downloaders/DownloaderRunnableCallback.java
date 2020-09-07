package application.downloaders;

/**
 * Callback interface, used to handle download finalizing from every thread
 */
public interface DownloaderRunnableCallback {

    void onDownloaderComplete(Thread thread);
    void reportOnBytesRead(int chunkId, Long bytesRead, String name);
}
