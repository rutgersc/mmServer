package minor.matchmaker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerSession {

    public String sessionId;
    public PlayerData playerData;
    public BufferedReader in;
    public PrintWriter out;
    public Socket socket;

    PlayerSession(String sessionId, PlayerData playerData, BufferedReader in, PrintWriter out, Socket socket) {
        this.sessionId = sessionId;
        this.playerData = playerData;
        this.in = in;
        this.out = out;
        this.socket = socket;
    }

    public void closeConnection() throws IOException {

        if(in != null)in.close();
        if(out != null)out.close();
        if(socket != null)socket.close();
    }
}
