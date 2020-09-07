# Pro/g/ramming Challenge 1: Download Manager (Java)

Application created with JavaFX, created to make downloading files from the Internets faster

# Changelog

### 22.07.2019
* Fixed most of the bugs connected to resuming downloading with chunks already complete (still happens on some occasions)
* Hooked up interface DownloadingCallback, and now - once again - downloadManager displays appropriate message on successful download, and properly cancels downloading threads
* Progress bar is finally working!
* Still needs a bit of work tho (display of speed, single-threaded downloaders, test for big sized files)

### 21.07.2019
* Swapped Downloader class with ExternalSourceConnector class and applied it for purposes of download manager.
* Working on bugs which happen when resuming downloading with chunks already complete / when connection is abruptly stopped via other means

### 10.07.2019
* Started applying SOLID principles and design patterns. This resulted in splitting FileDownloader class into 6 classes, 1 abstract class, 1 interface and 1 exception. Now, the downloading process is much more manageable.
* These aforementioned classes still need to be appended to mother class, and implement callbacks to it in order to display all changes
* Added short descriptions to each new class 

### 27.06.2019
* Added downloading files: files are split into 1MB chunks, which are then concatenated
* Added message on downloading success!
* Moved test file name to the input field, so application can be tested immediately
* Added support for chunked and unchunked files (unchunked happens where site doesn't support range values)
* Downloading button disables when downloading is in progress
* Added chunk tracking
* Added download cancellation & started work on resuming (problems with registering which chunks are finished & with proper chunk removal)

### 25.06.2019

* Added FXML design
* Added validation for URL and message, whenever input is invalid
* On pressing "Download your file" button application first checks whether link points to file. Then, if it is, logs its file size and if the server supports partial downloading