package org.example.rps;

import org.example.rps.dto.PlayerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.*;

public class ServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private static final int PORT = 8888;
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 100;
    private static final int KEEP_ALIVE_TIME = 60;
    private static volatile boolean isRunning = true;

    private static final Queue<PlayerInfo> players = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server started. Waiting for players...");

            while (isRunning) {
                Socket playerSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(playerSocket, players);
                executor.execute(clientHandler);
            }
        } catch (IOException e) {
            logger.error("Error occurred while accepting client connection", e);
        } finally {
            executor.shutdown();
        }
    }

    public static void stopServer() {
        isRunning = false;
    }
}
