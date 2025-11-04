package lk.ijse.introductiontonetworkprogrammingexam;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static final int port = 5000;
    static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            System.out.println("SERVER STARTED ON PORT " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());

                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        System.out.println("Broadcasting message: " + message);
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    try {
                        client.out.writeUTF(message);
                        client.out.flush();
                    } catch (IOException e) {
                        System.out.println("Error sending message to client: " + e.getMessage());
                        clients.remove(client);
                    }
                }
            }
        }
    }

    static class ClientHandler implements Runnable {
        Socket socket;
        DataInputStream in;
        DataOutputStream out;
        String userName;

        ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.out = new DataOutputStream(socket.getOutputStream());
                this.in = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // Read username first
                this.userName = in.readUTF();
                System.out.println(userName + " CLIENT CONNECTED");

                // Add to clients list
                synchronized (clients) {
                    clients.add(this);
                }

                // Broadcast that new user joined
                broadcast(userName + " joined the chat!", this);

                // Listen for messages
                while (true) {
                    String message = in.readUTF();
                    System.out.println("MESSAGE from " + userName + ": " + message);
                    broadcast(userName + ": " + message, this);
                }
            } catch (Exception e) {
                System.out.println("CLIENT DISCONNECTED: " + (userName != null ? userName : socket.getInetAddress()));
            } finally {
                // Clean up
                synchronized (clients) {
                    clients.remove(this);
                }
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (socket != null) socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Broadcast that user left
                if (userName != null) {
                    broadcast(userName + " left the chat!", this);
                }
            }
        }
    }
}