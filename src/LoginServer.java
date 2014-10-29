import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class LoginServer extends Thread {

    private SSLServerSocket serverSocket;

    LoginServer(int serverPort) throws IOException {

        String truststore = "servertruststore.jks";
        String keystore = "server.jks";

        char storePass[] = "aabb11".toCharArray();

        try {

            // Setup truststore
            KeyStore trustStore = KeyStore.getInstance("JKS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustStore.load(new FileInputStream(truststore), storePass);
            trustManagerFactory.init(trustStore);

            // Load keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keystore), storePass);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, storePass);

            // Setup SSL Context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            // Create Socket with the SSLContext
            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            this.serverSocket = (SSLServerSocket) ssf.createServerSocket(serverPort);

            printServerSocketInfo(serverSocket);
            System.out.println("SSL Server port: " + serverPort);

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        SSLSocket androidSocket = null;
        InputStreamReader inRaw = null;
        OutputStreamWriter outRaw = null;
        BufferedReader in = null;
        PrintWriter out = null;

        while (true) {

            System.out.println("SSL Login ready.");

            try {
                androidSocket = (SSLSocket) serverSocket.accept();
                printSocketInfo(androidSocket);

                inRaw = new InputStreamReader(androidSocket.getInputStream());
                outRaw = new OutputStreamWriter(androidSocket.getOutputStream());
                in = new BufferedReader(inRaw);
                out = new PrintWriter(new BufferedWriter(outRaw));

                String request = in.readLine();

                System.out.println("New request: " + request);

                switch (request) {

                    case "guestLogin":
                        handleGuestLogin(in,out);
                        break;

                    case "login":
                        handleLogin(in,out);
                        break;

                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                /*
                try {

                    if (inRaw != null) {
                        inRaw.close();
                    }
                    if (outRaw != null) {
                        outRaw.flush();
                        outRaw.close();
                    }
                    if (androidSocket != null) {
                       // androidSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

            }

        }

    }

    private void handleGuestLogin(BufferedReader in, PrintWriter out) throws IOException {

        //TODO: Set up guest ID
        out.println("Guest9001");
        out.flush();
    }

    private void handleLogin(BufferedReader in, PrintWriter out) throws IOException {
        String username = in.readLine();
        String password = in.readLine();

        boolean isValidLogin = false;

        //TODO: check username & password

        //TODO: Remove test
        if(username.equals("test")) {
            isValidLogin = true;
        }

        System.out.println("u: " + username + " - p: " + password);

        if(isValidLogin) {
            out.write("Success\n");
        }
        else {
            out.write("Fail\n");
        }
        out.flush();
    }










    private static void printSocketInfo(SSLSocket s) {
        System.out.println("Socket class: " + s.getClass());
        System.out.println("   Remote address = "
                + s.getInetAddress().toString());
        System.out.println("   Remote port = " + s.getPort());
        System.out.println("   Local socket address = "
                + s.getLocalSocketAddress().toString());
        System.out.println("   Local address = "
                + s.getLocalAddress().toString());
        System.out.println("   Local port = " + s.getLocalPort());
        System.out.println("   Need client authentication = "
                + s.getNeedClientAuth());
        SSLSession ss = s.getSession();
        System.out.println("   Cipher suite = " + ss.getCipherSuite());
        System.out.println("   Protocol = " + ss.getProtocol());
    }

    private static void printServerSocketInfo(SSLServerSocket s) {
        System.out.println("Server socket class: " + s.getClass());
        System.out.println("   Socker address = "
                + s.getInetAddress().toString());
        System.out.println("   Socker port = "
                + s.getLocalPort());
        System.out.println("   Need client authentication = "
                + s.getNeedClientAuth());
        System.out.println("   Want client authentication = "
                + s.getWantClientAuth());
        System.out.println("   Use client mode = "
                + s.getUseClientMode());
    }
}