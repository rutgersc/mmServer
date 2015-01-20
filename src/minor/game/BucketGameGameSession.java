package minor.game;

/**
 * Created by Callan on 20/01/15.
 */

import java.io.IOException;
import java.net.Socket;

import minor.matchmaker.PlayerSession;

/**
 * Created by Callan on 19/01/15.
 */
public class BucketGameGameSession extends Thread {

    private String gameId;
    private boolean activeSession = true;

    PlayerSession player1;
    PlayerSession player2;

    // Game Variables.

    private int scorePlayer1 = -1;
    private int scorePlayer2 = -1;


    BucketGameGameSession(PlayerSession player1, PlayerSession player2, String gameId) {
        this.player1 = player1;
        this.player2 = player2;
        this.gameId = gameId;

    }

    @Override
    public void run() {
        try {

            String s1;
            String s2;

            // Gameloop
            do {
                try {
                    Thread.sleep(1000);

                    s1 = player1.in.readLine();
                    parseCommand(1, s1);
                    s2 = player2.in.readLine();
                    parseCommand(2, s2);


                } catch (Exception e) {
                }
            } while (activeSession);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void requestParser(int player, int numberOfRequests) throws IOException {
        if(player != 1 && player != 2 && numberOfRequests < 1)
            System.out.println("REQUESTPARSER: Format error.");
        String s;
        for(int i = 0; i < numberOfRequests; i++) {
            if(player == 1) {
                s = player1.in.readLine();
                parseCommand(1, s);
            } else if(player == 2) {
                s = player2.in.readLine();
                parseCommand(2, s);
            }
        }
    }

    private void parseCommand(int player, String s)
    {
        String[] sA = s.split("\\s+");
        if(sA.length >= 1)
        {
            if(sA[0].equals("Method"))
            {
                System.out.println("BucketGameGameSession: Method Called");
                if(sA[1].equals("setScore"))
                {
                    System.out.println("GameSession: hasOtherPlayerChosen called.");
                    setScore(player, Integer.valueOf(sA[2]));
                } else if(sA[1].equals("getScore")) {
                    getScore(player);
                } else if(sA[1].equals("checkWinner")) {
                    //checkWinner(player, choicePlayer1, choicePlayer2);
                }
            } else if (sA[0].equals("Print"))
            {
                System.out.println("TicTacToeGameSession: Print Called");
                System.out.println(s);
            } else if (sA[0].equals("Request"))
            {
                System.out.println(sA[1] + " Requests made.");
                try {
                    requestParser(player, Integer.parseInt(sA[1]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void send(int player, String s) {
        if(player == 1) {
            player1.out.println(s);
            player1.out.flush();
        } else if(player == 2) {
            player2.out.println(s);
            player2.out.flush();
        }
    }

    private void setScore(int player, int score) {
        if(player == 1) {
            scorePlayer1 = score;
            System.out.println("Player 1 score updated.");
        } else if(player == 2) {
            scorePlayer2 = score;
            System.out.println("Player 2 score updated.");
        }
    }

    private void getScore(int player) {
        if(player == 1) {
            send(player, String.valueOf(scorePlayer2));
        } else if(player == 2) {
            send(player, String.valueOf(scorePlayer1));
        }
    }

}
