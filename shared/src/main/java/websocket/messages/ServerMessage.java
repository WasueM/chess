package websocket.messages;

import model.GameData;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    private GameData game;

    private String errorMessage;

    private String message;

    public static ServerMessage loadGame(GameData updatedGame) {
        ServerMessage msg = new ServerMessage(ServerMessageType.LOAD_GAME);
        msg.game = updatedGame;
        return msg;
    }

    public static ServerMessage error(String errorMessage) {
        ServerMessage msg = new ServerMessage(ServerMessageType.ERROR);
        msg.errorMessage = errorMessage;
        return msg;
    }

    public static ServerMessage notification(String notificationMessage) {
        ServerMessage msg = new ServerMessage(ServerMessageType.NOTIFICATION);
        msg.message = notificationMessage;
        return msg;
    }

    public GameData getGame() {
        return this.game;
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public String getServerMessage() {
        return this.message;
    }

    public String getServerErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
