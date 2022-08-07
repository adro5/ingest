package poller.provider;

import poller.collector.AbstractCollector;
import poller.config.PollerConfig;

import java.nio.file.Path;

public class S3FileProvider implements FileProvider{

    public S3FileProvider(PollerConfig config, AbstractCollector<Path> collector) {

    }
    @Override
    public boolean move() {
        return false;
    }

    /* At some point, cleanup() may become a part of the FileProvider interface.
       This may happen if the LFP gains support for its own collector.
     */
    private void cleanup() {

    }
}
