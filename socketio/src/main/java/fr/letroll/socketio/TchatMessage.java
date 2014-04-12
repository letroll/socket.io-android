package fr.letroll.socketio;
//package fr.letroll.socketio;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by letroll on 16/02/2014.
 */
public class TchatMessage extends Message
{
    private String pseudo,message;

    public TchatMessage(JSONObject msg){
        pseudo = message = "";
        try {
            message = msg.getString("message");
            pseudo = msg.getString("pseudo");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getIdentifier() {
        return pseudo;
    }

    @Override
    public Object getData() {
        return message;
    }

    @Override
    public String toString()
    {
        return pseudo+":"+message;
    }
}
