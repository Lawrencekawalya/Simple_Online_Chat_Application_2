package chatServer;

//import java.io.*;
//import java.net.*;
//import java.util.*;
//
//public class ChatServer {
//    private static final int PORT = 12345;
//    private static int userIdCounter = 1;
//    private static List<ClientHandler> clients = new ArrayList<>();
//
//    public static void main(String[] args) {
//        try {
//            ServerSocket serverSocket = new ServerSocket(PORT);
//            System.out.println("Chat Server started on port " + PORT);
//
//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                System.out.println("New client connected: " + clientSocket);
//
//                ClientHandler clientHandler = new ClientHandler(clientSocket);
//                clients.add(clientHandler);
//                clientHandler.start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    static class ClientHandler extends Thread {
//        private Socket clientSocket;
//        private PrintWriter out;
//
//        public ClientHandler(Socket clientSocket) {
//            this.clientSocket = clientSocket;
//        }
//
//        public void run() {
//            try {
//                out = new PrintWriter(clientSocket.getOutputStream(), true);
//                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//
//                // Assign a unique user ID to the client
//                int userId = userIdCounter++;
//                out.println("Welcome to the chat! Your user ID is: " + userId);
//
//                // Broadcast messages to all clients
//                String message;
//                while ((message = in.readLine()) != null) {
//                    System.out.println("Message received from user " + userId + ": " + message);
//                    broadcastMessage("User " + userId + ": " + message);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    clientSocket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        private void broadcastMessage(String message) {
//            for (ClientHandler client : clients) {
//                client.sendMessage(message);
//            }
//        }
//
//        private void sendMessage(String message) {
//            out.println(message);
//        }
//    }
//}


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static int userIdCounter = 1;
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static ExecutorService clientThreadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientThreadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (clientThreadPool != null && !clientThreadPool.isShutdown()) {
                clientThreadPool.shutdown();
            }
        }
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private int userId;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.userId = userIdCounter++;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println("Welcome to the chat! Your user ID is: " + userId);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Message received from user " + userId + ": " + message);
                    broadcastMessage("User " + userId + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeResources();
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client != this) {
                        client.sendMessage(message);
                    }
                }
            }
        }

        private void sendMessage(String message) {
            out.println(message);
        }

        private void closeResources() {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                clients.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

