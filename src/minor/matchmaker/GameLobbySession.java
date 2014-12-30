package minor.matchmaker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameLobbySession {

    String uniqueGameId;
    public PlayerSession queuedPlayer;
    PlayerSession newestPlayer;
    int playersAccepted = 0;
    boolean cancellingGame = false;
    private boolean isCancelled = false;

    public PlayerSession getPlayer1() {
        return queuedPlayer;
    }
    public PlayerSession getPlayer2() {
        return newestPlayer;
    }
    public String getGameId() {
        return uniqueGameId;
    }

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
