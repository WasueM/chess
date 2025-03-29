package client;

import com.google.gson.Gson;

import javax.websocket.*;
import java.net.URI;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class WSClient extends Endpoint {
    private static final Gson gson = new Gson();
    public Session session;
    private GameController gameController;

    public WSClient(String url) throws Exception {
        URI uri = new URI(url + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String jsonMessage) {
                ServerMessage message = gson.fromJson(jsonMessage, ServerMessage.class);

                switch (message.getServerMessageType()) {
                    case LOAD_GAME -> {
                        if (message.getGame() != null) {
                            System.out.println("LOAD_GAME received: " + message.getGame().gameName());

                            // update the board with the new one
                            gameController.show();
                        } else {
                            System.out.println("LOAD_GAME received with no game data!");
                        }
                    }
                    case ERROR -> {
                        String errorMessage = message.getServerErrorMessage();
                        System.err.println("ERROR: " + errorMessage);
                        gameController.showError(errorMessage);
                    }
                    case NOTIFICATION -> {
                        String notificationMessage = message.getServerMessage();
                        System.out.println("NOTIFICATION: " + notificationMessage);
                        gameController.showNotication(notificationMessage);
                    }
                }
            }
        });
    }

    public setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public void sendCommand(UserGameCommand command) throws Exception {
        String json = gson.toJson(command);
        this.session.getBasicRemote().sendText(json);
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}