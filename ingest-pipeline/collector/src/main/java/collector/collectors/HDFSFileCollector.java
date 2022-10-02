package collector.collectors;

import collector.config.CollectorConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.Arrays;

public class HDFSFileCollector extends AbstractFileCollector {

    public HDFSFileCollector(CollectorConfig collectorConfig) throws IOException {
        super(collectorConfig);
    }

    @Override
    public void collect() {
        Configuration configuration = new Configuration();

        try (final FileSystem hdfs = FileSystem.get(configuration)) {
            FileStatus[] statuses = hdfs.listStatus(new Path(config.getNormalizedSrcDir()));
            Arrays.stream(statuses)
                    .filter(FileStatus::isFile)
                    .parallel()
                    .forEach(fileStatus -> pushToQueue(fileStatus.getPath().toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
