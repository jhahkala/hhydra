hhydra
======

Hydra key part service implemented in hessian protocol


Usage
=====

Run the server
==============

Contents of the server config file hhydra.conf:
--------------------
```
port=50201
storeConfigFile=hhydra-storage.xml
host=sicx1.hip.helsinki.fi
sslCertFile=hostcert.pem
sslKey=hostkey.pem
trustStoreDir=/etc/grid-security/certificates
superuser=CN=trusted client,OU=Relaxation,O=Utopia,L=Tropic,C=UG
```
--------------------

Contents of hhydra-storage.xml
--------------------
```
<?xml version="1.0" encoding="UTF-8"?>
<infinispan xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns="urn:infinispan:config:5.0">
  <global />
  <default />
  <namedCache name="hhydra">
    <loaders shared="false">
      <loader
          class="org.infinispan.loaders.file.FileCacheStore"
          fetchPersistentState="true" ignoreModifications="false"
          purgeOnStartup="false">
        <properties>
          <property name="location" value="hhydra-storage.dat" />
        </properties>
      </loader>
    </loaders>
  </namedCache>
  <namedCache name="hydraUsers">
    <loaders shared="false">
      <loader
          class="org.infinispan.loaders.file.FileCacheStore"
          fetchPersistentState="true" ignoreModifications="false"
          purgeOnStartup="false">
        <properties>
          <property name="location" value="hhydra-storage.dat" />
        </properties>
      </loader>
    </loaders>
  </namedCache>
</infinispan>
```
--------------------

You should have the CA certificates in the directory specified and the host credentials in the directory where you run the program or where you specify them in the config file.

Start the server with command:
java -cp hhydra.jar org.hydra.server.HydraServer hhydra.conf

On RHEL machine don't forget to edit /etc/sysconfig/iptables to open the ports for the service.


Run the client
==============

Contents of the client config file hhydra-client-trusted.conf:
--------------------
```
username=testuser
password=testpw
useClientCredentials=false
trustStoreDir=certificates
hydraService=https://sicx1.hip.helsinki.fi:50201/
```
--------------------

You should have the trusted CA certificates in the specified directory.

Run the client with command:
java -cp hhydra.jar org.hydra.client.HydraClient -c hhydra-client-trusted.conf put --id test2 --iv 12345 --key 12345678 --min 2 --part 3

But if the user is not in the service yet, you have to add him before running the put command by runing adduser command:
java -cp hhydra.jar org.hydra.client.HydraClient -c hhydra-client-trusted.conf adduser --username testuser --password testpw


(Have to use openJDK, as the sun/oracle jdk gives security violation about loading the bouncycastle JCE. Or you have to install the Unlimited Strength Jurisdiction Policy Files, thanks to US government idiocy.)

