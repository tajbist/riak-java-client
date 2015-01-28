package com.basho.riak.client.core.operations.itest;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import sun.misc.BASE64Decoder;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;

public class RiakPemConnection {

    private static final KeyStore trustStore = loadTruststore();

    /**
     * Load Truststore using Trusted Certificates.
     * @return a keystore with all trusted certificate entries loaded
     */
    private static KeyStore loadTruststore(){

        KeyStore truststore = null;
        try {
            CertificateFactory cFactory = CertificateFactory.getInstance("X.509");

            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("cacert.pem");
            X509Certificate caCert = (X509Certificate) cFactory.generateCertificate(in);
            in.close();

            in = Thread.currentThread().getContextClassLoader().getResourceAsStream("cert.pem");
            X509Certificate serverCert = (X509Certificate) cFactory.generateCertificate(in);
            in.close();

            truststore = KeyStore.getInstance(KeyStore.getDefaultType());
            truststore.load(null, "password".toCharArray());
            truststore.setCertificateEntry("cacert", caCert);
            truststore.setCertificateEntry("server", serverCert);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return truststore;
    }

    /**
     * Load Keystore using the Private Key Pem file and Public Cert Pem file
     * @param privateKeyPemPath path to the Private Key Pem file
     * @param publicCertPemPath path to the Public Cert Pem file
     * @return a keystore with private certificate entry loaded
     */
    private static KeyStore loadKeystore(String privateKeyPemPath, String publicCertPemPath) {

        KeyStore keystore = null;
        try {
            CertificateFactory cFactory = CertificateFactory.getInstance("X.509");
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(publicCertPemPath);
            X509Certificate public_cert = (X509Certificate) cFactory.generateCertificate(in);
            in.close();

            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(privateKeyPemPath);
            byte[] privKeyBytes = new byte[in.available()];
            in.read(privKeyBytes, 0, in.available());
            in.close();

            String key = new String(privKeyBytes);
            key = key.replace("-----BEGIN PRIVATE KEY-----\n", "").replace("-----END PRIVATE KEY-----\n", "");

            BASE64Decoder base64Decoder = new BASE64Decoder();
            privKeyBytes = base64Decoder.decodeBuffer(key);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec ks = new PKCS8EncodedKeySpec(privKeyBytes);
            PrivateKey privKey = (PrivateKey) keyFactory.generatePrivate(ks);

            keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, "password".toCharArray());
            keystore.setKeyEntry("private-key", privKey,"".toCharArray(),new java.security.cert.Certificate[] { public_cert });

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return keystore;
    }

    /**
     * Initialize Cluster
     * @param builder all builder properties required to make connection to a node.
     * @return Riak Cluster object based on builder properties
     */
    private static RiakCluster initializeRiakCluster(RiakNode.Builder builder){
        RiakCluster cluster = null;
        try {
            cluster = new RiakCluster.Builder(builder.build()).build();
            cluster.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return cluster;
    }

    /**
     * Get Riak Cluster Handle. This is for unsecured connection when Riak security is disabled.
     * @return Riak Cluster Object
     */
    public static RiakCluster getRiakCluster(){

        RiakNode.Builder builder = new RiakNode.Builder();
        builder.withMinConnections(1);
        RiakCluster cluster = initializeRiakCluster(builder);

        return cluster;
    }

    /**
     * Get Riak Client Handle. This is for unsecured connection when Riak security is disabled.
     * @return Riak Client Object
     */
    public static RiakClient getRiakConnection(){
        RiakClient client = null;
        RiakCluster cluster = getRiakCluster();
        if(cluster!=null){
            client= new RiakClient(cluster);
        }
        return client;
    }

    /**
     * Get Riak Cluster Handle. This is for secured connection with user source set to either Trust or Password. Riak security is enabled.
     * @param username username with which the connection needs to be established
     * @param password password of the username provided
     * @return Riak Cluster Object
     */
    public static RiakCluster getRiakCluster(String username, String password){

        RiakNode.Builder builder = new RiakNode.Builder();
        builder.withMinConnections(1);
        builder.withAuth(username, password, trustStore);
        RiakCluster cluster = initializeRiakCluster(builder);

        return cluster;
    }

    /**
     * Get Riak Client Handle. This is for secured connection with user source set to either Trust or Password. Riak security is enabled.
     * @param username username with which the connection needs to be established
     * @param password password of the username provided
     * @return Riak Client Object
     */
    public static RiakClient getRiakConnection(String username, String password){
        RiakClient client = null;
        RiakCluster cluster = getRiakCluster(username,password);
        if(cluster!=null){
            client= new RiakClient(cluster);
        }
        return client;
    }

    /**
     * Get Riak Cluster Handle. This is for secured connection with user source set to certificate. Riak security is enabled.
     * @param username username with which the connection needs to be established
     * @param password password of the username provided
     * @param privateKeyPemPath path to the Private Key Pem file of the user.
     * @param publicCertPemPath path to the Public Cert Pem file of the Private key provided.
     * @return Riak Cluster Object
     */
    public static RiakCluster getRiakCluster(String username, String password, String privateKeyPemPath, String publicCertPemPath){
        KeyStore keyStore = loadKeystore(privateKeyPemPath,publicCertPemPath);

        RiakNode.Builder builder = new RiakNode.Builder();
        builder.withMinConnections(1);
        builder.withAuth(username, password, trustStore, keyStore, "");
        RiakCluster cluster = initializeRiakCluster(builder);

        return cluster;
    }

    /**
     * Get Riak Client Handle. This is for secured connection with user source set to certificate. Riak security is enabled.
     * @param username username with which the connection needs to be established
     * @param password password of the username provided
     * @param privateKeyPemPath path to the Private Key Pem file of the user.
     * @param publicCertPemPath path to the Public Cert Pem file of the Private key provided.
     * @return Riak Client Object
     */
    public static RiakClient getRiakConnection(String username, String password, String privateKeyPemPath, String publicCertPemPath){
        RiakClient client = null;
        RiakCluster cluster = getRiakCluster(username,password,privateKeyPemPath,publicCertPemPath);
        if(cluster!=null){
            client= new RiakClient(cluster);
        }
        return client;
    }
}
