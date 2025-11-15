Here’s an updated version of the README that matches your new multi-game + threading setup:

````markdown
# Two-Player Server Socket Game (Java)

## Overview

This project is a two-player network game written in Java using sockets and blocking I/O (input/output).  

- A **Server** listens for connections and groups clients into pairs.
- Each **pair of clients** is handled by its own **GameSession** thread.
- Each **Client** connects to the server, sends a username, then plays two rounds of a number-picking game.
- The server validates moves, calculates scores using highest common factor (HCF, highest common factor) and lowest common multiple (LCM, lowest common multiple), and reports the final winner using `1 / -1 / 0` notation (win / lose / draw).
- When the last active game finishes, the server automatically shuts itself down.

The focus is on understanding:

- Server–client communication over TCP (transmission control protocol) sockets.
- Line-based protocols using `BufferedReader` (buffered reader) and `BufferedWriter` (buffered writer).
- Spawning and managing threads for independent game sessions.
- Basic game state and scoring on the server.

---

## File Structure

```text
src/
  netgame/
    Server.java      # Accepts clients, pairs them, starts GameSession threads
    Client.java      # Client program for each player
    GameSession.java # Runnable game logic for a single two-player match
    GameUtils.java   # Utility functions: HCF, LCM, prime check, legal move logic
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

# You can start additional clients (4, 6, 8, …) and the server will
# group them into separate two-player games as pairs connect.
```

The server listens on port **24175**, and all clients connect to `127.0.0.1` (localhost).

---

## Server Behavior and Threading

* `Server` owns a single `ServerSocket` listening on port `24175`.

* Whenever **two clients** have connected, the server:

  1. Accepts `socket1` and `socket2`.
  2. Creates a `GameSession` with those sockets and a `gameId`.
  3. Increments a shared `activeGames` counter.
  4. Starts a new `Thread` running that `GameSession`.

* Each `GameSession` runs independently:

  * Handles all I/O (input/output) with its two players.
  * Runs **PreGame**, **Round 1**, **Round 2**, and closing logic.
  * When finished, calls back into the server via `gameFinished()`:

    * Decrements `activeGames`.
    * If `activeGames` becomes `0`, the server closes the `ServerSocket`.
    * Closing the `ServerSocket` causes the blocking `accept()` to throw, and the main server loop exits cleanly.

This means:

* Multiple games can be active at once (one thread per game).
* When the **last** game finishes, the server process exits automatically.

---

## Game Flow

### 1. Connection and Identification

For each game:

1. Server waits for two clients to connect.
2. First client in the pair:

   * Assigned **Player 1** and sent:
     `"You are Player 1"`.
3. Second client in the pair:

   * Assigned **Player 2** and sent:
     `"You are Player 2"`.
4. Each client:

   * Prompts locally: `What is your username?`
   * Sends the username to the server.
5. The `GameSession` reads both usernames and sends to both clients:

   ```text
   <player1Id> vs <player2Id>: Game Start!
   ```

---

### 2. Round 1

1. `GameSession` sends to both players:

   * `ROUND 1 START`
   * `Enter your number for Round 1:`
2. Each client:

   * Reads the messages in order.
   * Prompts the user to type a number.
   * Sends that number back to the server as a line of text.

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

   * Player 1’s Round 1 score = `HCF(x, y)`
   * Player 2’s Round 1 score = last digit of `LCM(x, y)`, i.e. `LCM(x, y) % 10`

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
===================================
player1Id scored: <p1Round1Score> with number: <p1Number>
player2Id scored: <p2Round1Score> with number: <p2Number>
```

Internally, the `GameSession` also records a simple per-round record:

* Player 1 win = `1`, lose = `-1`, draw = `0`.
* Player 2 gets the opposite.

---

### 3. Round 2

1. `GameSession` sends to both players:

   * `ROUND 2 START`
   * `Enter your number for Round 2:`
2. Each client:

   * Reads messages.
   * Prompts the user for a second number.
   * Sends that number to the server.

#### Extra rule: “Same number as Round 1”

If a player uses the **exact same number** in Round 2 as in Round 1:

* That Round 2 move is treated as **invalid**, even if it met the normal range and prime rules.

Special cases:

* Player 1 reused, Player 2 did not:

  * Player 1 Round 2 score = `0`
  * Player 2 Round 2 score = `100`

* Player 2 reused, Player 1 did not:

  * Player 1 Round 2 score = `100`
  * Player 2 Round 2 score = `0`

* Both reused:

  * Both scores = `0`

If neither reused, Round 2 uses the **same legality rules** as Round 1:

* Player 1: **50–99, non-prime**.
* Player 2: **60–99, non-prime**.

Then:

1. **Both legal** → HCF (highest common factor) and last digit of LCM (lowest common multiple).
2. **Only one legal** → legal gets `100`, illegal gets `0`.
3. **Both illegal** → both get `0`.

Round 2 summary sent to both clients:

```text
GAME <gameId> Results
===================================
<WIN MESSAGE or "GAME ENDED IN DRAW">
player1Id scored: <p1Round2Score> with number: <p1Round2Number> | Total Score:<p1Total> | Match Result: <p1ResultCode>
player2Id scored: <p2Round2Score> with number: <p2Round2Number> | Total Score:<p2Total> | Match Result: <p2ResultCode>
```

Where:

* `p1ResultCode` / `p2ResultCode` are from `{1, -1, 0}`.

---

### 4. Final Result (1 / -1 / 0 notation) and Shutdown

For each game:

```text
totalP1 = round1P1 + round2P1
totalP2 = round1P2 + round2P2
```

Result codes:

* Player’s total > opponent’s total → result code = `1` (win).
* Player’s total < opponent’s total → result code = `-1` (lose).
* Totals equal → result code = `0` (draw).

The winner’s name (if any) is included in the `GAME <gameId> Results` block, and each client prints a local message such as:

```text
GAME OVER, PLAY AGAIN SOON!
```

When a `GameSession` finishes:

* It closes its two sockets and I/O (input/output) streams.
* Calls `gameFinished()` on the server, decrementing the global `activeGames` counter.
* If `activeGames` hits `0`, the server closes the `ServerSocket`, causing the main `accept()` loop to exit and the process to end cleanly.

---

## `GameUtils` Details

`GameUtils.java` provides helper methods:

* `int HCF(int a, int b)`
  Uses the Euclidean algorithm to compute the highest common factor (HCF, highest common factor) of two integers.

* `int LCM(int a, int b)`
  Uses `LCM(a, b) = (a * b) / HCF(a, b)` to compute the lowest common multiple (LCM, lowest common multiple).

* `boolean primeNumber(int x)`
  Returns `true` if `x` is prime, using trial division up to `sqrt(x)` (square root of x).

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
  Communication follows a strict order: role messages, game start messages, round start messages, prompts, number submissions, and result summaries. Both client and server must agree on this order.

* **Threaded game sessions**
  Each two-player game runs in its own `GameSession` thread, allowing multiple games to be played concurrently.

* **Server-side game state**
  The server/session keeps track of:

  * Usernames.
  * Round 1 and Round 2 numbers.
  * Round scores and total scores.
  * Final result codes using `1 / -1 / 0` notation.

---

## Possible Extensions

If you wanted to extend this project further, you could:

* Add per-game logging to a file (for example, saving results and moves).
* Add input validation on the client side (reject non-numeric input before sending).
* Improve the text protocol to include explicit “END” markers or status codes.
* Implement a lobby system where players can choose opponents.
* Build a simple graphical user interface (GUI, graphical user interface) on top of the existing client logic.

```
```
