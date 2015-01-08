/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minor.game;

import java.io.*;
import java.net.Socket;
import static minor.game.GameSessionsServer.Echo;


class GameSession extends Thread {

    private String gameId;
    private boolean activeSession = true;
    private String playerTurn = "";

    String player1Name;
    Socket player1Socket;

    String player2Name;
    Socket player2Socket;


    GameSession(Socket player1Socket, Socket player2Socket, String gameId, String p1Name, String p2Name) {
        this.player1Socket = player1Socket;
        this.player2Socket = player2Socket;
        this.gameId = gameId;
        this.player1Name = p1Name;
        this.player2Name = p2Name;
        playerTurn = player1Name;
    }

    @Override
    public void run() {
        try {

            ClientHandler player1 = new ClientHandler(player1Socket);
            ClientHandler player2 = new ClientHandler(player2Socket);

/*
            //init game
            player1.send("Method getName");
            player2.send("Method getName");
            String s1 = player1.receive();
            String s2 = player2.receive();
            parseCommand(s1);
            parseCommand(s2);

*/

            // Gameloop
            do {
                try {
                    Thread.sleep(1000);

                    player1.send("TESTTTT");
                    player2.send("TESTTT");
                    //String s1 = player1.receive();
                    //System.out.println(s1);
                    //parseCommand(s1);
                    //String speler2Reaksie = in_2.readLine();

                    //System.out.println("Speler1: " + speler1Reaksie);
                    //System.out.println("Speler2: " + speler2Reaksie);

                } catch (Exception e) {
                }
            } while (activeSession);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseCommand(String s)
    {
        String[] sA = s.split("\\s+");
        if(sA.length >= 1)
        {
            if(sA[0].equals("Method"))
            {
                System.out.println("GameSession: Method Called");
                if(sA[1].equals("checkTurn"))
                {
                    System.out.println("GameSession: checkTurn Called");
                    String output = checkTurn();

                }
            }
            else if (sA[0].equals("Print"))
            {
                System.out.println("GameSession: Print Called");
            }
        }
    }

    private void getPlayer() {

    }

    private String checkTurn()
    {
        return playerTurn;
    }
}