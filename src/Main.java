import java.io.IOException;

/**
 * Created by hoi on 10/23/14.
 */
public class Main {

    public static void main(String[] args) {

        boolean enableLoginServer = false;
        boolean enableMatchmakingServer = false;
        Integer port = 0;
        Integer SSLPort = 0;

        if (args.length == 4) {
            try {
                port = Integer.parseInt(args[0]);
                SSLPort = Integer.parseInt(args[1]);
                enableLoginServer = Boolean.parseBoolean(args[2]);
                enableMatchmakingServer = Boolean.parseBoolean(args[3]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        }

        try {
            //Set up login server
            if(enableLoginServer) {
                (new LoginServer(SSLPort)).start();
            }

            //Set up matchmaking server
            if(enableMatchmakingServer) {
                (new mmServer(port)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
