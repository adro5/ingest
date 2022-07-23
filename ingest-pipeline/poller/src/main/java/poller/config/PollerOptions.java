package poller.config;

import org.apache.commons.cli.Options;

public class PollerOptions extends Options {
    public PollerOptions() {
        super();
        super.addRequiredOption("configFile", "configFile",true, "configuration file used to build poller");
    }

}
