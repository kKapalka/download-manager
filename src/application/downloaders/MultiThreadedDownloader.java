package application.downloaders;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import application.DownloadingCallback;

/**
 * Downloader class implementation, which handles multithreaded file downloading
 */
public class MultiThreadedDownloader extends Downloader implements DownloaderRunnableCallback{

    private final long STANDARD_CHUNK_SIZE = 1024 * 1024;
    List<Thread> downloaderThreadList;
    boolean canConcatenateChunks;

    public MultiThreadedDownloader(DownloadedFileData data, DownloadingCallback callback) {
        super(data, callback);
        downloaderThreadList = new ArrayList<>();
    }

    @Override
    public void downloadFile() {
        callback.setDataChunks((int)(fileSize / STANDARD_CHUNK_SIZE)+1);
        canConcatenateChunks = false;
        for(int i=0;i<fileSize;i+=STANDARD_CHUNK_SIZE){
            long rangeEnd = i+STANDARD_CHUNK_SIZE-1;
            if(rangeEnd > fileSize){
                rangeEnd = fileSize;
            }
            Runnable runnable = new DownloaderRunnable(url,fileName+(i/STANDARD_CHUNK_SIZE),i,rangeEnd,this,
                  (int)(i/STANDARD_CHUNK_SIZE));
            Thread thread = new Thread(runnable);
            System.out.println("in range: "+(rangeEnd > fileSize));
            downloaderThreadList.add(thread);
            if(i+STANDARD_CHUNK_SIZE > fileSize){
                canConcatenateChunks = true;
            }
            thread.start();
        }
    }

    @Override
    public void stopDownloading(boolean isComplete) {
        canConcatenateChunks = isComplete;
        for(int i = 0;i<downloaderThreadList.size();i++){
            onDownloaderComplete(downloaderThreadList.get(downloaderThreadList.size()-(1+i)));
        }
        downloaderThreadList.forEach(this::onDownloaderComplete);
        if(!isComplete){
            callback.onDownloadingCancelled();
        }
    }

    @Override
    public void onDownloaderComplete(Thread thread) {
        thread.interrupt();
        downloaderThreadList.remove(thread);
        if(downloaderThreadList.size() == 0 && canConcatenateChunks){
            try {
                OutputStream out = new FileOutputStream(fileName);
                byte[] buf = new byte[(int)fileSize];
                for(int i=0;i<fileSize;i+=STANDARD_CHUNK_SIZE){
                    InputStream in = new FileInputStream(fileName+(i/STANDARD_CHUNK_SIZE));
                    int b = 0;
                    while ((b = in.read(buf)) >= 0)
                        out.write(buf, 0, b);
                    in.close();
                    new File(fileName+(i/STANDARD_CHUNK_SIZE)).delete();
                }
                out.close();
                callback.onDownloadingComplete(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reportOnBytesRead(int chunkId, Long bytesRead, String name) {
        callback.onDataReceived(chunkId,bytesRead);
    }
}
