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
        System.out.println("The Server is now Running | 0/2 players connected");

        socket1 = serverSocket.accept(); // Accept connection for player 1
        in1 = new BufferedReader (new InputStreamReader(socket1.getInputStream()));
        out1 = new BufferedWriter (new OutputStreamWriter(socket1.getOutputStream()));

        out1.write("You are Player 1"); //* Notify player 1 of their role (Once the server accepts a connection from the client, it should inform the client which player (player one or player two) that he/she is. )
        out1.newLine();
        out1.flush();
        
        System.out.println("Player 1 has connected | 1/2 players connected");

        socket2 = serverSocket.accept(); // Accept connection for player 2
        in2 = new BufferedReader (new InputStreamReader(socket2.getInputStream()));
        out2 = new BufferedWriter (new OutputStreamWriter(socket2.getOutputStream()));

        out2.write("You are Player 2"); //* Notify player 2 of their role (Once the server accepts a connection from the client, it should inform the client which player (player one or player two) that he/she is. )
        out2.newLine();
        out2.flush();

        System.out.println("Player 2 has connected | 2/2 players connected");

        player1Id = in1.readLine(); //Read username from player 1
        player2Id = in2.readLine(); //Read username from player 2


        //* Once the server collects the names, it should print the statement “<player one ID> vs. <player two ID>: Game start” */
        System.out.println(player1Id + " vs " + "" + player2Id + ":" + " Game Start!");

        out1.write("ROUND 1 START");
        out1.newLine();
        out1.write("Enter your number for Round 1:");
        out1.newLine();
        out1.flush();
        
        out2.write("ROUND 1 START");
        out2.newLine();
        out2.write("Enter your number for Round 1:");
        out2.newLine();
        out2.flush();
        //* Then the server should send a request to ask the two players to send in the first number.


        int p1rd1 = Integer.parseInt(in1.readLine()); // Read number from player 1
        int p2rd1 = Integer.parseInt(in2.readLine()); // Read number from player 2

        int round1p1score = 0;
        int round1p2score = 0;

        if (GameUtils.legalMoveP1(p1rd1) && GameUtils.legalMoveP2(p2rd1)) {
            round1p2score = GameUtils.HCF(p1rd1, p2rd1);
            round1p2score = GameUtils.LCM(p1rd1, p2rd1);
            }
            else{if (!GameUtils.legalMoveP1(p1rd1) && GameUtils.legalMoveP2(p2rd1)) {
                round1p1score = 0;
                round1p2score = 100;

                }else if (GameUtils.legalMoveP1(p1rd1) && !GameUtils.legalMoveP2(p2rd1)) {
                    round1p1score = 100;
                    round1p2score = 0;
                    }
                }
        
        int p1Record = 0;
        int p2Record = 0;

        if (round1p1score > round1p2score){
            p1Record = 1; // Player 1 wins
            p2Record = -1; // Player 2 loses
        } else if (round1p1score < round1p2score) {
            p1Record = -1; // Player 1 loses
            p2Record = 1; // Player 2 wins
        }

        //* The server should then print the result of round 1: including the number selected, and the score of each player 
        msg = "Round 1 Results\n===========\n"
            + player1Id + " scored: " + round1p1score + " with number: " + p1rd1 + "\n"
            + player2Id + " scored: " + round1p2score + " with number: " + p2rd1;
        
        //Send results to player 1 
        out1.write(msg);
        out1.newLine();
        out1.flush();

        //Send results to player 2
        out2.write(msg);
        out2.newLine();
        out2.flush();
        
        System.out.println(msg);

        out1.write("ROUND 2 START");
        out1.newLine();
        out1.write("Enter your number for Round 2:");
        out1.newLine();
        out1.flush();
        
        out2.write("ROUND 2 START");
        out2.newLine();
        out2.write("Enter your number for Round 2:");
        out2.newLine();
        out2.flush();
        
        int p1rd2 = Integer.parseInt(in1.readLine()); // Read Round 2 number from player 1
        int p2rd2 = Integer.parseInt(in2.readLine()); // Read Round 2 number from player 2

        int round2p1score = 0;
        int round2p2score = 0;

        // 1) Same-as-round-1 checks
        boolean p1Same = (p1rd2 == p1rd1);
        boolean p2Same = (p2rd2 == p2rd1);

        if (p1Same && !p2Same) {
        // P1 reused number, P2 did not
            round2p1score = 0;
            round2p2score = 100;

        } else if (!p1Same && p2Same) {
        // P2 reused number, P1 did not
            round2p1score = 100;
            round2p2score = 0;

        } else if (p1Same && p2Same) {
        // both reused their number
            round2p1score = 0;
            round2p2score = 0;

        } else {
            // 2) Normal Round 2 legality + scoring (using rd2 values)

            boolean p1LegalR2 = GameUtils.legalMoveP1(p1rd2);
            boolean p2LegalR2 = GameUtils.legalMoveP2(p2rd2);

            if (p1LegalR2 && p2LegalR2) {
                round2p1score = GameUtils.HCF(p1rd2, p2rd2);
                round2p2score = GameUtils.LCM(p1rd2, p2rd2) % 10; // last digit

            } else if (!p1LegalR2 && p2LegalR2) {
                round2p1score = 0;
                round2p2score = 100;

            } else if (p1LegalR2 && !p2LegalR2) {
                round2p1score = 100;
                round2p2score = 0;

            } else {
                // both illegal
                round2p1score = 0;
                round2p2score = 0;
            }
        }


        int totalP1score = round1p1score + round2p1score;
        int totalP2score = round1p2score + round2p2score;

        int p1Result;
        int p2Result;

        if (totalP1score > totalP2score) {
            p1Result = 1;
            p2Result = -1;
        } else if (totalP1score < totalP2score) {
            p1Result = -1;
            p2Result = 1;
        } else {
            p1Result = 0;
            p2Result = 0;
        }


        //*Then the server should calculate the score for round 2, and send the following tp the players
        // - The score of round 2 for each player
        // - The total score for each player
        // - Whether the player win/lose/draw the match (sending 1/-1/0 respectively) 

        msg = "Round 2 Results\n===========\n"
            + player1Id + " scored: " + round2p1score + " with number: " + p1rd2 + " | Total Score:" + totalP1score + " | Match Result: " + p1Result + "\n"
            + player2Id + " scored: " + round2p2score + " with number: " + p2rd2 + " | Total Score:" + totalP2score + " | Match Result: " + p2Result;
        
        //Send results to player 1 
        out1.write(msg);
        out1.newLine();
        out1.flush();

        //Send results to player 2
        out2.write(msg);
        out2.newLine();
        out2.flush();

        System.out.println(msg);
        
        if(p1Result == 1 ) System.out.println("(" + player1Id +") Player 1 Wins!");
        else if (p2Result == 1) System.out.println("(" + player1Id +") Player 2 Wins!");
        else System.out.println("It's a Draw!");

        // Close all connections
        in1.close();
        out1.close();
        socket1.close();
        in2.close();
        out2.close();
        socket2.close();
        serverSocket.close(); //* The server should also quit.     
    }

    
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(24175); // * The server should be started at the background, and it will listen for client connecting. (Use port 24175 for this program)
        Server server = new Server(serverSocket); // Creates a new instance of the Server class
        server.runServer();
        }
    }
    

