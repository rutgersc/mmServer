

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by hoi on 10/24/14.
 */
public class mmServer extends Thread {

    private int serverPort;
    private ServerSocket serverSocket;

    public static HashMap<String, PlayerData> sessions;

    /**
     * Add session
     *
     * @param uuid
     * @param playerData
     */
    public static void addSession(UUID uuid, PlayerData playerData) {
        sessions.put(uuid.toString(), playerData);
        //TODO: make method secure for when LoginServer.java uses this method (Multithreading)
        System.out.println("Added new session: " + uuid + " username: " + playerData.username);
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
        dummyLoc.setLatitude(53.212082); dummyLoc.setLongitude(5.799376);
        Location dummyLoc2 = new Location("dummy");
        dummyLoc2.setLatitude(53.212046); dummyLoc2.setLongitude(5.800518);
        Location dummyLoc3 = new Location("dummy");
        dummyLoc3.setLatitude(53.212032); dummyLoc2.setLongitude(5.800100);
        String uuid1 = "518923af-465f-4b2b-b31d-a0c57ce0518b";//UUID.randomUUID().toString();
        String uuid2 = "9d32d79a-de11-4e72-a61c-c9996f47a8b7";//UUID.randomUUID().toString();
        sessions.put(uuid1, new PlayerData("1","testuser1000", dummyLoc, new Date()));
        sessions.put(uuid2, new PlayerData("1","testuser2000", dummyLoc2, new Date()));
        sessions.put("testSessionId", new PlayerData("1","testuser2000", dummyLoc3, new Date()));
    }

    public void run() {

        System.out.println("Server listening on port: " + serverPort);

        while (true) {
            try {
                Socket androidRequest = serverSocket.accept();
                System.out.println("Got new socket");
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
        PrintWriter out;

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
                    out = new PrintWriter(outRaw);

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

                        case "requestNearbyPlayers":
                            handleRequest_requestNearbyPlayers(in,out);
                            break;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleRequest_updateLocation(BufferedReader in, PrintWriter out) throws IOException {

        String sessionId = in.readLine();
        PlayerData playerData = checkSessionId(sessionId);

        String latitude = in.readLine();
        String longitude = in.readLine();

        try {
            System.out.println("Got new location"  + latitude + " " + longitude);
            if(playerData != null) {
                playerData.location = new Location("");
                playerData.location.setLatitude(Double.parseDouble(latitude));
                playerData.location.setLongitude(Double.parseDouble(longitude));
            }
        } catch (NumberFormatException e) {
            System.err.println("Failed to convert string(s) to double: " + latitude + " " + longitude);
        }
    }

    private void handleRequest_requestNearbyPlayers(BufferedReader in, PrintWriter out) throws IOException {
        String sessionId = in.readLine();
        PlayerData playerData = checkSessionId(sessionId);

        out.println(getNearbyPlayersString(playerData.location));
        out.flush();
    }

    private String getNearbyPlayersString(Location location) {

        StringBuilder sb = new StringBuilder();

        for(Map.Entry<String, PlayerData> entry : mmServer.sessions.entrySet()) {
            String uuidKey = entry.getKey();
            PlayerData playerData = entry.getValue();

            float distance = location.distanceTo(playerData.location);

            if(distance > 9001) {//TODO: Change to usable value

                sb.append(playerData.username);
                sb.append(",");
                sb.append(playerData.location.getLatitude());
                sb.append(",");
                sb.append(playerData.location.getLongitude());
                sb.append(";");
            }
        }

        if(sb.length() == 0) {
            sb.append("Empty");
        }

        return sb.toString();
    }

    private PlayerData checkSessionId(String sessionId) {
        PlayerData playerData = mmServer.sessions.get(sessionId);

        if(playerData == null) {
            //TODO: No session with this id found
        }

        return playerData;
    }

}


