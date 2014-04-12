package fr.letroll.socketio;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.DisconnectCallback;
import com.koushikdutta.async.http.socketio.ErrorCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.JSONCallback;
import com.koushikdutta.async.http.socketio.ReconnectCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.koushikdutta.async.http.socketio.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.letroll.socketio.adapter.TchatMessageAdapter;

public class MainActivity extends Activity implements Constant {
    @InjectView(R.id.tvStatus)
    TextView tvStatus;
    @InjectView(R.id.edtPseudo)
    EditText edtPseudo;
    @InjectView(R.id.edtMessage)
    EditText edtMessage;
    @InjectView(R.id.lvMsg)
    ListView lvMsg;

    private List<TchatMessage> msgList = new ArrayList<>();

    private SocketIOClient mClient = null;

    private TchatMessageAdapter adp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        adp = new TchatMessageAdapter(msgList);
        lvMsg.setAdapter(adp);
        //######

        SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), Constant.SERVER_URL, new ConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, SocketIOClient client) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                mClient = client;
                setConnexionStatus(true);

                loge("connected");

                //Save the returned SocketIOClient instance into a variable so you can disconnect it later
                client.setDisconnectCallback(disconnecCallback);
                client.setReconnectCallback(reconnectCallback);
                client.setErrorCallback(errorCallback);
                client.setJSONCallback(jsonCallBack);
                client.setStringCallback(stringCallBack);

                //You need to explicitly specify which events you are interested in receiving
                client.addListener("recupererMessages", recupererMessagesListener);
                client.addListener("recupererNouveauMessage", recupererNouveauMessageListener);

                client.setStringCallback(stringCallBack);
                client.on("nouveauMessage", new EventCallback() {
                    @Override
                    public void onEvent(JSONArray argument, Acknowledge acknowledge) {
                        loge("args: " + argument.toString());
                    }
                });
                client.setJSONCallback(new JSONCallback() {
                    @Override
                    public void onJSON(JSONObject jsonObject, Acknowledge acknowledge) {
                        loge("json: " + jsonObject.toString());
                    }
                });
            }
        });
    }

    private void loge(String txt) {
        Log.e("test", txt);
    }

    private void toastc(Context c, String txt) {
        Toast.makeText(c, txt, Toast.LENGTH_SHORT).show();
    }

    private void setConnexionStatus(final boolean status) {
        loge("setConnexionStatus:" + status);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText(status ? R.string.connected : R.string.not_connected);
                tvStatus.setBackgroundColor(status ? 0xFF00FF00 : 0xFFFF0000);
            }
        });
    }


    private EventCallback recupererNouveauMessageListener = new EventCallback() {
        @Override
        public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
            loge("onEvent recupererNouveauMessageListener jsonarray:" + (jsonArray == null ? "null" : jsonArray.toString()) + " ack:" + (acknowledge == null ? "null" : acknowledge.toString()));
            try {
                msgList.add(new TchatMessage(jsonArray.getJSONObject(0)));
                notifyList();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void notifyList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adp.notifyDataSetChanged();
            }
        });
    }

    private JSONArray getTextToSend() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(getTchatMessage(edtPseudo.getText().toString(), edtMessage.getText().toString()));
        return array;
    }

    private JSONObject getTchatMessage(String pseudo, String msg) throws JSONException {
        return new JSONObject().put("pseudo", pseudo).put("message", msg);
    }

    @OnClick(R.id.btnClearTxt)
    public void clearTxt() {
        emit("clearTxt");
        msgList.clear();
        notifyList();
    }

    @OnClick(R.id.btnRefreshData)
    public void refreshData() {
        String pseudo=edtPseudo.getText().toString();
        String message=edtMessage.getText().toString();

        try {
            if (!TextUtils.isEmpty(message) && !TextUtils.isEmpty(pseudo)) {
                emit("nouveauMessage", getTextToSend());
                msgList.add(new TchatMessage(getTchatMessage(pseudo, message)));
                notifyList();
                edtMessage.setText("");
            } else {
                toastc(this, "Un pseudo et un texte sont nécessaire");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private EventCallback recupererMessagesListener = new EventCallback() {
        @Override
        public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
            if (jsonArray == null) {
                loge("onEvent recupererMessagesListener jsonarray:null");
                loge("pas de message dispo donc on vide l'afficache");
                msgList.clear();
                notifyList();
                return;
            }
            loge("onEvent recupererMessagesListener jsonarray:" + jsonArray.toString());
            try {
                JSONArray data = jsonArray.getJSONArray(0);
                int msgCount = data.length();
                if (msgCount > 0) {
                    for (int i = 0; i < msgCount; i++) {
                        try {
                            msgList.add(new TchatMessage(data.getJSONObject(i)));
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }

                    notifyList();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            edtMessage.setText("");
                        }
                    });

                    loge("message dispo :" + msgList.size());
                } else {
                    loge("pas de message dispo donc on vide l'afficache");
                    msgList.clear();
                    notifyList();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private StringCallback stringCallBack = new StringCallback() {
        @Override
        public void onString(String s, Acknowledge acknowledge) {
            loge(s);
        }
    };
    private DisconnectCallback disconnecCallback = new DisconnectCallback() {
        @Override
        public void onDisconnect(Exception e) {
            loge("onDisconnect");
            if (e != null) {
                e.printStackTrace();
                return;

            }
            setConnexionStatus(false);
        }
    };
    private ReconnectCallback reconnectCallback = new ReconnectCallback() {
        @Override
        public void onReconnect() {
            loge("onReconnect");
            setConnexionStatus(true);
        }
    };
    private JSONCallback jsonCallBack = new JSONCallback() {
        @Override
        public void onJSON(JSONObject jsonObject, Acknowledge acknowledge) {
            loge("onJSON:" + jsonObject.toString());
        }
    };
    private ErrorCallback errorCallback = new ErrorCallback() {
        @Override
        public void onError(String s) {
            loge("onError:" + s);
        }
    };

    private void emit(String txt) {
        if (mClient == null) {
            toastc(this, "client null");
        } else {
            mClient.emit(txt);
        }
    }

    private void emit(String txt,JSONArray arr) {
        if (mClient == null) {
            toastc(this, "client null");
        } else {
            mClient.emit(txt,arr);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClient != null)
            mClient.disconnect();
    }

}