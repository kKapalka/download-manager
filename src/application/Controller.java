package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements DownloadingCallback {
    public TextField URLinput;
    public Button downloadButton;
    public Text infoText;
    public AnchorPane scrollPanelContainer;
    public TextArea textArea;
    public ProgressBar progressBar;
    List<Long> totalDownloadedBytes;
    Long lastRegisteredBytesRead = 0L;
    Long previouslyRegisteredBytesRead = 0L;
    Timer timer;

    ExternalSourceConnector conn;
    Pattern p = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    Matcher m;

    public void onDownloadButtonClick() {
        lastRegisteredBytesRead = 0L;
        previouslyRegisteredBytesRead = 0L;
        totalDownloadedBytes = new ArrayList<>();
        m = p.matcher(URLinput.getText());
        boolean matches = m.matches();
        if(!matches){
            error("Please input a valid URL");
        } else{
            message("Connecting to external source...");
            try {
                conn = new ExternalSourceConnector(URLinput.getText(), this);
                conn.connectToExternalSource();
                conn.beginDownloading();
                message("Connection established. Downloading file...");
            } catch (MalformedURLException e) {
                error("Error forming URL");
                e.printStackTrace();
            } catch (FileSizeException e) {
                error("Couldn't determine file size");
                e.printStackTrace();
            } catch (IOException e) {
                error("Couldn't connect to external source");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void initialize() {
        URLinput.textProperty().addListener((observableValue, s, t1) -> {
            m = p.matcher(t1);
            if(!m.matches()){
                error("");
            } else{
                message("");
            }
        });
    }

    @Override
    public void onDataReceived(int chunkId, Long totalBytesRead) {
        totalDownloadedBytes.set(chunkId,totalBytesRead);
        lastRegisteredBytesRead = totalDownloadedBytes.stream().mapToLong(Long::longValue).sum();
        double progress =
              (double) lastRegisteredBytesRead / (double)conn.getFileSize();
        progressBar.setProgress(progress);
    }

    @Override
    public void onDownloadingComplete(boolean isFileChunked) {
        message("Downloading complete!");
        timer.cancel();
    }

    public void onCancelButtonClick(){
        timer.cancel();
        conn.stopDownloading(false);
    }

    @Override
    public void onDownloadingCancelled() {
        message("Downloading cancelled!");
    }

    @Override
    public void onDownloadError() {

    }

    @Override
    public void setDataChunks(int dataChunksCount){
        totalDownloadedBytes = new ArrayList<>();
        for(int i=0;i<dataChunksCount;i++){
            totalDownloadedBytes.add(0L);
        }
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Double megabytesPerSecond =
                      (double)(lastRegisteredBytesRead - previouslyRegisteredBytesRead) / (double)(1024 * 1024);
                System.out.println(BigDecimal.valueOf(megabytesPerSecond).setScale(3, RoundingMode.HALF_UP));
                previouslyRegisteredBytesRead = lastRegisteredBytesRead;
                if(lastRegisteredBytesRead == conn.getFileSize()){
                    conn.stopDownloading(true);
                }
            }
        };
        timer.schedule(task,1000,1000);
    }

    private void error(String errorMessage){
        URLinput.setStyle("-fx-text-inner-color: red;");
        infoText.setText(errorMessage);
        infoText.setFill(Color.RED);
    }
    private void message(String message){
        URLinput.setStyle("-fx-text-inner-color: black;");
        infoText.setText(message);
        infoText.setFill(Color.BLACK);
    }
}
