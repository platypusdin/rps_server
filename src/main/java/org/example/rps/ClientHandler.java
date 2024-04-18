package org.example.rps;

import org.example.rps.dto.PlayerInfo;
import org.example.rps.dto.Result;
import org.example.rps.dto.Weapon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.UUID;

public class ClientHandler extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private Socket socket;
    private PlayerInfo playerInfo;
    private Queue<PlayerInfo> players;
    private static final Object lock = new Object();

    public ClientHandler(Socket socket, Queue<PlayerInfo> players) {
        this.socket = socket;
        this.players = players;
    }

    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            output.println("Enter your name:");
            String playerName = input.readLine();
            logger.info("Received login from client: {}", playerName);

            synchronized (lock) {
                playerInfo = new PlayerInfo(UUID.randomUUID().toString(), playerName);
                players.offer(playerInfo);
                lock.notifyAll();
            }

            waitForOpponent(output);
            playGame(output, input);

            if (!socket.isClosed()) {
                logger.debug("Close socket for player {}", playerInfo.getLogin());
                socket.close();
            }
        }  catch (IOException | InterruptedException e) {
            logger.error("Error during the game", e);
        }
    }

    private void waitForOpponent(PrintWriter output) throws IOException {
        synchronized (lock) {
            while (players.size() < 2) {
                try {
                    output.println("Searching the opponent...");
                    lock.wait();
                } catch (InterruptedException e) {
                    logger.error("Interrupted error during the opponent searching", e);
                }
            }
        }
    }

    private PlayerInfo findOpponent() {
        PlayerInfo opponentInfo = null;
        while (opponentInfo == null) {
            opponentInfo = players
                    .stream()
                    .filter(o -> !playerInfo.getPlayerId().equals(o.getPlayerId()))
                    .findFirst()
                    .orElse(null);
        }
        synchronized (lock) {
            players.remove(playerInfo);
            players.remove(opponentInfo);
        }
        return opponentInfo;
    }

    private void playGame(PrintWriter output, BufferedReader input) throws IOException, InterruptedException {
        PlayerInfo opponentInfo = this.findOpponent();

        String playerName = playerInfo.getLogin();
        String opponentName = opponentInfo.getLogin();
        logger.info("The game has started for player {} and player {}", playerName, opponentName);

        output.println(String.format("The search is complete. Your opponent is %s", opponentName));
        this.fight(opponentInfo, output, input);
        logger.info("Game over");
    }

    private void fight(PlayerInfo opponentInfo, PrintWriter output, BufferedReader input) throws IOException {
        String playerName = opponentInfo.getLogin();

        String playerChoice = "";
        boolean valid;
        do {
            output.println("Please select your move (rock, paper, scissors):");
            playerChoice = input.readLine().toLowerCase();
            valid = Weapon.isValid(playerChoice);
        } while (!valid);

        logger.info("The player {} choose {}", playerName, playerChoice);

        playerInfo.setWeapon(playerChoice);

        String opponentChoice ="";

        try {
            output.println("Waiting for opponent's move...");
            opponentChoice = opponentInfo.waitForWeapon();
        } catch (InterruptedException e) {
            logger.error("Interrupted error during the opponent weapon waiting", e);
        }

        output.println(String.format("Your choice: %s. Your opponent's choice: %s.", playerChoice, opponentChoice));

        logger.info("Start counting results");
        Result battleResult =
                Weapon.valueOf(playerChoice.toUpperCase())
                        .battleResult(Weapon.valueOf(opponentChoice.toUpperCase()));
        output.println(battleResult.getMessage());

        logger.info("For player {} result is {}.", playerName, battleResult.name());
        if (Result.DRAW == battleResult || Result.ERROR == battleResult) {
            if (Result.DRAW == battleResult) {
                logger.info("It's a draw. Start battle again.");
            } else {
                logger.info("Error with counting results. Start battle again.");
            }
            playerInfo.setWeapon("");
            opponentInfo.setWeapon("");
            fight(opponentInfo, output, input);
        }
    }

}
