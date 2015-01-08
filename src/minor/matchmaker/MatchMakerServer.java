package minor.matchmaker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import minor.Location;
import minor.Utility;
import minor.game.GameSessionsServer;

/**
 * Created by hoi on 10/24/14.
 */
public class MatchMakerServer extends Thread {

    private int serverPort;
    private ServerSocket serverSocket;

    public static HashMap<String, PlayerData> sessions;
    private static final Object sessionsLock = new Object();

    /**
     * Add session
     *
     * @param uuid
     * @param username
     */
    public static void addOrRetreiveExistingSession(String uuid, String username) {

        //TODO: make method secure for when minor.LoginServer.java uses this method (Multithreading)
        PlayerData playerData;
        synchronized (sessionsLock) {

            //TODO: Check if uuid is expired (?)
            playerData = sessions.get(uuid);

            if(playerData == null) {
                playerData = new PlayerData("1", username, new Location(""), new Date());
            }

            sessions.put(uuid, playerData);
        }

        System.out.println("Added new session: " + uuid + " username: " + username);
    }

    public static boolean isValidExistingUuid(String uuid) {

        synchronized (sessionsLock) {
            return sessions.containsKey(uuid);
        }
    }


    public MatchMakerServer(int port) throws IOException {
        this.serverPort = port;
        this.serverSocket = new ServerSocket(serverPort);

        // Set up how many threads will handle the requests
        int numberOfRequestProcessors = 20;
        System.out.println("Setting up " + numberOfRequestProcessors + " RequestProcessors");

        for(int i = 0; i < numberOfRequestProcessors; i++) {
            (new Thread(new RequestProcessor())).start();
            System.out.println("minor.matchmaker.RequestProcessor #" + i + " - started");
        }

        sessions = new HashMap<>();

        Location dummyLoc = new Location("dummy");
        dummyLoc.setLatitude(53.212032); dummyLoc.setLongitude(5.800100);
        PlayerData dummy = new PlayerData("1","testuser3000", dummyLoc, new Date());
        sessions.put("ec1d2602-0397-48f7-9bd9-599b26ac80d5", dummy);
        generateDummyPlayers(dummyLoc);
    }

    public void generateDummyPlayers(Location startLocation) {
        /**
         * TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA
         */
        Location dummyLoc = new Location("dummy");
        dummyLoc.setLatitude(startLocation.getLatitude() + 0.1); dummyLoc.setLongitude(startLocation.getLongitude() + 0.1);
        Location dummyLoc2 = new Location("dummy");
        dummyLoc2.setLatitude(startLocation.getLatitude() - 0.1); dummyLoc2.setLongitude(startLocation.getLongitude() - 0.1);

        String uuid1 = "518923af-465f-4b2b-b31d-a0c57ce0518b";//UUID.randomUUID().toString();
        String uuid2 = "9d32d79a-de11-4e72-a61c-c9996f47a8b7";//UUID.randomUUID().toString();
        sessions.put(uuid1, new PlayerData("1","testuser1000", dummyLoc, new Date()));
        sessions.put(uuid2, new PlayerData("1","testuser2000", dummyLoc2, new Date()));
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

    private static MatchMaker matchMaker = new MatchMaker();
    private static GameSessionsServer gameSessionsServer = new GameSessionsServer();

    public static void processRequest(Socket request) {

        // Add all the requests to a pool. Then whenever
        // a minor.matchmaker.RequestProcessor Thread is available it will pop one from the pool.
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
                    } catch (InterruptedException e) {
                    }
                }

