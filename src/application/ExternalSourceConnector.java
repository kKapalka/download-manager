package application;

import application.downloaders.DownloadedFileData;
import application.downloaders.Downloader;
import application.downloaders.DownloaderFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class designed to connect to external file source, and start downloading files from it
 */
public class ExternalSourceConnector {

    private boolean supportForDownloadRanges = false;
    private long fileSize = 0;
    private String fileName;
    private URL url;
    DownloadingCallback callback;
    Downloader downloader;
    public ExternalSourceConnector(String stringUrl, DownloadingCallback callback) throws MalformedURLException {
        this.url = new URL(stringUrl);
        this.fileName = stringUrl.substring( stringUrl.lastIndexOf('/')+1);
        this.callback = callback;
    }

    public void connectToExternalSource() throws IOException, FileSizeException{
        URLConnection conn = url.openConnection();
        System.out.println(conn.getHeaderFields());
        try {
            this.supportForDownloadRanges = conn.getHeaderField("Accept-Ranges").equals("bytes");
        } catch(NullPointerException e){
            this.supportForDownloadRanges = false;
        }
        this.fileSize = conn.getContentLength();
        if(this.fileSize < 0)
        {
            throw new FileSizeException();
        }
        conn.getInputStream().close();
    }

    public void beginDownloading(){
        downloader = DownloaderFactory.create(new DownloadedFileData(this.url,this.fileName,this.fileSize,
              this.supportForDownloadRanges), callback);
        downloader.downloadFile();
    }

    public void stopDownloading(boolean isComplete){
        downloader.stopDownloading(isComplete);
    }

    public long getFileSize() {
        return fileSize;
    }
}
