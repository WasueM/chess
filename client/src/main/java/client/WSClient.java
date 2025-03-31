package client;

import chess.ChessBoard;
import chess.ChessBoardJSONAdapter;
import com.google.gson.Gson;

import javax.websocket.*;
import java.net.URI;

import com.google.gson.GsonBuilder;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class WSClient extends Endpoint {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ChessBoard.class, new ChessBoardJSONAdapter())
            .create();
    public Session session;
    private GameController gameController;

    public WSClient(String url) throws Exception {

        // switch the http url to the websocket version
        String websocketURL = url.replaceFirst("http", "ws") + "ws";
        URI uri = new URI(websocketURL);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String jsonMessage) {
                System.out.println("MESSAGE RECEIVED");
                System.out.println(jsonMessage);
                ServerMessage message = GSON.fromJson(jsonMessage, ServerMessage.class);
                System.out.println("MESSAGE PARSED");
                System.out.println(message);

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

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public void sendCommand(UserGameCommand command) throws Exception {
        String json = GSON.toJson(command);
        this.session.getBasicRemote().sendText(json);
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}