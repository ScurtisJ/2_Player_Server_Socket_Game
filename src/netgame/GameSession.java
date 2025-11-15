package src.netgame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.io.IOException;

public class GameSession implements Runnable{


    private Socket socket1;
    private Socket socket2;

    private BufferedReader in1;
    private BufferedWriter out1;

    private BufferedReader in2;
    private BufferedWriter out2;

    private String player1Id; // Player 1's ID
    private String player2Id; // Player 2's ID

    private int p1rd1; // Player 1's number for round 1
    private int p2rd1; // Player 2's number for round 1
    private int p1rd2; // Player 1's number for round 2
    private int p2rd2; // Player 2's number for round 2

    private int round1p1score = 0; // Score for player 1 in round 1
    private int round1p2score = 0; // Score for player 2 in round 1
    private int round2p1score = 0; // Score for player 1 in round 2
    private int round2p2score = 0; // Score for player 2 in round 2

    private int p1Record = 0; // Win/Lose/Draw record for player 1
    private int p2Record = 0; // Win/Lose/Draw record for player 2

    private String msg; // Big Block Message to be sent to players

    private boolean p1Same; // Player 1 reused number in round 2
    private boolean p2Same; // Player 2 reused number in round 2

    private boolean p1LegalR2; // Player 1's round 2 legality
    private boolean p2LegalR2; // Player 2's round 2 legality

    private int totalP1score; // Total score for player 1
    private int totalP2score; // Total score for player 2

    private int p1Result; // Final result for player 1
    private int p2Result; // Final result for player 2

    private int game;

    public static String gameWinner;

    public GameSession(Socket socket1, Socket socket2, int game){
        this.socket1 = socket1;
        this.socket2 = socket2;
        this.game = game;
    }

    @Override
    public void run() {
        try {
            RunGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void PreGame() throws IOException{

        in1 = new BufferedReader (new InputStreamReader(socket1.getInputStream()));
        out1 = new BufferedWriter (new OutputStreamWriter(socket1.getOutputStream()));

        in2 = new BufferedReader (new InputStreamReader(socket2.getInputStream()));
        out2 = new BufferedWriter (new OutputStreamWriter(socket2.getOutputStream()));

        out1.write("You are Player 1"); //* Notify player 1 of their role (Once the server accepts a connection from the client, it should inform the client which player (player one or player two) that he/she is. )
        out1.newLine();
        out1.flush();

        out2.write("You are Player 2"); //* Notify player 2 of their role (Once the server accepts a connection from the client, it should inform the client which player (player one or player two) that he/she is. )
        out2.newLine();
        out2.flush();
        player1Id = in1.readLine(); //Read username from player 1
        player2Id = in2.readLine(); //Read username from player 2


        //* Once the server collects the names, it should send the statement “<player one ID> vs. <player two ID>: Game start” */
        out1.write(player1Id + " vs " + "" + player2Id + ":" + " Game Start!");
        out1.newLine();
        out1.flush();

        out2.write(player1Id + " vs " + "" + player2Id + ":" + " Game Start!");
        out2.newLine();
        out2.flush();

    }

    private void Round1() throws IOException{
        out1.write("ROUND 1 START");
        out1.newLine();
        out1.flush();

        out1.write("Enter your number for Round 1: ");
        out1.newLine();
        out1.flush();
        
        out2.write("ROUND 1 START");
        out2.newLine();
        out2.flush();

        out2.write("Enter your number for Round 1: ");
        out2.newLine();
        out2.flush();
        //* Then the server should send a request to ask the two players to send in the first number.


        p1rd1 = Integer.parseInt(in1.readLine()); // Read number from player 1
        p2rd1 = Integer.parseInt(in2.readLine()); // Read number from player 2

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

        if (round1p1score > round1p2score){
            p1Record = 1; // Player 1 wins
            p2Record = -1; // Player 2 loses
        } else if (round1p1score < round1p2score) {
            p1Record = -1; // Player 1 loses
            p2Record = 1; // Player 2 wins
        }

        //* The server should then print the result of round 1: including the number selected, and the score of each player 
        msg = "Round 1 Results\n===================================\n"
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
    }

    private void Round2() throws IOException { 
        out1.write("ROUND 2 START");
        out1.newLine();
        out1.flush();

        out1.write("Enter your number for Round 2: ");
        out1.newLine();
        out1.flush();
        
        out2.write("ROUND 2 START");
        out2.newLine();
        out2.flush();

        out2.write("Enter your number for Round 2: ");
        out2.newLine();
        out2.flush();
        
        p1rd2 = Integer.parseInt(in1.readLine()); // Read Round 2 number from player 1
        p2rd2 = Integer.parseInt(in2.readLine()); // Read Round 2 number from player 2

        // 1) Same-as-round-1 checks
        p1Same = (p1rd2 == p1rd1);
        p2Same = (p2rd2 == p2rd1);

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

            p1LegalR2 = GameUtils.legalMoveP1(p1rd2);
            p2LegalR2 = GameUtils.legalMoveP2(p2rd2);

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


        totalP1score = round1p1score + round2p1score;
        totalP2score = round1p2score + round2p2score;

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

        if(p1Result == 1 ) gameWinner = player1Id;
        else if (p2Result == 1) gameWinner = player2Id;
        else gameWinner = null;
        
        if(gameWinner != null) {
            msg = "GAME " + game + " Results\n===================================\n"
            + gameWinner + " WINS\n"
            + player1Id + " scored: " + round2p1score + " with number: " + p1rd2 + " | Total Score:" + totalP1score + " | Match Result: " + p1Result + "\n"
            + player2Id + " scored: " + round2p2score + " with number: " + p2rd2 + " | Total Score:" + totalP2score + " | Match Result: " + p2Result;
        } else {
            msg = "GAME " + game + " Results\n===================================\n"
            + "GAME ENDED IN DRAW\n"
            + player1Id + " scored: " + round2p1score + " with number: " + p1rd2 + " | Total Score:" + totalP1score + " | Match Result: " + p1Result + "\n"
            + player2Id + " scored: " + round2p2score + " with number: " + p2rd2 + " | Total Score:" + totalP2score + " | Match Result: " + p2Result;
        }
        

        //Send results to player 1 
        out1.write(msg);
        out1.newLine();
        out1.flush();

        //Send results to player 2
        out2.write(msg);
        out2.newLine();
        out2.flush();
    }
    

    private void RunGame() throws IOException {
        PreGame();
        Round1();
        Round2();
        CloseAll();
        gameFinished();
    }

    private void CloseAll() throws IOException {
        socket1.close();
        socket2.close();
        in1.close();
        out1.close();
        in2.close();
        out2.close();
    }

    private void gameFinished() throws IOException {
        Server.activeGames--;
        if (gameWinner == null) {
            System.out.println("GAME ENDED" + game + " || DRAW || ACTIVE GAMES: " + Server.activeGames);
            
        }else  System.out.println("GAME ENDED || " + gameWinner + " WON || ACTIVE GAMES: " + Server.activeGames);
    
        if (Server.activeGames == 0) {
            System.out.println("NO ACTIVE GAMES || SHUTTING DOWN SERVER");
            Server.serverSocket.close();
        }
    }
    
}

    
