package minor.game;

import java.io.IOException;
import java.net.Socket;

import minor.matchmaker.PlayerSession;

/**
 * Created by Callan on 19/01/15.
 */
public class RockPaperScissorGameSession extends Thread {

    private String gameId;
    private boolean activeSession = true;

    PlayerSession player1;
    PlayerSession player2;

    // Game Variables.

    private final int ROCK = 1;
    private final int PAPER = 2;
    private final int SCISSOR = 3;

    private final int WINNER_PLAYER1 = 1;
    private final int WINNER_PLAYER2 = 2;
    private final int TIE = 3;

    private int choicePlayer1 = -1;
    private int choicePlayer2 = -1;


    RockPaperScissorGameSession(PlayerSession player1, PlayerSession player2, String gameId) {
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
                System.out.println("RockPaperScissorGameSession: Method Called");
                if(sA[1].equals("hasOtherPlayerChosen"))
                {
                    System.out.println("GameSession: hasOtherPlayerChosen called.");
                    hasOtherPlayerChosen(player);
                } else if(sA[1].equals("setChoice")) {
                    setChoice(player, sA[2]);
                } else if(sA[1].equals("checkWinner")) {
                    checkWinner(player, choicePlayer1, choicePlayer2);
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

    private void hasOtherPlayerChosen(int player) {
        boolean result;
        if(player == 1) {
            if(choicePlayer2 == -1) {
                result = false;
            } else {
                result = true;
            }
            player1.out.println(String.valueOf(result));
            player1.out.flush();
        }
        if(player == 2) {
            if(choicePlayer1 == -1) {
                result = false;
            } else {
                result = true;
            }
            player2.out.println(String.valueOf(result));
            player2.out.flush();;
        }

    }

    private void setChoice(int player, String choiceToSet) {
        String s;
        int choice = Integer.valueOf(choiceToSet);
        if(player == 1) {
            System.out.println("Player 1 choice set to: " + choice);
            choicePlayer1 = choice;
        } else if (player == 2) {
            System.out.println("Player 2 choice set to: " + choice);
            choicePlayer2 = choice;
        }
    }

    private void checkWinner(int player, int choice1, int choice2) {
        int winner = -1;
        switch (choice1){
            case ROCK:
                switch(choice2) {
                    case ROCK:
                        winner = TIE;
                        break;
                    case PAPER:
                        winner = WINNER_PLAYER2;
                        break;
                    case SCISSOR:
                        winner = WINNER_PLAYER1;
                        break;
                }
                break;
            case PAPER:
                switch(choice2) {
                    case ROCK:
                        winner = WINNER_PLAYER1;
                        break;
                    case PAPER:
                        winner = TIE;
                        break;
                    case SCISSOR:
                        winner = WINNER_PLAYER2;
                        break;
                }
                break;
            case SCISSOR:
                switch(choice2) {
                    case ROCK:
                        winner = WINNER_PLAYER2;
                        break;
                    case PAPER:
                        winner = WINNER_PLAYER1;
                        break;
                    case SCISSOR:
                        winner = TIE;
                        break;
                }
                break;
        }

        switch(winner) {
            case WINNER_PLAYER1:
                if(player == 1) {
                    player1.out.println("You won!");
                    player1.out.flush();
                } else if (player == 2) {
                    player2.out.println("You lost!");
                    player2.out.flush();
                }
                break;
            case WINNER_PLAYER2:
                if(player == 1) {
                    player1.out.println("You lost!");
                    player1.out.flush();
                } else if(player == 2) {
                    player2.out.println("You win!");
                    player2.out.flush();
                }
                break;
            case TIE:
                if(player == 1) {
                    player1.out.println("Tie!");
                    player1.out.flush();
                } else if(player == 2) {
                    player2.out.println("Tie!");
                    player2.out.flush();
                }
                break;
        }
    }
}
