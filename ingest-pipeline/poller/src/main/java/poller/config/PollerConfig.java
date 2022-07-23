package poller.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PollerConfig {
    private String srcDirectory;
    private String destDirectory;
    private int batchSize;
    private int pollRate;
    private int portNumber;
    // Copies files and leaves originals instead of removing them after
    private boolean copyOnWrite;
    private Path normalizedSrcDir;
    private Path normalizedDestDir;
    private String srcURIScheme;
    private String destURIScheme;

    public PollerConfig() {}
    public PollerConfig(String src, String dest, int port, int poll) throws Exception {
        this(src, dest, 1, port, poll, false);
    }
    public PollerConfig(String src, String dest, int port, int poll, int batch) throws Exception {
        this(src, dest, batch, port, poll, false);
    }
    public PollerConfig(String src, String dest, int port, int poll, int batch, boolean copy) throws Exception {
        this.srcDirectory = src;
        this.destDirectory = dest;
        this.portNumber = port;
        this.pollRate = poll;
        this.batchSize = batch;
        this.copyOnWrite = copy;

        validateAndSplit(srcDirectory, true);
        validateAndSplit(destDirectory, false);
    }

    //Getters
    @JsonGetter("srcDirectory")
    public String getSrcDirectory() {
        return this.srcDirectory;
    }
    @JsonGetter("destDirectory")
    public String getDestDirectory() {
        return this.destDirectory;
    }
    @JsonGetter("batchSize")
    public int getBatchSize() {
        return this.batchSize;
    }
    @JsonGetter("copyOnWrite")
    public boolean isCopyOnWrite() {
        return this.copyOnWrite;
    }
    @JsonGetter("portNumber")
    public int getPortNumber() {
        return this.portNumber;
    }
    @JsonGetter("pollRate")
    public int getPollRate() {
        return this.pollRate;
    }
    public Path getNormalSrcDirectory() {
        return this.normalizedSrcDir;
    }
    public Path getNormalDestDirectory() {
        return this.normalizedDestDir;
    }

    // Setters
    @JsonSetter("srcDirectory")
    public void setSrcDirectory(String directory) throws Exception {
        this.srcDirectory = directory;
        validateAndSplit(srcDirectory, true);
    }
    @JsonSetter("destDirectory")
    public void setDestDirectory(String directory) throws Exception {
        this.destDirectory = directory;
        validateAndSplit(destDirectory, false);
    }
    @JsonSetter("batchSize")
    public void setBatchSize(int batch) {
        this.batchSize = batch;
    }
    @JsonSetter("copyOnWrite")
    public void setCopyOnWrite(boolean copy) {
        this.copyOnWrite = copy;
    }
    @JsonSetter("portNumber")
    public void setPortNumber(int port) {
        this.portNumber = port;
    }
    @JsonSetter("pollRate")
    public void setPollRate(int poll) {
        this.pollRate = poll;
    }
    protected void setNormalSrcDirectory(Path normal) {
        this.normalizedSrcDir = normal;
    }
    protected void setNormalSrcDirectory(String normal) {
        this.normalizedSrcDir = Paths.get(normal);
    }
    protected void setNormalDestDirectory(Path normal) {
        this.normalizedDestDir = normal;
    }
    protected void setNormalDestDirectory(String normal) {
        this.normalizedDestDir = Paths.get(normal);
    }
    public String getSrcURIScheme() {
        return this.srcURIScheme;
    }
    public String getDestURIScheme() {
        return this.destURIScheme;
    }

    protected void validateAndSplit(String path, boolean isSrc) throws Exception {
        if (!path.equals("")) {
            if (path.contains("://")) {
                String[] splitString = path.split("://", 2);
                if (isSrc) {
                    srcURIScheme = splitString[0];
                    normalizedSrcDir = Paths.get(splitString[1]);
                } else {
                    destURIScheme = splitString[0];
                    normalizedDestDir = Paths.get(splitString[1]);
                }
            }
            else {
                throw new Exception("Improperly formatted directory. Missing [scheme]://");
            }
        } else {
            throw new Exception("Invalid Configuration: Empty directory provided");
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n-----Poller Configuration-----\n");
        builder.append("Source Directory: " + srcDirectory + "\n");
        builder.append("Destination Directory: " + destDirectory + "\n");
        builder.append("Batch size: " + batchSize + "\n");
        builder.append("Poll rate: " + pollRate + "\n");
        builder.append("Copy On Write: " + copyOnWrite + "\n");
        builder.append("Port Number: " + portNumber + "\n");
        return builder.toString();
    }
}
