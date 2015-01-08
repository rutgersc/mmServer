/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minor.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import minor.Main;
import minor.matchmaker.GameLobbySession;
import minor.matchmaker.PlayerSession;

/**
 *
 * @author Callan
 */
public class GameSessionsServer extends Thread {

    private static int port = 5000;

    private static ArrayList<String> userList;
    private static ArrayList<GameSession> gameSessionList;
    private static int IDCounter = 0;
    
    public GameSessionsServer() {
        Echo("Server initialized");
        userList = new ArrayList<>();
        gameSessionList = new ArrayList<>();
    }

    public synchronized void addGameSession(GameLobbySession lobbySession) {

        PlayerSession player1 = lobbySession.getPlayer1();
        PlayerSession player2 = lobbySession.getPlayer2();

        System.out.println("GameId: " + lobbySession.getGameId());
        System.out.println("user 1: " + player1.playerData.username);
        System.out.println("user 2: " + player1.playerData.username);

        Main.guiC.echo("GameId: " + lobbySession.getGameId());
        Main.guiC.echo("user 1: " + player1.playerData.username);
        Main.guiC.echo("user 2: " + player1.playerData.username);


        //TODO <----------------------- gamesessie wordt hier aangemaakt
        //TODO <-----------------------
        //TODO <-----------------------
        //TODO <-----------------------

        Socket player1Socket = player1.socket;
        Socket player2Socket = player2.socket;
        String gameId = lobbySession.getGameId();

        GameSession newGameSession = new GameSession(player1.socket, player2.socket, gameId);
        // newGameSession.run(); // Runnen?
        //TODO: toevoegen aan sessie lijst i think

        //TODO <-----------------------
        //TODO <-----------------------
        //TODO <----------------------- gamesessie wordt hier aangemaakt

    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int portToSet) {
        port = portToSet;
    }

    // Method to get the IP Address of the Host.
     public static String getIp() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
     
     public static void Echo(String s){
        Main.guiC.echo(s);
    }


    public static void addUser(String s) {
        userList.add(s);
    }
    
    public static ArrayList<String> getUserList() {
        return userList;
    }
     
     
}
