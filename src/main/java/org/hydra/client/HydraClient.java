package org.hydra.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Properties;

import org.bouncycastle.crypto.CryptoException;
import org.hydra.HydraAPI;
import org.hydra.KeyPiece;
import org.joni.test.meta.ACLItem;

import com.beust.jcommander.JCommander;
import com.caucho.hessian.client.TMHessianURLConnectionFactory;

import fi.hip.sicx.srp.HandshakeException;
import fi.hip.sicx.srp.SRPAPI;
import fi.hip.sicx.srp.SRPClient;
import fi.hip.sicx.srp.SRPUtil;
import fi.hip.sicx.srp.SessionKey;
import fi.hip.sicx.srp.SessionToken;
import fi.hip.sicx.srp.hessian.HessianSRPProxy;
import fi.hip.sicx.srp.hessian.HessianSRPProxyFactory;

public class HydraClient {
    public static final String ENDPOINT_OPT = "hydraService";

    /**
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     * @throws GeneralSecurityException
     * @throws HandshakeException 
     * @throws CryptoException 
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, GeneralSecurityException, CryptoException, HandshakeException {
        CommandMain cm = new CommandMain();
        JCommander jc = new JCommander(cm);

        CommandPut commandPut = new CommandPut();
        CommandGet commandGet = new CommandGet();
        CommandRemove commandRm = new CommandRemove();
        CommandAddUser commandAddUser = new CommandAddUser();
        jc.addCommand("put", commandPut);
        jc.addCommand("get", commandGet);
        jc.addCommand("rm", commandRm);
        jc.addCommand("adduser", commandAddUser);
        jc.parse(args);

        if (cm.configFile == null) {
            System.err.println("Command missing mandatory configuration file parameter.");
            jc.usage();
            System.exit(1);
        }
        // client
        File configFile = new File(cm.configFile);
        if (!configFile.exists()) {
            System.err.println("Configuration file " + cm.configFile + " does not exist.");
            System.exit(2);
        }

        Properties props = new Properties();
        props.load(new FileReader(configFile));
        props.setProperty("useClientCredentials", "false");
        HessianSRPProxyFactory factory = HessianSRPProxyFactory.getFactory(props);

        String url = props.getProperty(ENDPOINT_OPT, "https://localhost:40669/");
        String srpUrl = url + "SRPService";
        SRPAPI srpService = (SRPAPI) factory.create(SRPAPI.class, srpUrl);
        
        // add the user
        if(jc.getParsedCommand().equals("adduser")){
            adduser(commandAddUser, srpService);
            return;
        }
        String username = props.getProperty("username");
        SessionKey key = SRPClient.login(srpService, username, props.getProperty("password"));

        byte identity[] = SRPUtil.stringBytes(username);
        HydraAPI service = (HydraAPI) factory.create(HydraAPI.class, url + "HydraService");
        HessianSRPProxy proxy = (HessianSRPProxy) Proxy.getInvocationHandler(service);
        proxy.setSession(new SessionToken(identity, key.getK()).toString());
      
        if (cm.verbose) {
            System.out.println("Server version: " + service.getVersion());
        }

        if (jc.getParsedCommand().equals("put")) {
            put(commandPut, service, username);
        } else {
            if (jc.getParsedCommand().equals("get")) {
                get(commandGet, service);
            } else {
                if (jc.getParsedCommand().equals("rm")) {
                    remove(commandRm, service);
                } else {
                    System.err.println("Invalid command: " + jc.getParsedCommand());

                }
            }
        }

    }

    private static void adduser(CommandAddUser commandAddUser, SRPAPI srpService) {
        SRPClient.putVerifier(srpService, commandAddUser.username, commandAddUser.password);
        
    }

    private static void remove(CommandRemove commandRm, HydraAPI service) throws IOException {
        service.removeKeyPiece(commandRm.id);
        System.out.println("Key " + commandRm.id + " removed.");
    }

    private static void get(CommandGet commandGet, HydraAPI service) throws IOException {
        KeyPiece piece = service.getKeyPiece(commandGet.id);
        System.out.println("Key id: " + commandGet.id);
        System.out.println("Key value: " + piece.keyPiece.toString());
        System.out.println("The IV: " + (piece.iv == null ? "null" : piece.iv.toString()));
        System.out.println("Min number of keypieces: " + piece.minPieces);
        System.out.println("Number of the piece: " + piece.pieceNumber);
    }

    private static void put(CommandPut commandPut, HydraAPI service, String username) throws IOException {
        BigInteger key = new BigInteger(commandPut.key);
        BigInteger iv = new BigInteger(commandPut.iv);

        KeyPiece piece = new KeyPiece();
        piece.iv = iv;
        piece.keyPiece = key;
        piece.minPieces = commandPut.minParts;
        piece.pieceNumber = commandPut.partNumber;

        piece.addACLItem(new ACLItem(username, true, true));
        service.putKeyPiece(commandPut.id, piece);
        System.out.println("Key " + commandPut.id + " stored.");
    }

}
