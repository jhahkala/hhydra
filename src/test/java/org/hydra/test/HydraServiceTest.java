package org.hydra.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import org.bouncycastle.crypto.CryptoException;
import org.hydra.HydraAPI;
import org.hydra.KeyPiece;
import org.hydra.server.HydraServer;
import org.joni.test.meta.ACLItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fi.hip.sicx.srp.HandshakeException;
import fi.hip.sicx.srp.SRPAPI;
import fi.hip.sicx.srp.SRPClient;
import fi.hip.sicx.srp.SRPUtil;
import fi.hip.sicx.srp.SessionKey;
import fi.hip.sicx.srp.SessionToken;
import fi.hip.sicx.srp.hessian.HessianSRPProxy;
import fi.hip.sicx.srp.hessian.HessianSRPProxyFactory;

public class HydraServiceTest {
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

    public HydraAPI login(String username, String passwordString) throws FileNotFoundException, IOException, GeneralSecurityException,
            CryptoException, HandshakeException {
        // client
        HessianSRPProxyFactory factory = HessianSRPProxyFactory.getFactory(TRUSTED_CLIENT_CONFIG_FILE);
        String srpUrl = "https://localhost:8773/SRPService";
        SRPAPI srpService = (SRPAPI) factory.create(SRPAPI.class, srpUrl);

        SRPClient.putVerifier(srpService, username, passwordString);

        byte identity[] = SRPUtil.stringBytes(username);
        byte password[] = SRPUtil.stringBytes(passwordString);

        SessionKey key = SRPClient.login(srpService, identity, password);

        String url = "https://localhost:8773/HydraService";
        HydraAPI service = (HydraAPI) factory.create(HydraAPI.class, url);
        HessianSRPProxy proxy = (HessianSRPProxy) Proxy.getInvocationHandler(service);
        proxy.setSession(new SessionToken(identity, key.getK()).toString());

        return service;

    }

    public void testDummy() {
        return;
    }

    @Test
    public void testDelete() throws Exception {

        HydraAPI service = login(TEST_USER, TEST_USER_PW);

        boolean exception = false;
        try {
            service.removeKeyPiece(null);
        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            service.removeKeyPiece("test");

        } catch (IOException e) {
            exception = true;
        }
        assertTrue(exception);

        KeyPiece piece = new KeyPiece();
        piece.iv = new BigInteger("111");
        piece.minPieces = 2;
        piece.pieceNumber = 0;
        piece.keyPiece = new BigInteger("222");
        piece.addACLItem(new ACLItem(TEST_USER, true, true));

        service.putKeyPiece("test", piece);
        service.removeKeyPiece("test");

        piece.removeACLItem(0);
        piece.addACLItem(new ACLItem(TEST_USER, false, true));
        service.putKeyPiece("test", piece);
        service.removeKeyPiece("test");
        service.putKeyPiece("test", piece);
        exception = false;
        try {
            service.removeKeyPiece("test2");

        } catch (IOException e) {
            exception = true;
        }
        assertTrue(exception);

        piece.removeACLItem(0);
        piece.addACLItem(new ACLItem(TEST_USER, true, false));
        service.putKeyPiece("test3", piece);

        exception = false;
        try {
            service.removeKeyPiece("test3");

        } catch (IOException e) {
            exception = true;
        }
        assertTrue(exception);
        exception = false;
        try {
            service.removeKeyPiece(null);

        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue(exception);
        service = login(TEST_USER2, TEST_USER2_PW);

        exception = false;
        try {
            service.removeKeyPiece("test");

        } catch (IOException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testPut() throws Exception {

        HydraAPI service = login(TEST_USER, TEST_USER_PW);

        KeyPiece piece = new KeyPiece();
        piece.iv = new BigInteger("111");
        piece.minPieces = 2;
        piece.pieceNumber = 0;
        piece.keyPiece = new BigInteger("222");
        piece.addACLItem(new ACLItem(TEST_USER, true, true));

        service.putKeyPiece("test", piece);
        boolean exception = false;
        try {
            service.putKeyPiece("test", piece);

        } catch (IOException e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            service.putKeyPiece("test", null);

        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue(exception);
        exception = false;
        try {
            service.putKeyPiece("test2", null);

        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue(exception);
        exception = false;
        try {
            service.putKeyPiece(null, null);

        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue(exception);
        exception = false;
        try {
            service.putKeyPiece(null, piece);

        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testGet() throws Exception {

        HydraAPI service = login(TEST_USER, TEST_USER_PW);

        boolean exception = false;
        try {
            service.getKeyPiece(null);

        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue(exception);

        KeyPiece piece = new KeyPiece();
        piece.iv = new BigInteger("111");
        piece.minPieces = 2;
        piece.pieceNumber = 0;
        piece.keyPiece = new BigInteger("222");
        piece.addACLItem(new ACLItem(TEST_USER, true, true));

        service.putKeyPiece("test", piece);
        exception = false;
        try {
            service.getKeyPiece(null);

        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            service.getKeyPiece("test2");

        } catch (IOException e) {
            exception = true;
        }
        assertTrue(exception);

        KeyPiece pieceGot = service.getKeyPiece("test");
        assertEquals(piece.iv, pieceGot.iv);
        assertTrue(piece.minPieces == pieceGot.minPieces);
        assertTrue(piece.pieceNumber == pieceGot.pieceNumber);
        assertEquals(piece.keyPiece, pieceGot.keyPiece);
        assertEquals(piece.getACL(), pieceGot.getACL());

        pieceGot.removeACLItem(0);
        pieceGot.addACLItem(new ACLItem(TEST_USER, false, true));
        service.putKeyPiece("test2", pieceGot);
        exception = false;
        try {
            service.getKeyPiece("test2");

        } catch (IOException e) {
            exception = true;
        }
        assertTrue(exception);
        service.removeKeyPiece("test2");
        pieceGot.addACLItem(new ACLItem(TEST_USER2, true, true));
        service.putKeyPiece("test2", pieceGot);
        exception = false;
        try {
            service.getKeyPiece("test2");

        } catch (IOException e) {
            exception = true;
        }
        assertTrue(exception);

    }
}
