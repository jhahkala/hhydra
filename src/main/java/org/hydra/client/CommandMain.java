package org.hydra.client;

import com.beust.jcommander.Parameter;

public class CommandMain {
    @Parameter(names = { "-v", "--verbose" }, description = "Verbose output.")
    public boolean verbose = false;

    @Parameter(names = { "-c", "--conf" }, description = "Mandatory. The configuration file that contains connection parameters.")
    public String configFile = null;

}
