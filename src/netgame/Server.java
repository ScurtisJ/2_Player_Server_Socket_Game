package src.netgame;

import java.net.Socket;
import java.net.ServerSocket;


public class Server {

    // Sockets for two players
    private Socket socket1 = null; 
    private Socket socket2 = null;
    public static int port = 10000;
    public static int game = 1;

    //how many games are currently running
    public static int activeGames = 0;
    
    public static ServerSocket serverSocket; // Server socket to listen for incoming connections (We set this up in main to listen on port)
    
        public Server(ServerSocket serverSocket) {
            Server.serverSocket = serverSocket;
        }
    
        public void runServer() throws Exception {
        System.out.println("\nSERVER STARTED || LISTENING FOR PLAYERS\n");

        while (!serverSocket.isClosed()) {
            socket1 = serverSocket.accept(); // Accept connection for player 1
            System.out.println("GAME " + game + " CREATED");

            activeGames++;

            socket2 = serverSocket.accept(); // Accept connection for player 2
            GameSession gameSession = new GameSession(socket1, socket2, game); // Create a new game session for the two players

            Thread t = new Thread(gameSession); // Create a new thread for the game session
            t.start(); // Start the game session 

            game++;
            
            }


        }

        public static void main(String[] args) throws Exception {
            ServerS