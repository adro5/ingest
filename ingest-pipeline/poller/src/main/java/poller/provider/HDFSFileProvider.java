package poller.provider;

import poller.collector.AbstractCollector;
import poller.config.PollerConfig;

import java.nio.file.Path;

public class HDFSFileProvider implements FileProvider {

    public HDFSFileProvider(PollerConfig config, AbstractCollector<Path> collector) {

    }
    @Override
    public boolean move() {
        return false;
    }

    private void cleanup() {

    }
}
