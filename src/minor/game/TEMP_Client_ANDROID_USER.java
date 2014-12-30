package minor.game;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TEMP_Client_ANDROID_USER extends Thread {

    String name;
    Socket socket;
    boolean stayAlive = true;

    InputStream inputStream;
    OutputStream outputStream;
    InputStreamReader inputStreamChar;
    OutputStreamWriter outputStreamChar;
    BufferedReader in;
    PrintWriter out;

    public TEMP_Client_ANDROID_USER(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
    }

    public void run() {

        try {

            // // http://www.javaworld.com/article/2077322/core-java/sockets-programming-in-java-a-tutorial.html

            inputStream = socket.getInputStream();                 // De laagste laag van een stream, deze werkt alleen met bytes
            inputStreamChar = new InputStreamReader(inputStream);  // Wrapper om de input/output-stream, zet chars om naar bytes
            in = new BufferedReader(inputStreamChar);              // Nog weer een wrapper. Deze implementeert een buffer voor max efficiency

            outputStream = socket.getOutputStream();               // Zelfde als hierboven
            outputStreamChar = new OutputStreamWriter(outputStream);
            out = new PrintWriter(outputStreamChar);


            //Get response from server
            String response;
            do{
                while ((response = in.readLine()) != null)
                {
                    System.out.println(this.name + " " + response);
                    parseCommand(response);
                    try
                    {
                        Thread.sleep(1000);
                        out.println("send");
                        out.flush();
                        System.out.println(this.name + ": sleeping 1000");
                    } catch (Exception e) { e.printStackTrace(); }

                }

            } while(stayAlive);


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Connection broken with server.");
        }
    }

    private void parseCommand(String string)
    {
        String[] sA = string.split("\\s+");
        if(sA.length >= 1)
        {
            if(sA[0].equals("Method"))
            {
                System.out.println(name + ": Method Called");
                if(sA[1].equals("getName"))
                {
                    sendName();
                }
            }
            else if (sA[0].equals("Print"))
            {
                System.out.println("Print Called");
            }
        }
    }

    private void sendName()
    {
        out.println("Method setName " + this.name);
        out.flush();
    }

}