                androidSocket = pool.remove(0);
            }
            try {

                inRaw = new InputStreamReader(androidSocket.getInputStream());
                outRaw = new OutputStreamWriter(androidSocket.getOutputStream());
                in = new BufferedReader(inRaw);
                out = new PrintWriter(outRaw);

                // Player is now in the Lobby
                String connectionType = in.readLine();
                String sessionId = in.readLine();
                PlayerData playerData = onPlayerConnected(sessionId);

                if (playerData != null) {

                    System.out.println("+++++++ Player " + playerData.username + " Connected: " + connectionType);

                    switch (connectionType) {

                        case "lobbySession":
                            handleConnection_lobbySession(sessionId, playerData, in, out);
                            break;

                        case "searchGame":
                            handleConnection_searchGame(sessionId, playerData, in, out, androidSocket);
                            break;
                    }
                }


            } catch (IOException e) {
                //e.printStackTrace();
                System.err.println("connection interrupted");
            }

        }
    }

    private PlayerData onPlayerConnected(String sessionId) {
        PlayerData playerData = MatchMakerServer.sessions.get(sessionId);

        if(playerData == null) {
            //TODO: No session with this id found
        }

        return playerData;
    }

    private void onPlayerDisconnected(String sessionId) {
        // remove from the searching list (
        matchMaker.removePlayer(sessionId);
    }

    public void handleConnection_lobbySession(String sessionId, PlayerData playerData, BufferedReader in, PrintWriter out) throws IOException {

        while(true) {
            String request = in.readLine();

            if(request == null) {
                System.out.println("------- Player " + playerData.username + " Disconnected");
                onPlayerDisconnected(sessionId);
                break;
            }

            System.out.println("[" + playerData.username + "] Request: " + request);

            // TODO: Process request
            switch (request) {
                case "updateLocation":
                    handleRequest_updateLocation(playerData, in, out);
                    break;

                case "requestNearbyPlayers":
                    handleRequest_requestNearbyPlayers(playerData, in, out);
                    break;
            }
        }
    }

    private void handleRequest_updateLocation(PlayerData playerData, BufferedReader in, PrintWriter out) throws IOException {

        String latitude = in.readLine();
        String longitude = in.readLine();

        try {
            System.out.println("[" + playerData.username + "] New location: "  + latitude + " " + longitude);
            playerData.location = new Location("");
            playerData.location.setLatitude(Double.parseDouble(latitude));
            playerData.location.setLongitude(Double.parseDouble(longitude));

        } catch (NumberFormatException e) {
            System.err.println("Failed to convert string(s) to double: " + latitude + " " + longitude);
        }
    }

    private void handleRequest_requestNearbyPlayers(PlayerData playerData, BufferedReader in, PrintWriter out) throws IOException {

        out.println(getNearbyPlayersString(playerData.location));
        out.flush();

        System.out.println("[" + playerData.username + "] Sent nearby players");
    }

    private String getNearbyPlayersString(Location location) {

        StringBuilder sb = new StringBuilder();

        for(Map.Entry<String, PlayerData> entry : MatchMakerServer.sessions.entrySet()) {
            PlayerData otherPlayerData = entry.getValue();

            if(Utility.isPlayerNearby(location, otherPlayerData.location)) {

                sb.append(otherPlayerData.username);
                sb.append(",");
                sb.append(otherPlayerData.location.getLatitude());
                sb.append(",");
                sb.append(otherPlayerData.location.getLongitude());
                sb.append(";");
            }
        }

        if(sb.length() == 0) {
            sb.append("Empty");
        }

        return sb.toString();
    }

    public void handleConnection_searchGame(String sessionId, PlayerData playerData, BufferedReader in, PrintWriter out, Socket socket) throws IOException {

        GameLobbySession gameLobbySession = null;

        while(true) {

            String request = null;

            try {
                request = in.readLine();
            } catch (IOException e) {
                System.err.println("searchGame connection lost");
            }

            if(request == null) {
                System.out.println("---------------- Player " + playerData.username + " Stopped searching");
                onPlayerDisconnected(sessionId);

                //TODO re-enable
//                if(gameLobbySession != null)
//                    gameLobbySession.cancelGame();
                break;
            }

            System.out.println("[" + playerData.username + "] Request: " + request);

            switch (request) {
                case "startSearch":
                    String gameType = in.readLine();

                    PlayerSession playerSession = new PlayerSession(sessionId, playerData, in, out, socket);
                    gameLobbySession = matchMaker.searchGame(playerSession, gameType); // TODO: Fix gameType

                    if(gameLobbySession == null) { // if(No other nearby player found)
                        matchMaker.addPlayer(sessionId, playerSession);
                        System.out.println("[" + playerData.username + "] Started searching for a game");

                        out.println("waitingForPlayer");
                        out.flush();
                    }
                    else { // Found nearby player!

                        System.out.println("[" + playerData.username + "] Found a game");

                        matchMaker.removePlayer(gameLobbySession.queuedPlayer.sessionId);

                        gameLobbySession.sendGameFound();
                    }
                    break;

                case "acceptGameChoice":
                    String gameId = in.readLine();
                    String gameAcceptedS = in.readLine();
                    Boolean gameAccepted = Boolean.valueOf(gameAcceptedS);
                    GameLobbySession gameLobby = MatchMaker.gameLobbies.get(gameId);

                    if(gameLobby != null) {

                        if(gameAccepted) {
                            gameLobby.playerAccepted(playerData.username);
                            if (gameLobby.allPlayersAccepted()) {

                                gameLobby.sendStartGameCommand();
                                System.out.println("Game started.................");
                                gameSessionsServer.addGameSession(gameLobby);
                                return;
                            }
                        }
                        else {
                            //gameLobby.cancelGame();
                        }
                    }

                    break;
            }
        }
    }
}



