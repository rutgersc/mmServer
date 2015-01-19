/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minor.game;

import minor.matchmaker.PlayerSession;

import java.io.*;
import java.net.Socket;
import static minor.game.GameSessionsServer.Echo;


class TicTacToeGameSession extends Thread {

    private TicTacToe game;
    private String gameId;

    PlayerSession player1;
    String player1Name;
    Socket player1Socket;

    PlayerSession player2;
    String player2Name;
    Socket player2Socket;

    private char[][] board;
    boolean player1Set = false;
    boolean player2Set = false;
    private int playerTurn = 1;
    private boolean activeSession = true;
    private int ID = 0;

    TicTacToeGameSession(PlayerSession player1, PlayerSession player2, String gameId) {
        this.player1 = player1;
        this.player2 = player2;
        this.gameId = gameId;
        game = new TicTacToe();
        game.initializeBoard();
        this.board = game.getBoard();
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

    private void parseCommand(int player, String s)
    {
        String[] sA = s.split("\\s+");
        if(sA.length >= 1)
        {
            if(sA[0].equals("Method"))
            {
                System.out.println("TicTacToeGameSession: Method Called");
                if(sA[1].equals("getBoard"))
                {
                    System.out.println("TicTacToeGameSession: getBoard Called");
                    sendBoard(player);

                } else if(sA[1].equals("getID"))
                {
                    System.out.println("TicTacToeGameSession: getID Called");

                }else if(sA[1].equals("getTurn"))
                {
                    System.out.println("TicTacToeGameSession: getTurn Called");
                    sendTurn(player);
                }
                else if(sA[1].equals("setBoard"))
                {
                    System.out.println("TicTacToeGameSession: setBoard Called");
                    receiveBoard(player);
                }
                else if(sA[1].equals("setTurn"))
                {
                    System.out.println("TicTacToeGameSession: setTurn Called");
                    setTurn(player);
                } else if(sA[1].equals("getPlayer"))
                {
                    System.out.println("TicTacToeGameSession: getPlayer Called");
                    sendPlayer(player);
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

    private void sendBoard(int player) {

        if (player == 1) {
            player1.out.println(game.printBoardString());
            player1.out.flush();
        } else if (player == 2) {
            player2.out.println(game.printBoardString());
            player2.out.flush();
        }
    }

    private void receiveBoard(int player) {
        String s = null;
        if (player == 1) {
            try {
                s = player1.in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if( player == 2) {
            try {
                s = player2.in.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if( s != null) {
            System.out.println("Board received is: " + s);
            game.setBoard(s);
        } else {
            System.out.println("RECEIVEBOARDERROR");
        }
    }

    private void sendTurn(int player) {
        if (player == 1) {
            if(playerTurn == 1) {
                player1.out.println("true");
            } else if (playerTurn == 2) {
                player1.out.println("false");
            }
            player1.out.flush();
        } else if (player == 2) {
            if(playerTurn == 1) {
                player2.out.println("false");
            } else if (playerTurn == 2) {
                player2.out.println("true");
            }
            player2.out.flush();
        }
    }

    private void setTurn(int player) {
        if (player == 1) {
            playerTurn = 2;
        } else if (player == 2) {
            playerTurn = 1;
        }
    }

    private void sendPlayer(int player) {
        if (player == 1) {
            player1.out.println("1");
            player1.out.flush();
        } else if (player == 2) {
            player2.out.println("2");
            player2.out.flush();
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
}