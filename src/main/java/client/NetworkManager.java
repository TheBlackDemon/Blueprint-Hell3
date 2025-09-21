package client;

import network.NetworkMessage;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class NetworkManager implements Runnable {
    private final ClientMain client;
    private final BufferedReader reader;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public NetworkManager(ClientMain client) throws IOException {
        this.client = client;
        this.reader = new BufferedReader(new InputStreamReader(client.socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            String inputLine;
            while (running.get() && (inputLine = reader.readLine()) != null) {
                try {
                    NetworkMessage message = NetworkMessage.fromJson(inputLine);
                    client.handleMessage(message);
                } catch (Exception e) {
                    System.err.println("Error processing incoming message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Network error: " + e.getMessage());
                client.disconnect();
            }
        }
    }

    public void stop() {
        running.set(false);
        try {
            reader.close();
        } catch (IOException ignored) {}
    }
}
