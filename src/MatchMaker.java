import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MatchMaker {

    public static HashMap<String, PlayerSession> playersSearching;

    MatchMaker() {

        playersSearching = new HashMap<>();
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
            gameLobbySession = new GameLobbySession(playerSession, queuedPlayer);
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

    PlayerSession queuedPlayer, newestPlayer;

    GameLobbySession(PlayerSession queuedPlayer, PlayerSession newestPlayer) {
        this.queuedPlayer = queuedPlayer;
        this.newestPlayer = newestPlayer;
    }

    public void sendGameFound() {
        queuedPlayer.out.println("gameFound");
        newestPlayer.out.println("gameFound");
    }

    public boolean getPlayersConfirmation() {
        boolean confirmed = false;

        try {
            String player1 = queuedPlayer.in.readLine();
            String player2 = newestPlayer.in.readLine();

            confirmed = (player1.equals("accept") && player2.equals("accept"));

        } catch (IOException e) {
            System.out.println("One or both of the players disconnected or declined");
        } catch (NullPointerException e) {
            System.out.println("One or Both of the players disconnected");
        }

        return confirmed;
    }

    public void sendStartGameCommand() {
        //TODO

        queuedPlayer.out.println("startGame");
        newestPlayer.out.println("startGame");
    }
}
