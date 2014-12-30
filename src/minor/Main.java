package minor;

import java.io.IOException;

import minor.game.GameSessionsServer;
import minor.matchmaker.MatchMakerServer;
import minor.gui.*;

/**
 * Created by hoi on 10/23/14.
 */
public class Main {

    public static GUIController guiC;
    public static GameSessionsServer gameSessionsServer;

    public static Integer port_SSL_LOGIN;
    public static Integer port_MATCHMAKING;

    public static LoginServer loginServer;
    public static MatchMakerServer matchMakerServer;

    public static void main(String[] args) {

        boolean enableLoginServer = false;
        boolean enableMatchmakingServer = false;
        boolean enableGUI = true;

        if (args.length == 4) {
            try {
                port_MATCHMAKING = Integer.parseInt(args[0]);
                port_SSL_LOGIN = Integer.parseInt(args[1]);
                enableLoginServer = Boolean.parseBoolean(args[2]);
                enableMatchmakingServer = Boolean.parseBoolean(args[3]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        }


        //Set up login server
        if (enableLoginServer) {
            startLoginServer();
        }

        //Set up matchmaking server
        if (enableMatchmakingServer) {
            startMatchmakingServer();
        }

        if (enableGUI) {
            guiC = new GUIController();
            guiC.initGUI();
            guiC.echo("Gui initialized. Server is Idle.");
            //guiC.echo(String.valueOf(Thread.activeCount()));
            guiC.startUpdatingGUI();
        }
    }

    public static void startLoginServer() {
        try {
            loginServer = new LoginServer(port_SSL_LOGIN);
            loginServer.start();
            guiC.echo("Started login server");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startMatchmakingServer()  {
        try {
            matchMakerServer = new MatchMakerServer(port_MATCHMAKING);
            matchMakerServer.start();
            guiC.echo("Started matchmaking server");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
