package org.hydra.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import junit.framework.TestCase;

import org.glite.security.trustmanager.ContextWrapper;
import org.hydra.HydraAPI;
import org.hydra.KeyPiece;
import org.hydra.server.HydraServer;
import org.joni.test.meta.ACLItem;
import org.joni.test.meta.client.TMHostnameVerifier;
import org.junit.Test;

import com.caucho.hessian.client.HessianProxyFactory;

public class HydraServiceTest {
    public static final String TEST_USER = "CN=trusted client,OU=Relaxation,O=Utopia,L=Tropic,C=UG";
    public static final String TEST_USER2 = "CN=trusted clientserver,OU=Relaxation,O=Utopia,L=Tropic,C=UG";
    public static final String TRUSTED_CLIENT_CONFIG_FILE = "src/test/hydra-client-trusted.conf";
    public static final String TRUSTED_CLIENT2_CONFIG_FILE = "src/test/hydra-client2-trusted.conf";
    public static final String SERVER_PURGE_CONFIG_FILE = "src/test/hydra-purge.conf";
    HydraServer server;

    public void setup() throws Exception {
        server = new HydraServer();
        server.configure(SERVER_PURGE_CONFIG_FILE);
        server.start();
        File configFile = new File(TRUSTED_CLIENT_CONFIG_FILE);
        Properties props = new Properties();
        props.load(new FileReader(configFile));
        ContextWrapper wrapper = new ContextWrapper(props, false);
        HttpsURLConnection.setDefaultSSLSocketFactory(wrapper.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new TMHostnameVerifier());

    }
    
    public void testDummy(){
    	return;
    }

    @Test
    public void testDelete() throws Exception {
        try {

            setup();

            String url = "https://localhost:8773/HydraService";
            HessianProxyFactory factory = new HessianProxyFactory();
            HydraAPI service = (HydraAPI) factory.create(HydraAPI.class, url);

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
            File configFile = new File(TRUSTED_CLIENT2_CONFIG_FILE);
            Properties props = new Properties();
            props.load(new FileReader(configFile));
            ContextWrapper wrapper = new ContextWrapper(props, false);
            HttpsURLConnection.setDefaultSSLSocketFactory(wrapper.getSocketFactory());

            exception = false;
            try {
                service.removeKeyPiece("test");

            } catch (IOException e) {
                exception = true;
            }
            assertTrue(exception);
        } finally {
            server.stop();
        }
    }

    @Test
    public void testPut() throws Exception {
        try {

            setup();

            String url = "https://localhost:8773/HydraService";
            HessianProxyFactory factory = new HessianProxyFactory();
            HydraAPI service = (HydraAPI) factory.create(HydraAPI.class, url);

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
        } finally {
            server.stop();
        }
    }

    @Test
    public void testGet() throws Exception {
        try {

            setup();

            String url = "https://localhost:8773/HydraService";
            HessianProxyFactory factory = new HessianProxyFactory();
            HydraAPI service = (HydraAPI) factory.create(HydraAPI.class, url);

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

        } finally {
            server.stop();
        }
    }
}
