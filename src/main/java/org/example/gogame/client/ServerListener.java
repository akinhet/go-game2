package org.example.gogame.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Listens for incoming messages from the server on a separate thread.
 * Forwards received messages to the ClientGameController.
 *
 * @author Piotr Zieniewicz, Jan Langier
 */
public class ServerListener implements Runnable {
    private BufferedReader in;
    private ClientGameController controller;

    /**
     * Constructs a ServerListener.
     *
     * @param inputStream The input stream from the socket.
     * @param controller The controller to handle messages.
     */
    public ServerListener(InputStream inputStream, ClientGameController controller) {
        this.in = new BufferedReader(new InputStreamReader(inputStream));
        this.controller = controller;
    }

    /**
     * The run loop constantly reads lines from the input stream.
     */
    @Override
    public void run() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                controller.handleServerMessage(response);
            }
        } catch (IOException e) {
            System.out.println("Connection closed.");
            controller.handleConnectionError();
        }
    }
}