package minor.game;

import java.io.*;
import java.net.Socket;

/**
 * @author Callan
 * @since 16/12/14
 */
public class ClientHandler extends Thread {
    BufferedReader in;
    PrintWriter out;

    public ClientHandler(Socket socket){
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {}
    }

    public void send(String s)
    {
        out.println(s);
        out.flush();
    }

    public String receive()
    {
        try{
            String s = in.readLine();
            return s;
        } catch (IOException e) {}
        System.out.println("receive error");
        return null;
    }
}
