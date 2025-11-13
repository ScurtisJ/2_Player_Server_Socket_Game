# Two-Player Server Socket Game (Java)

## Overview

This project is a simple two-player network game written in Java using sockets and blocking I/O (input/output).  

- A **Server** waits for exactly two clients to connect.
- Each **Client** connects to the server, sends a username, then plays two rounds of a number-picking game.
- The server validates moves, calculates scores using highest common factor (HCF, highest common factor) and lowest common multiple (LCM, lowest common multiple), and reports the final winner using `1 / -1 / 0` notation (win / lose / draw).

The focus is on understanding:

- Server–client communication over TCP (transmission control protocol) sockets.
- Line-based protocols using `BufferedReader` (buffered reader) and `BufferedWriter` (buffered writer).
- Basic game state and scoring on the server.

---

## File Structure

```text
src/
  netgame/
    Server.java     # Main server logic
    Client.java     # Client program for each player
    GameUtils.java  # Utility functions: HCF, LCM, prime check, legal move logic
````

---

## How to Compile and Run

From the project root (where `src/` lives):

```bash
# Compile
javac src/netgame/*.java

# Start the server (in terminal 1)
java src.netgame.Server

# Start client 1 (in terminal 2)
java src.netgame.Client

# Start client 2 (in terminal 3)
java src.netgame.Client
```

The server listens on port **24175**, and both clients connect to `127.0.0.1` (localhost).

---

## Game Flow

### 1. Connection and Identification

1. Server starts and listens for connections on port `24175`.
2. First client connects:

   * Server assigns **Player 1** and sends:
     `"You are Player 1"`.
3. Second client connects:

   * Server assigns **Player 2** and sends:
     `"You are Player 2"`.
4. Each client:

   * Prompts locally: `What is your username?`
   * Sends the username to the server.
5. Server reads both usernames and prints to its console:
   `player1Id vs player2Id: Game Start!`

---

### 2. Round 1

1. Server sends to both players:

   * `ROUND 1 START`
   * `Enter your number for Round 1:`
2. Each client:

   * Reads the messages.
   * Prompts the user to type a number.
   * Sends that number back to the server.

#### Legal move rules (Round 1)

* **Player 1**:

  * Number must be between **50 and 99** (inclusive).
  * Number must be **non-prime** (not a prime number).
* **Player 2**:

  * Number must be between **60 and 99** (inclusive).
  * Number must be **non-prime**.

Legality is checked via:

* `GameUtils.legalMoveP1(int a)`
* `GameUtils.legalMoveP2(int b)`

#### Scoring rules (Round 1)

Let `x` be Player 1’s number and `y` be Player 2’s number.

1. **Both moves legal**:

   * Player 1’s round score = `HCF(x, y)`
   * Player 2’s round score = last digit of `LCM(x, y)`, i.e. `LCM(x, y) % 10`
2. **Only Player 1 legal**:

   * Player 1 = `100`
   * Player 2 = `0`
3. **Only Player 2 legal**:

   * Player 1 = `0`
   * Player 2 = `100`
4. **Both illegal**:

   * Player 1 = `0`
   * Player 2 = `0`

The server sends a multi-line summary to *each* client, for example:

```text
Round 1 Results
===========
player1Id scored: <p1Round1Score> with number: <p1Number>
player2Id scored: <p2Round1Score> with number: <p2Number>
```

---

### 3. Round 2

1. Server sends to both players:

   * `ROUND 2 START`
   * `Enter your number for Round 2:`
2. Each client:

   * Reads messages.
   * Prompts the user for a second number.
   * Sends that number to the server.

#### Extra rule: “Same number as Round 1”

If a player uses the **exact same number** in Round 2 as in Round 1:

* That Round 2 move is treated as **invalid**, even if it met the normal range and prime rules.
* Scoring then follows the same 0/100 penalty logic as for any other illegal move.

So for Round 2:

* First we check if each player reused their Round 1 number.
* If they did, that player’s Round 2 move is automatically invalid.

#### Legal move rules (Round 2)

After applying the “same as Round 1” rule:

* If not reused:

  * Player 1’s Round 2 number must still be **50–99, non-prime**.
  * Player 2’s Round 2 number must still be **60–99, non-prime**.
* Then the same logic as Round 1 is used:

  * Both legal → HCF (highest common factor) and last digit of LCM (lowest common multiple).
  * One legal → legal gets `100`, illegal gets `0`.
  * Both illegal → both get `0`.

Again, the server sends a summary:

```text
Round 2 Results
===========
player1Id scored: <p1Round2Score> with number: <p1Round2Number> | Total Score: <p1Total> | Match Result: <p1ResultCode>
player2Id scored: <p2Round2Score> with number: <p2Round2Number> | Total Score: <p2Total> | Match Result: <p2ResultCode>
```

---

### 4. Final Result (1 / -1 / 0 notation)

After both rounds, the server calculates:

```text
totalP1 = round1P1 + round2P1
totalP2 = round1P2 + round2P2
```

Then determines a **result code** for each player:

* Player’s total > opponent’s total → result code = `1` (win)
* Player’s total < opponent’s total → result code = `-1` (lose)
* Totals equal → result code = `0` (draw)

These `1 / -1 / 0` values are included in the final Round 2 summary line as `Match Result`.

The server also prints the final result to its own console, for example:

```text
(player1Id) Player 1 Wins!
```

or

```text
It's a Draw!
```

Finally, the server closes all sockets and terminates, and each client prints a local message such as:

```text
GAME OVER, PLAY AGAIN SOON!
```

---

## `GameUtils` Details

`GameUtils.java` provides helper methods:

* `int HCF(int a, int b)`
  Uses the Euclidean algorithm to compute the highest common factor (HCF, highest common factor) of two integers.

* `int LCM(int a, int b)`
  Uses `LCM(a, b) = (a * b) / HCF(a, b)` to compute the lowest common multiple (LCM, lowest common multiple).

* `boolean primeNumber(int x)`
  Returns `true` if `x` is prime, using trial division up to `sqrt(x)`.

* `boolean legalMoveP1(int a)`
  Returns `true` if Player 1’s move is valid (50–99, non-prime).

* `boolean legalMoveP2(int b)`
  Returns `true` if Player 2’s move is valid (60–99, non-prime).

---

## Key Concepts Demonstrated

* **Sockets and TCP (transmission control protocol)**
  The server uses a `ServerSocket` to accept incoming `Socket` connections from clients on a fixed port.

* **Blocking I/O (input/output)**
  The program uses `BufferedReader.readLine()` and `BufferedWriter.write()` with `flush()`. Calls to `readLine()` block until a full line (ending with newline) arrives.

* **Simple text-based protocol**
  Communication follows a strict order: role messages, round start messages, prompts, number submissions, and result summaries. Both client and server must agree on this order.

* **Game state on the server**
  The server keeps track of:

  * Usernames.
  * Round 1 and Round 2 numbers.
  * Round scores and total scores.
  * Final result codes using `1 / -1 / 0` notation.

---

## Possible Extensions

If you wanted to extend this project further, you could:

* Add threads on the server side to handle multiple game sessions concurrently.
* Add input validation on the client side (reject non-numeric input before sending).
* Improve the text protocol to include explicit “END” markers.
* Build a simple graphical user interface (GUI, graphical user interface) on top of the existing client logic.

---

```
```
