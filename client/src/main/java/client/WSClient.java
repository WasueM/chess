package client;

import com.google.gson.Gson;

import javax.websocket.*;
import java.net.URI;
import java.util.Scanner;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;

public class WSClient extends Endpoint {
    private static final Gson gson = new Gson();
    public Session session;

    public WSClient(String url) throws Exception {
        URI uri = new URI(url + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
    }

    public void send(String msg) throws Exception {
        this.session.getBasicRemote().sendText(msg);
    }

    public void sendCommand(UserGameCommand command) throws Exception {
        String json = gson.toJson(command);
        this.session.getBasicRemote().sendText(json);
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}