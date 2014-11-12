

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by hoi on 10/24/14.
 */
public class mmServer extends Thread {

    private int serverPort;
    private ServerSocket serverSocket;

    public static HashMap<String, UserData> sessions;

    /**
     * Add session
     *
     * @param uuid
     * @param userData
     */
    public static void addSession(UUID uuid, UserData userData) {
        sessions.put(uuid.toString(), userData);
        //TODO: make method secure for when LoginServer.java uses this method (Multithreading)
        System.out.println("Added new session: " + uuid + " username: " + userData.username);
    }

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

        sessions = new HashMap<>();
        /**
         * TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA
         */
        Location dummyLoc = new Location("dummy");
        dummyLoc.setLatitude(20.3);
        dummyLoc.setLongitude(52.6);
        String uuid1 = "518923af-465f-4b2b-b31d-a0c57ce0518b";//UUID.randomUUID().toString();
        String uuid2 = "9d32d79a-de11-4e72-a61c-c9996f47a8b7";//UUID.randomUUID().toString();
        sessions.put(uuid1, new UserData("1","testuser1000", dummyLoc, new Date()));
        sessions.put(uuid2, new UserData("1","testuser2000", dummyLoc, new Date()));
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
                    StringBuilder requestB = new StringBuilder();
                    while(true) {
                        c = inRaw.read();
                        requestB.append((char)c);
                        if (c == '\r' || c == '\n') break;
                    }
                    String request = requestB.toString();
                    System.out.println("Got request: " + request);

                    // TODO: Process request
                    switch (request) {
                        case "updateLocation":
                            handleRequest_updateLocation(in,out);
                            break;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleRequest_updateLocation(BufferedReader in, BufferedWriter out) throws IOException {

        String sessionId = in.readLine();

        UserData userData = checkSessionId(sessionId);

        String latitude = in.readLine();
        String longitude = in.readLine();

        try {
            System.out.println("Got new location"  + latitude + " " + longitude);
            if(userData != null) {
                userData.currentLocation = new Location("");
                userData.currentLocation.setLatitude(Double.parseDouble(latitude));
                userData.currentLocation.setLongitude(Double.parseDouble(longitude));
            }
        } catch (NumberFormatException e) {
            System.err.println("Failed to convert string(s) to double: " + latitude + " " + longitude);
        }
    }

    private UserData checkSessionId(String sessionId) {
        UserData userData = mmServer.sessions.get(sessionId);

        if(userData == null) {
            //TODO: No session with this id found
        }

        return userData;
    }

}


