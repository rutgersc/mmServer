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

    String player1Name;
    Socket player1Socket;

    String player2Name;
    Socket player2Socket;

    private String playerturn = "";
    private boolean activeSession = true;

    GameSession(Socket player1Socket, Socket player2Socket, String gameId) {
        this.player1Socket = player1Socket;
        this.player2Socket = player2Socket;
        this.gameId = gameId;
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


                    String s1 = player1.receive();
                    System.out.println(s1);
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
                if(sA[1].equals("setName"))
                {
                    System.out.println("GameSession: setName Called");
                    setPlayerName(sA[2]);
                }
            }
            else if (sA[0].equals("Print"))
            {
                System.out.println("GameSession: Print Called");
            }
        }
    }

    private void setPlayerName(String s)
    {
        if(player1Name == null)
        {
            player1Name = s;
            Echo("GameSession: Set Player1Name to " + s);
            GameSessionsServer.addUser(s);
        }
        else if(player2Name == null)
        {
            player2Name = s;
            Echo("GameSession: Set Player2Name to " + s);
            GameSessionsServer.addUser(s);
        }
        else{
            Echo("Setname ERROR");
        }
    }
}