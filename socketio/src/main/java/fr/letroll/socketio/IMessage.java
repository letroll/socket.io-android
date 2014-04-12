package fr.letroll.socketio;

/**
 * Created by letroll on 16/02/2014.
 */
public interface IMessage {
    public abstract String getIdentifier();
    public abstract Object getData();
}
