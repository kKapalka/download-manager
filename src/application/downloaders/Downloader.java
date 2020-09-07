package application.downloaders;

import java.net.URL;

import application.DownloadingCallback;

/**
 * Abstract class, which handles downloading files
 */
public abstract class Downloader {

    URL url;
    String fileName;
    long fileSize;
    DownloadingCallback callback;

    public Downloader(DownloadedFileData data, DownloadingCallback callback) {
        this.url = data.getUrl();
        this.fileName = data.getFileName();
        this.fileSize = data.getFileSize();
        this.callback = callback;
    }

    public abstract void downloadFile();
    public abstract void stopDownloading(boolean isComplete);
}
