package application.downloaders;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import application.DownloadingCallback;

/**
 * Downloader class implementation, which handles singlethreaded file downloading
 */
public class SingleThreadedDownloader extends Downloader {

    boolean cancelled = false;
    Long totalBytesRead;

    public SingleThreadedDownloader(DownloadedFileData data, DownloadingCallback callback) {
        super(data, callback);
    }

    @Override
    public void downloadFile() {
        totalBytesRead = 0L;
        cancelled = false;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                byte dataBuffer[] = new byte[(int) fileSize];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, (int) fileSize)) != -1) {
                    if(cancelled){
                        throw new IOException("Downloading cancelled");
                    }
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                    totalBytesRead+=bytesRead;
                    System.out.println(totalBytesRead);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFound");
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

    @Override
    public void stopDownloading(boolean isComplete) {
        cancelled = !isComplete;
    }
}
