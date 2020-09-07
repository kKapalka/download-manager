package application.downloaders;

import application.DownloadingCallback;

/**
 * Factory class which creates appropriate Downloader classes depending on the sources' support for 'ranges' parameter
 */
public class DownloaderFactory {

    public static Downloader create (DownloadedFileData data, DownloadingCallback callback){
        if(data.isSupportForRanges()){
            return new MultiThreadedDownloader(data, callback);
        } else{
            return new SingleThreadedDownloader(data, callback);
        }
    }

}
