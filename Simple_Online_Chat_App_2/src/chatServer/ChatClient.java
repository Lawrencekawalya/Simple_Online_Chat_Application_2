package chatServer;
//
//import java.io.*;
//import java.net.*;
//
//public class ChatClient {
//    private static final String SERVER_IP = "localhost";
//    private static final int SERVER_PORT = 12345;
//
//    public static void main(String[] args) {
//        try {
//            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
//            System.out.println("Connected to chat server");
//
//            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
//            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//            // Read messages from the server
//            new Thread(() -> {
//                try {
//                    String message;
//                    while ((message = in.readLine()) != null) {
//                        System.out.println(message);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }).start();
//
//            // Send messages to the server
//            String userMessage;
//            while ((userMessage = userInput.readLine()) != null) {
//                out.println(userMessage);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class ChatClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final long RECONNECT_INTERVAL_MS = 5000; // 5 seconds

    public static void main(String[] args) {
        while (true) {
            try {
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                System.out.println("Connected to chat server");
                
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Read messages from the server
                new Thread(() -> {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                // Send messages to the server
                String userMessage;
                while ((userMessage = userInput.readLine()) != null) {
                    out.println(userMessage);
                }
                
                // Close the socket and exit the loop if the user exits the chat
                socket.close();
                break;
            } catch (IOException e) {
                // Connection error occurred, attempt to reconnect
                System.err.println("Connection error: " + e.getMessage());
                System.err.println("Attempting to reconnect in " + TimeUnit.MILLISECONDS.toSeconds(RECONNECT_INTERVAL_MS) + " seconds...");
                try {
                    TimeUnit.MILLISECONDS.sleep(RECONNECT_INTERVAL_MS);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
