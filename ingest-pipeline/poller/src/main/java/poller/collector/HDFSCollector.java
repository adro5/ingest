package poller.collector;

import poller.config.PollerConfig;

import java.util.Comparator;

public class HDFSCollector<E> extends AbstractCollector<E> {

    public HDFSCollector(Comparator<E> comparator, PollerConfig pollerConfig) {
        super(comparator, pollerConfig);
    }

    @Override
    public void collect() {

    }
}
