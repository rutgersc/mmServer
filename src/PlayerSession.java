import java.io.BufferedReader;
import java.io.PrintWriter;

public class PlayerSession {

    String sessionId;
    PlayerData playerData;
    BufferedReader in;
    PrintWriter out;

    PlayerSession(String sessionId, PlayerData playerData, BufferedReader in, PrintWriter out) {
        this.playerData = playerData;
        this.in = in;
        this.out = out;
    }
}
