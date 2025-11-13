package src.netgame;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Server {

    // Sockets for two players
    private Socket socket1 = null; 
    private Socket socket2 = null;

    // I/O streams for player 1
    private BufferedReader in1 = null;
    private BufferedWriter out1 = null;

    // I/O streams for player 2
    private BufferedReader in2 = null;
    private BufferedWriter out2 = null;

    private String player1Id;
    private String player2Id;

    private String msg;


    private ServerSocket serverSocket; // Server socket to listen for incoming connections (We set this up in main to listen on port 24175)

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void runServer() throws Exception {
        System.out.println("The Server is 