package collector.collectors;

import collector.config.CollectorConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class LocalFileCollector extends AbstractFileCollector{
    public LocalFileCollector(CollectorConfig collectorConfig) {
        super(collectorConfig);
    }

    @Override
    public void collect() {
        // Get all modifiable files in directory and push to queue after
        try (Stream<Path> stream = config.isRecursiveSearch() ? Files.walk(Paths.get(config.normalizedSrcDir)) : Files.list(Paths.get(config.normalizedSrcDir))) {
            stream.filter(file -> !Files.isDirectory(file) && Files.isReadable(file) && Files.isWritable(file))
                    .parallel()
                    .forEach(file -> pushToQueue(file.toString()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
