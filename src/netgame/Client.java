package src.netgame;

import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

public class Client {

    private Socket socket = null;
    private BufferedReader in = null;
    private BufferedWriter out = null;

    public Client(Socket socket) throws IOException {
        this.socket = socket;

        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void startClient() throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.print("What is your username?\n");
        out.write(scanner.nextLine()); //*The client should then send an ID (for this program, a single string) to the server for the record.
        out.newLine();
        out.flush();

        String serverMessage = in.readLine();
        System.out.println("Server: " + serverMessage);

        System.out.println(in.readLine()); // Read and print the Round 1 Start Message from the server
        System.out.println(in.readLine()); // Read the Prompt for Round 1 from the server

        out.write(scanner.nextLine()); // Clients sends its round 1 value to the server
        out.newLine();  
        out.flush();

        System.out.println(in.readLine()); // "Round 1 Results"
        System.out.println(in.readLine()); // "==========="
        System.out.println(in.readLine()); // "<player1 line>"
        System.out.println(in.readLine()); // "<player2 line>"

        System.out.println(in.readLine()); // Read and print the Round 2 Start Message from the server
        System.out.println(in.readLine()); // Read the Prompt for Round 2 from the server

        out.write(scanner.nextLine()); // * Once the client receives the information, it should send in a number for round 2
        out.newLine();
        out.flush();

        System.out.println(in.readLine()); // "Round 2 Results"
        System.out.println(in.readLine()); // "==========="
        System.out.println(in.readLine()); // "<player1 line>"
        System.out.println(in.readLine()); // "<player2 line>"

        System.out.println("\n GAME OVER, PLAY AGAIN SOON!"); // Read and print the Game Over Message from the server

        socket.close(); //*  After that, the client should disconnect from the server, and quit


    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 24175); //*Connect to the server on localhost at port 24175, The client will send in a request  
        // to the server requesting a game (for this program, you always assume the client and server is on the same machine (so you should use “127.0.0.1” as the IP address)

        Client client = new Client(socket); // Create a new instance of the Client class that connects to the server
        client.startClient(); // Start the client
    }
    
}
