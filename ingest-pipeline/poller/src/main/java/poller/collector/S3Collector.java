package poller.collector;

import poller.config.PollerConfig;

import java.util.Comparator;

public class S3Collector<E> extends AbstractCollector<E>{

    public S3Collector(Comparator<E> comparator, PollerConfig config) {
        super(comparator, config);
    }

    @Override
    public void collect() {

    }
}
