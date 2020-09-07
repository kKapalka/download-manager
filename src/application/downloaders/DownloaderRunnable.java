package application.downloaders;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Runnable designed to download data from external source, split into chunks
 */
public class DownloaderRunnable implements Runnable {

    URL url;
    String fileName;
    long rangeStart, rangeEnd;
    int chunkId;
    DownloaderRunnableCallback callback;
    long totalBytesRead = 0;
    long currentBytesRead = 0;
    Timer timer;


    public DownloaderRunnable(URL url, String fileName, long rangeStart, long rangeEnd,
          DownloaderRunnableCallback callback, int chunkId) {
        this.url = url;
        this.fileName = fileName;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.callback = callback;
        this.chunkId = chunkId;
        timer = new Timer();
    }

    @Override
    public void run() {
        try {
            currentBytesRead = 0;
            TimerTask newTimerTask = new TimerTask() {
                @Override
                public void run() {
                    callback.reportOnBytesRead(chunkId, totalBytesRead,fileName);
                }
            };
            timer.schedule(newTimerTask,1000,1000);
            File tempFile = new File(fileName);
            if(tempFile.exists()){
                rangeStart = rangeStart + tempFile.length();
                totalBytesRead = tempFile.length();
                callback.reportOnBytesRead(chunkId, totalBytesRead,fileName);
            }
            if(rangeStart < rangeEnd) {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("Range", "bytes=" + rangeStart + "-" + rangeEnd);
                try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(fileName, true)) {
                    byte dataBuffer[] = new byte[(int) (rangeEnd - rangeStart) + 1];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, (int) (rangeEnd - rangeStart) + 1)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        if (Thread.currentThread().isInterrupted()) {
                            throw new IOException("Thread is interrupted");
                        }
                        currentBytesRead += bytesRead;
                        totalBytesRead += bytesRead;
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }finally {
                    timer.cancel();
                    callback.reportOnBytesRead(chunkId, totalBytesRead,fileName);
                    if(rangeStart + currentBytesRead >= rangeEnd) {
                        callback.onDownloaderComplete(Thread.currentThread());
                    }
                }
            } else{
                timer.cancel();
                callback.onDownloaderComplete(Thread.currentThread());
            }
        } catch (FileNotFoundException e){
            System.out.println("FileNotFound");
            e.printStackTrace();
        } catch (IOException e){
            System.out.println("IOException");
            e.printStackTrace();
            timer.cancel();
            callback.onDownloaderComplete(Thread.currentThread());
        }
    }
}
