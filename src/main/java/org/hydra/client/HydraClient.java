package org.hydra.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.X509KeyManager;

import org.glite.security.trustmanager.ContextWrapper;
import org.glite.security.util.DN;
import org.glite.security.util.DNHandler;
import org.hydra.HydraAPI;
import org.hydra.KeyPiece;
import org.joni.test.meta.ACLItem;
import org.joni.test.meta.client.TMHostnameVerifier;

import com.beust.jcommander.JCommander;
import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.TMHessianURLConnectionFactory;

public class HydraClient {
    public static final String ENDPOINT_OPT = "hydraService";
    private static ContextWrapper _wrapper = null;

    /**
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     * @throws GeneralSecurityException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, GeneralSecurityException {
        CommandMain cm = new CommandMain();
        JCommander jc = new JCommander(cm);

        CommandPut commandPut = new CommandPut();
        CommandGet commandGet = new CommandGet();
        CommandRemove commandRm = new CommandRemove();
        jc.addCommand("put", commandPut);
        jc.addCommand("get", commandGet);
        jc.addCommand("rm", commandRm);
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
        _wrapper = new ContextWrapper(props, false);

        TMHostnameVerifier verifier = new TMHostnameVerifier();

        String url = props.getProperty(ENDPOINT_OPT, "https://localhost:40669/HydraService");
        HessianProxyFactory factory = new HessianProxyFactory();
        TMHessianURLConnectionFactory connectionFactory = new TMHessianURLConnectionFactory();
        connectionFactory.setWrapper(_wrapper);
        connectionFactory.setVerifier(verifier);
        connectionFactory.setHessianProxyFactory(factory);
        factory.setConnectionFactory(connectionFactory);
        HydraAPI service = (HydraAPI) factory.create(HydraAPI.class, url);
       
        if (cm.verbose) {
            System.out.println("Server version: " + service.getVersion());
        }

        if (jc.getParsedCommand().equals("put")) {
            put(commandPut, service);
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

    private static void put(CommandPut commandPut, HydraAPI service) throws IOException {
        BigInteger key = new BigInteger(commandPut.key);
        BigInteger iv = new BigInteger(commandPut.iv);

        KeyPiece piece = new KeyPiece();
        piece.iv = iv;
        piece.keyPiece = key;
        piece.minPieces = commandPut.minParts;
        piece.pieceNumber = commandPut.partNumber;

        X509KeyManager manager = _wrapper.getKeyManager();
        String aliases[] = manager.getClientAliases("RSA", null);
        X509Certificate certs[] = manager.getCertificateChain(aliases[0]);
        X509Certificate cert = certs[0];
        DN dn = DNHandler.getSubject(cert);
        String dnString = dn.getRFCDNv2();
        piece.addACLItem(new ACLItem(dnString, true, true));
        service.putKeyPiece(commandPut.id, piece);
        System.out.println("Key " + commandPut.id + " stored.");
    }

}
