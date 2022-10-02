package config

import org.apache.commons.cli.Options

class OptionsConfiguration : Options() {
    init {
        super.addRequiredOption("configFile", "configFile",true, "configuration file used to build tools")
    }
}