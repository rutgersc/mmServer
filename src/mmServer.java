import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Created by hoi on 10/24/14.
 */
public class mmServer extends Thread {

    private int serverPort;
    private ServerSocket serverSocket;

    mmServer(int port) throws IOException {
        this.serverPort = port;
        this.serverSocket = new ServerSocket(serverPort);

        // Set up how many threads will handle the requests
        int numberOfRequestProcessors = 2;
        System.out.println("Setting up " + numberOfRequestProcessors + " RequestProcessors");

        for(int i = 0; i < 2; i++) {
            (new Thread(new RequestProcessor())).start();
            System.out.println("RequestProcessor #" + i + " - started");
        }
    }

    public void run() {

        System.out.println("Server listening on port: " + serverPort);

        while (true) {
            try {
                Socket androidRequest = serverSocket.accept();
                System.out.println("Got Request");
                RequestProcessor.processRequest(androidRequest);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class RequestProcessor implements Runnable {

    private static LinkedList<Socket> pool = new LinkedList<>();
    private static final Object lock = new Object();

    public static void processRequest(Socket request) {

        // Add all the requests to a pool. Then whenever
        // a RequestProcessor Thread is available it will pop one from the pool.
        synchronized (pool) {
            pool.add(pool.size(), request);
            //System.out.println("          client[" + pool.size() + "] +1 added");
            pool.notifyAll();
        }
    }

    @Override
    public void run() {

        Socket androidSocket;
        InputStreamReader inRaw;
        OutputStreamWriter outRaw;
        BufferedReader in;
        BufferedWriter out;

        while(true) {

            synchronized (pool) {
                while (pool.isEmpty()) {
                    try {
                        pool.wait();
                    } catch (InterruptedException e) { }
                }

                androidSocket = pool.remove(0);

                try {

                    inRaw = new InputStreamReader(androidSocket.getInputStream());
                    outRaw = new OutputStreamWriter(androidSocket.getOutputStream());
                    in = new BufferedReader(inRaw);
                    out = new BufferedWriter(outRaw);

                    //String test = in.readLine();
                    //System.out.println(" UHH " + test);

                    int c;
                    StringBuffer request = new StringBuffer();
                    while(true) {
                        c = inRaw.read();
                        request.append((char)c);
                        if (c == '\r' || c == '\n') break;
                    }
                    System.out.println("Got request: " + request);

                    // TODO: Process request

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


