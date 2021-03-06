package minor.matchmaker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import minor.Location;
import minor.Main;
import minor.Utility;
import minor.game.GameSessionsServer;

/**
 * Created by hoi on 10/24/14.
 */
public class MainServer extends Thread {

    private int serverPort;
    private ServerSocket serverSocket;

    public static HashMap<String, PlayerData> sessions;
    private static final Object sessionsLock = new Object();

    /**
     * Add session
     */
    public static void addSession(String uuid, PlayerData user) {

        //TODO: make method secure for when minor.LoginServer.java uses this method (Multithreading)
        synchronized (sessionsLock) {

            //TODO: Check if uuid is expired (?)
            PlayerData existingSessionUser = sessions.get(uuid);

            if(user == null) {
                if(existingSessionUser != null) {
                    user = existingSessionUser;
                }
            }

            sessions.put(uuid, user);
        }

        if(user != null && user.username != null) {
            System.out.println("Added new session: " + uuid + " username: " + user.username);
        }
    }

    public static boolean isValidExistingUuid(String uuid) {

        synchronized (sessionsLock) {
            return sessions.containsKey(uuid);
        }
    }


    public MainServer(int port) throws IOException {
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
    }

    public void generateDummyPlayers() {

        Location startLocation = new Location("dummy");
        startLocation.setLatitude(53.212032); startLocation.setLongitude(5.800100);
        PlayerData dummy = new PlayerData("testuser3000", startLocation, new Date());
        sessions.put("ec1d2602-0397-48f7-9bd9-599b26ac80d5", dummy);
        /**
         * TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA-TEST-DATA
         */
        Location dummyLoc = new Location("dummy");
        dummyLoc.setLatitude(startLocation.getLatitude() + 0.1); dummyLoc.setLongitude(startLocation.getLongitude() + 0.1);
        Location dummyLoc2 = new Location("dummy");
        dummyLoc2.setLatitude(startLocation.getLatitude() - 0.1); dummyLoc2.setLongitude(startLocation.getLongitude() - 0.1);

        String uuid1 = "518923af-465f-4b2b-b31d-a0c57ce0518b";//UUID.randomUUID().toString();
        String uuid2 = "9d32d79a-de11-4e72-a61c-c9996f47a8b7";//UUID.randomUUID().toString();
        sessions.put(uuid1, new PlayerData("testuser1000", dummyLoc, new Date()));
        sessions.put(uuid2, new PlayerData("testuser2000", dummyLoc2, new Date()));
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
                    Main.guiC.echo("+++++++ Player " + playerData.username + " Connected: " + connectionType);

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
        PlayerData playerData = MainServer.sessions.get(sessionId);

        if(playerData == null) {
            //TODO: No session with this id found
        }

        return playerData;
    }

    private void onPlayerDisconnected(String sessionId) {
        // remove from the searching list (
        matchMaker.removePlayer(sessionId);

        // TODO: Remove player from session list
        //MainServer.sessions.remove(sessionId);
    }


    // *****************************************************************
    //  Lobby Connection
    // *****************************************************************

    public void handleConnection_lobbySession(String sessionId, PlayerData playerData, BufferedReader in, PrintWriter out) throws IOException {

        while(true) {
            String request = in.readLine();

            if(request == null) {
                System.out.println("------- Player " + playerData.username + " Disconnected from LOBBY");
                Main.guiC.echo("------- Player " + playerData.username + " Disconnected  from LOBBY");

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
        Main.guiC.echo("[" + playerData.username + "] Sent nearby players");
    }

    private String getNearbyPlayersString(Location location) {

        StringBuilder sb = new StringBuilder();

        for(Map.Entry<String, PlayerData> entry : MainServer.sessions.entrySet()) {
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

    // *****************************************************************
    //  Matchmaking Connection
    // *****************************************************************

    public void handleConnection_searchGame(String sessionId, PlayerData playerData, BufferedReader in, PrintWriter out, Socket socket) throws IOException {

        GameLobbySession gameLobbySession = null;
        boolean isGameStarted = false;

        while(true) {

            if(isGameStarted) {
                return;
            }

            String request = null;

            try {
                request = in.readLine();
            } catch (IOException e) {
                System.err.println(playerData.username + " - searchGame connection lost");
            }

            if(request == null) {
                System.out.println("---------------- Player " + playerData.username + " Stopped searching");
                Main.guiC.echo("---------------- Player " + playerData.username + " Stopped searching");
                onPlayerDisconnected(sessionId);

                if(gameLobbySession != null)
                    gameLobbySession.cancelGame();
                break;
            }

            if(!request.equals("."))
                System.out.println("[" + playerData.username + "] SearchGame Request: " + request);

            switch (request) {
                case "startSearch":
                    String gameType = in.readLine();

                    PlayerSession playerSession = new PlayerSession(sessionId, playerData, in, out, socket);
                    gameLobbySession = matchMaker.searchGame(playerSession, gameType); // TODO: Fix gameType

                    if(gameLobbySession == null) { // if(No other nearby player found)
                        matchMaker.addPlayer(sessionId, playerSession);
                        System.out.println("[" + playerData.username + "] Started searching for a game");
                        Main.guiC.echo("[" + playerData.username + "] Started searching for a game");

                        out.println("waitingForPlayer");
                        out.flush();
                    }
                    else { // Found nearby player!

                        System.out.println("[" + playerData.username + "] Found a game");
                        Main.guiC.echo("[" + playerData.username + "] Found a game");

                        matchMaker.removePlayer(gameLobbySession.queuedPlayer.sessionId);

                        gameLobbySession.sendGameFound();
                    }
                    break;

                case "acceptGameChoice":
                    String gameId = in.readLine();
                    String gameAcceptedS = in.readLine();
                    Boolean gameAccepted = Boolean.valueOf(gameAcceptedS);
                    GameLobbySession gameLobby = MatchMaker.gameLobbies.get(gameId);

                    isGameStarted = gameAccepted;
                    if(gameLobby != null) {

                        if(gameAccepted) {
                            gameLobby.playerAccepted(playerData.username);
                            if (gameLobby.allPlayersAccepted()) {

                                System.out.println("Game started.................");
                                Main.guiC.echo("Game started.................");
                                gameSessionsServer.addGameSession(gameLobby);
                            }
                        }
                        else {
                            gameLobby.cancelGame();
                        }
                    }

                    break;
            }
        }
    }
}



