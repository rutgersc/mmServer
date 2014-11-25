import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MatchMaker {

    public static HashMap<String, PlayerSession> playersSearching;
    public static Map<String, GameLobbySession> gameLobbies;

    MatchMaker() {

        playersSearching = new HashMap<>();
        gameLobbies = new ConcurrentHashMap<>();
    }

    public synchronized GameLobbySession searchGame(PlayerSession playerSession, String gameType) {

        GameLobbySession gameLobbySession = null;
        PlayerSession queuedPlayer = null;

        for (Map.Entry<String, PlayerSession> entry : playersSearching.entrySet()) {
            PlayerSession otherPlayerSession = entry.getValue();

            if(Utility.isPlayerNearby(playerSession.playerData.location,  otherPlayerSession.playerData.location)) {
                queuedPlayer = otherPlayerSession;
                break;
            }
        }

        if(queuedPlayer != null) {
            gameLobbySession = new GameLobbySession(queuedPlayer, playerSession);
            gameLobbies.put(gameLobbySession.uniqueGameId, gameLobbySession);
        }

        return gameLobbySession;
    }

    public synchronized void addPlayer(String sessionId, PlayerSession playerSession) {
        //TODO: Check if sessionId is already used
        playersSearching.put(sessionId, playerSession);
        System.out.println("Players searching: " + playersSearching.size());
    }

    public synchronized PlayerSession removePlayer(String sessionId) {
        PlayerSession removed = playersSearching.remove(sessionId);
        System.out.println("Players searching: " + playersSearching.size());
        return removed;
    }
}

class GameLobbySession {

    String uniqueGameId;
    PlayerSession queuedPlayer, newestPlayer;
    int playersAccepted = 0;
    boolean cancellingGame = false;
    private boolean isCancelled = false;

    GameLobbySession(PlayerSession queuedPlayer, PlayerSession newestPlayer) {
        this.queuedPlayer = queuedPlayer;
        this.newestPlayer = newestPlayer;
        this.uniqueGameId = generateGameId(queuedPlayer.playerData, newestPlayer.playerData);
        System.out.println("Game id: " + uniqueGameId);
    }

    private String generateGameId(PlayerData playerData1, PlayerData playerData2) {

        String a = playerData1.username.substring(0, 2);
        String b = playerData2.username.substring(0, 2);

        String date = new SimpleDateFormat("ddMMyyyy-HHmmss").format(new Date());

        return date + "-" + a + "-" + b;
    }

    public void sendGameFound() {

        queuedPlayer.out.println("gameFound");
        queuedPlayer.out.println(uniqueGameId);
        queuedPlayer.out.flush();

        newestPlayer.out.println("gameFound");
        newestPlayer.out.println(uniqueGameId);
        newestPlayer.out.flush();
    }

    public synchronized void playerAccepted(String playername) {
        playersAccepted += 1;
    }

    public synchronized boolean allPlayersAccepted() {
        if(playersAccepted == 2) { //TODO: Don't use int here please
            return true;
        }
        return false;
    }

    public synchronized void cancelGame() throws IOException {
        if(!isCancelled) {
            queuedPlayer.out.println("cancelGame");
            newestPlayer.out.println("cancelGame");
            queuedPlayer.out.flush();
            newestPlayer.out.flush();

            queuedPlayer.closeConnection();
            newestPlayer.closeConnection();

            System.out.println("[" + newestPlayer.playerData.username + "] Cancelled a game with " + queuedPlayer.playerData.username + " gameId: " + uniqueGameId);
            MatchMaker.gameLobbies.remove(this.uniqueGameId);
        }

        isCancelled = true;
    }

    public void sendStartGameCommand() {
        queuedPlayer.out.println("startGame");
        newestPlayer.out.println("startGame");
        queuedPlayer.out.flush();
        newestPlayer.out.flush();

        System.out.println("[" + newestPlayer.playerData.username + "] Started a game with " + queuedPlayer.playerData.username + " gameId: " + uniqueGameId);
        MatchMaker.gameLobbies.remove(this.uniqueGameId);
    }



    public void sendOtherPlayerGameDeclined() {

    }
}
