package application;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FileDownloader {

    String stringUrl;
    String fileName;
    List<String> fileNames;
    boolean supportForRange = false;
    List<Thread> downloaderThreads;
    List<Runnable> runnables;
    DownloadingCallback callback;
    int fileSize = 0;

    public FileDownloader(String stringUrl, DownloadingCallback callback) {
        this.stringUrl = stringUrl;
        this.callback = callback;
        fileNames = new ArrayList<>();
        downloaderThreads = new ArrayList<>();
        runnables = new ArrayList<>();
    }

    public String getFileName(){
        return fileName;
    }
    public int getFileSize(){
        return fileSize;
    }
    public List<String> retrieveFileNames(){
        return fileNames;
    }
    public boolean receiveDownloadData(){
        java.net.URL url;
        URLConnection conn;
        int size;
        try {
            url = new URL(stringUrl);
            conn = url.openConnection();
            supportForRange = conn.getHeaderField("Accept-Ranges").equals("bytes");
            size = conn.getContentLength();
            fileName = stringUrl.substring( stringUrl.lastIndexOf('/')+1);
            if(size < 0)
                System.out.println("Could not determine file size.");
            else{
                fileSize = size;
                System.out.println(stringUrl + "\nSize: " + size);
            }
            conn.getInputStream().close();
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    public void startDownloading() {
        downloaderThreads = new ArrayList<>();
        runnables = new ArrayList<>();
        if (supportForRange) {
            fileNames = new ArrayList<>();
            callback.setDataChunks((int) Math.ceil(fileSize / (1024f * 1024f)));
            for (int i = 0; i < fileSize; i += 1024 * 1024) {
                int rangeStart = i;
                String tempFileName = fileName + (rangeStart / (1024 * 1024));
                fileNames.add(tempFileName);
                int j = i + ((1024 * 1024) - 1);
                if (j > fileSize) {
                    j = fileSize;
                }
                File tempFile = new File(tempFileName);
                if(tempFile.exists()){
                    rangeStart = rangeStart + (int)tempFile.length();
                }
                int fileBytesRead = rangeStart - i;
                final int finalRangeStart = rangeStart;
                final int rangeEnd = j;
                Runnable newRunnable = () -> {
                        try {

                            URL newUrl = new URL(stringUrl);
                            HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
                            connection.addRequestProperty("Range", "bytes=" + finalRangeStart + "-" + rangeEnd);
                            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                                 FileOutputStream fileOutputStream = new FileOutputStream(tempFileName,true)) {
                                byte dataBuffer[] = new byte[rangeEnd - finalRangeStart + 1];
                                int bytesRead;
                                int totalBytesRead = fileBytesRead;
                                while ((bytesRead = in.read(dataBuffer, 0, rangeEnd - finalRangeStart + 1)) != -1) {
                                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                                    if(Thread.currentThread().isInterrupted()){
                                        throw new IOException("Thread is interrupted");
                                    }
                                    totalBytesRead += bytesRead;
                                    callback.onDataReceived(finalRangeStart / (1024 * 1024), (long)totalBytesRead);
                                }
                                callback.onDataReceived(finalRangeStart / (1024 * 1024), (long)totalBytesRead);
                            } catch (IOException e) {
                                if(!e.getMessage().equals("Thread is interrupted")){
                                    if(e.getMessage().contains("416")){
                                        if(finalRangeStart >= fileSize){
                                            System.out.println("TRIGGERED");
                                            callback.onDownloadingComplete(true);
                                        }
                                    } else {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println(Thread.currentThread().isInterrupted());
                            if(!Thread.currentThread().isInterrupted()){
                                System.out.println("TRIGGERED");
                                callback.onDownloadingComplete(true);
                            }
                        }
                };
                runnables.add(newRunnable);
                Thread newThread = new Thread(newRunnable);

                downloaderThreads.add(newThread);
                newThread.start();
            }
        } else{
            try {
                URL newUrl = new URL(stringUrl);
                HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
                try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                    byte dataBuffer[] = new byte[fileSize];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, fileSize)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    // handle exception
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                callback.onDownloadingComplete(false);
            }
        }
    }
    public void stopDownloading(){
        for(int i=0;i<downloaderThreads.size(); i++){
            downloaderThreads.get(i).interrupt();
        }
        callback.onDownloadingCancelled();
    }
}
