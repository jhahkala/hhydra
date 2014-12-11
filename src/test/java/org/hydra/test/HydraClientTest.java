package org.hydra.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.bouncycastle.crypto.CryptoException;
import org.hydra.client.HydraClient;
import org.hydra.server.HydraServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fi.hip.sicx.srp.HandshakeException;

public class HydraClientTest {
    public static final String TEST_USER = "USerNAmssfedfs";
    public static final String TEST_USER_PW = "PassWordaa";
    public static final String TEST_USER2 = "2userNNAamee";
    public static final String TEST_USER2_PW = "passslkjlkj";
    public static final String TRUSTED_CLIENT_CONFIG_FILE = "src/test/hydra-client-trusted.conf";
    public static final String TRUSTED_CLIENT2_CONFIG_FILE = "src/test/hydra-client2-trusted.conf";
    public static final String SERVER_PURGE_CONFIG_FILE = "src/test/hydra-purge.conf";
    HydraServer server;

    @After
    public void endserver() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Before
    public void setupServer() throws Exception {
        System.out.println("Starting hydra server....");
        server = new HydraServer();
        server.configure(SERVER_PURGE_CONFIG_FILE);
        server.start();

    }

    @Test
    public void testClient() throws FileNotFoundException, IOException, GeneralSecurityException, CryptoException, HandshakeException{
        HydraClient.main(new String[]{"--conf", TRUSTED_CLIENT_CONFIG_FILE, "adduser", "--username", TEST_USER, "--password", TEST_USER_PW});
        HydraClient.main(new String[]{"--conf", TRUSTED_CLIENT_CONFIG_FILE, "put", "--id", "test2", "--iv", "12345", "--key", "12345678", "--min", "2", "--part", "3"});
        HydraClient.main(new String[]{"--conf", TRUSTED_CLIENT_CONFIG_FILE, "get", "--id", "test2"});
        HydraClient.main(new String[]{"--conf", TRUSTED_CLIENT_CONFIG_FILE, "rm", "--id", "test2"});
        
        // Should actually fail, as overwriting users should not be allowed...
        HydraClient.main(new String[]{"--conf", TRUSTED_CLIENT_CONFIG_FILE, "adduser", "--username", TEST_USER, "--password", TEST_USER_PW});
    }
    
}
