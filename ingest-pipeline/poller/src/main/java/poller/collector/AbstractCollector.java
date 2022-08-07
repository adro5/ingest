package poller.collector;

import poller.config.PollerConfig;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class AbstractCollector<E> {
    private ConcurrentSkipListMap<E, Boolean> workingDataSink;
    private final PollerConfig config;

    public AbstractCollector(Comparator<E> comparator, PollerConfig pollerConfig) {
        this.workingDataSink = new ConcurrentSkipListMap<>(comparator);
        this.config = pollerConfig;
    }

    protected void add(E element, boolean value) {
        workingDataSink.putIfAbsent(element, value);
    }
    protected void add(Map<E, Boolean> elements) {
        workingDataSink.putAll(elements);
    }
    public void remove(E element) {
        workingDataSink.remove(element);
    }
    // Will return a single unmarked file and mark it as in progress
    public E poll() {
        for (E item : workingDataSink.descendingKeySet()) {
            if (!workingDataSink.get(item)) {
                workingDataSink.put(item, true);
                return item;
            }
        }
        return null;
    }
    // Will read batch size from config and return a list of files to process and mark them as in progress
    public List<E> pollList() {
        List<E> collect = new ArrayList<>();
        for (E item : workingDataSink.descendingKeySet()) {
            if (!workingDataSink.get(item)) {
                if (collect.size() < config.getBatchSize()) {
                    workingDataSink.put(item, true);
                    collect.add(item);
                } else { break; }
            }
        }
        return collect;
    }
    // Will update skip list with directory listings
    public abstract void collect();
}
