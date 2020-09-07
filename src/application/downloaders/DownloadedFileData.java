package application.downloaders;

import java.net.URL;

/**
 * Data structure containing essential data from external file source
 */
public class DownloadedFileData {
    private URL url;
    private String fileName;
    private boolean supportForRanges;
    private long fileSize;

    public DownloadedFileData(URL url, String fileName, long fileSize, boolean supportForRanges) {
        this.url = url;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.supportForRanges = supportForRanges;
    }

    public URL getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isSupportForRanges() {
        return supportForRanges;
    }
}
