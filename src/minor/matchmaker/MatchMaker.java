package minor.matchmaker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import minor.Utility;

public class MatchMaker {

    public static HashMap<String, PlayerSession> playersSearching;
    public static Map<String, GameLobbySession> gameLobbies;

    public MatchMaker() {

        playersSearching = new HashMap<>();
        gameLobbies = new ConcurrentHashMap<>();
    }

    public synchronized GameLobbySession searchGame(PlayerSession playerSession, String gameType) {

        GameLobbySession gameLobbySession = null;
        PlayerSession queuedPlayer = null;

        for (Map.Entry<String, PlayerSession> entry : playersSearching.entrySet()) {
            PlayerSession otherPlayerSession = entry.getValue();

            if(Utility.isPlayerNearby(playerSession.playerData.location, otherPlayerSession.playerData.location)) {
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

