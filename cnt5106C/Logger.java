package cnt5106C;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Logger {
    private String peerId;
    protected final String logFile;

    public Logger(String args){
        this.peerId = args;
        this.logFile = "log_peer_"+args+".log";
    }

    public void log(String s) throws IOException {
        log(logFile, s);
    }

    public void log(String f, String s) throws IOException {
        TimeZone EST = TimeZone.getTimeZone("GMT-4:00");
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss ");
        format.setTimeZone(EST);
        String currentTime = format.format(date);
        FileWriter aWriter = new FileWriter(f, true);
        aWriter.write(currentTime + ": Peer " + peerId + " " +s + ".\n");
        aWriter.flush();
        aWriter.close();
    }
}
