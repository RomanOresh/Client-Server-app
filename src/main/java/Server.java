import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 8080;
    private static int clientCounter = 0;
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("[SERVER] Запуск сервера...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientCounter++;
                String clientName = "client-" + clientCounter;
                System.out.println("[SERVER] " + clientName + " успешно подключился");

                ClientHandler clientHandler = new ClientHandler(clientName, clientSocket);
                clients.put(clientName, clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("[SERVER] Ошибка у сервера: " + e.getMessage());
        }
    }

    public static void removeClient(String clientName) {
        clients.remove(clientName);
        System.out.println("[SERVER] " + clientName + " отключился");
    }

    private static class ClientHandler implements Runnable {
        private final String clientName;
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(String clientName, Socket socket) {
            this.clientName = clientName;
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("[SERVER] Вы подключены как " + clientName);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }
                    System.out.println("[" + clientName + "] " + message);
                }
            } catch (IOException e) {
                System.out.println("[SERVER] Ошибка соединения с " + clientName);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Server.removeClient(clientName);
            }
        }
    }
}