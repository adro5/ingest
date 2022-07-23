package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import poller.config.PollerConfig;
import poller.provider.LocalFileProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PollerTest {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    PollerConfig testConfig;
    private static final Logger log = LogManager.getLogger();
    @Test
    public void testConfigLoad() throws IOException {
        this.testConfig = mapper.readValue(new File("src/main/resources/test-config.yaml"), PollerConfig.class);

        assertEquals(1,testConfig.getBatchSize());
        assertEquals("file:///home/adamrobinson/dev/ingest/poller/src/main/resources/testSrc/",testConfig.getSrcDirectory());
        assertEquals("file:///home/adamrobinson/dev/ingest/poller/src/main/resources/testDest/",testConfig.getDestDirectory());
        assertEquals(8888, testConfig.getPortNumber());
        assertTrue(testConfig.isCopyOnWrite());

        StringBuilder builder = new StringBuilder();
        builder.append("\n-----Poller Configuration-----\n");
        builder.append("Source Directory: file:///home/adamrobinson/dev/ingest/poller/src/main/resources/testSrc/\n");
        builder.append("Destination Directory: file:///home/adamrobinson/dev/ingest/poller/src/main/resources/testDest/\n");
        builder.append("Batch size: " + 1 + "\n");
        builder.append("Poll rate: " + 10000 + "\n");
        builder.append("Copy On Write: " + true + "\n");
        builder.append("Port Number: " + 8888 + "\n");

        assertEquals(builder.toString(), testConfig.toString());
    }

    @Test
    public void testCopyFiles() {
        try {
            this.testConfig = mapper.readValue(new File("src/main/resources/test-config.yaml"), PollerConfig.class);
            LocalFileProvider lfp = new LocalFileProvider(testConfig);
            assertTrue(lfp.move());
            Files.delete(Paths.get("src/main/resources/testDest/testDoc1"));
            Files.delete(Paths.get("src/main/resources/testDest/testDoc2"));
        } catch (IOException ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
