package collector.collectors;

import collector.config.CollectorConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public abstract class AbstractFileCollector {
    protected final CollectorConfig config;
    protected Producer<String, String> producer;

    public AbstractFileCollector(CollectorConfig collectorConfig) {
        this.config = collectorConfig;
        this.producer = new KafkaProducer<>(config.getProperties(), new StringSerializer(), new StringSerializer());
    }

    protected String qualifyPathName(String scheme, String filename) {
        return scheme + "://" + filename;
    }

    protected void pushToQueue(String file) {
        // Re-qualifying entire filename
        producer.send(new ProducerRecord<>("filesToCollect", qualifyPathName(config.srcURIScheme, file), config.getDestDirectory()));
        producer.close();
    }

    public abstract void collect();
}
