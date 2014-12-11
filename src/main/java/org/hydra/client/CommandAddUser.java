package org.hydra.client;

import com.beust.jcommander.Parameter;

public class CommandAddUser {
    @Parameter(names = "--username", description = "The username of the user to add.")
    public String username;
    @Parameter(names = "--password", description = "The password for the user.")
    public String password;

}
