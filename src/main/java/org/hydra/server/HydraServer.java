package org.hydra.server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glite.security.trustmanager.ContextWrapper;

import fi.hip.sicx.srp.SRPService;

public class HydraServer {

    private Server _server = null;
    public static final String PORT_OPT = "port";
    public static final String HOST_OPT = "host";

    public HydraServer() {
    }

    public void join() throws InterruptedException {
        _server.join();
    }

    public void stop() throws Exception {
        if (_server != null) {
            _server.stop();
        }
    }

    public void start() throws Exception {
        _server.start();
    }

    /**
     * The configuration method, configures the service based on the contents of
     * the configuration file.
     * 
     * @param filename the file to read the configuration from.
     * @throws IOException Thrown in case there is a problem in reading the
     *             configuration file(s).
     * @throws GeneralSecurityException thrown when there is a violation of
     *             security constraints.
     */
    public void configure(String filename) throws IOException, GeneralSecurityException {
        if (filename == null) {
            throw new NullPointerException("Configuration file can't be null.");
        }
        File configFile = new File(filename);
        if (!configFile.exists() || configFile.isDirectory()) {
            throw new IOException("Configuration file \"" + configFile + "\" does not exist or is a directory.");
        }
        Properties props = new Properties();
        props.load(new FileReader(configFile));

        SslContextFactory factory = new SslContextFactory();
        factory.setSslContext((new ContextWrapper(props, false)).getContext());
        factory.setWantClientAuth(false);
        factory.setNeedClientAuth(false);
        SslSelectChannelConnector connector = new SslSelectChannelConnector(factory);

        int port = Integer.parseInt(props.getProperty(PORT_OPT));
        String host = props.getProperty(HOST_OPT);

        connector.setPort(port);
        connector.setHost(host);

        _server = new Server(port);
        _server.setSendServerVersion(false);
        _server.setSendDateHeader(false);
        _server.setConnectors(new Connector[] { connector });
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        context.setContextPath("/"); // = everything, no securitycontexthandler etc yet

        SRPService srpService = new SRPService(filename);
        context.addServlet(new ServletHolder(srpService), "/SRPService");
        //needed for the access to the session cache
        context.addServlet(new ServletHolder(new HydraService(filename, srpService)), "/HydraService");
        context.setContextPath("/");
        _server.setHandler(context);

    }


    /**
     * The main method to start the server from command line.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("MetaServer needs the configuration file as an argument!");
            System.exit(2);
        }

        if (args[0] == null) {
            System.out.println("MetaServer needs the configuration file as an argument!");
            System.exit(2);
        }

        HydraServer server = new HydraServer();
        server.configure(args[0]);
        server.start();
        server.join();

    }

}
