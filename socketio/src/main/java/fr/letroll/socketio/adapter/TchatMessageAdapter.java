package fr.letroll.socketio.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import fr.letroll.socketio.MyApp;
import fr.letroll.socketio.R;
import fr.letroll.socketio.TchatMessage;

/**
 * Created by letroll on 22/02/2014.
 */
public class TchatMessageAdapter extends BaseAdapter {

    // Attributs
    private LayoutInflater inflater;
    private List<TchatMessage> list;

    public TchatMessageAdapter(List<TchatMessage> list){
        inflater = LayoutInflater.from(MyApp.INSTANCE);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder vh;

        if(convertView == null){
            convertView = inflater.inflate(R.layout.tchat_message_list,parent,false);
            vh = new ViewHolder();
            vh.line = (TextView) convertView.findViewById(R.id.tvLine);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }

        vh.line.setText(list.get(pos).toString());

        return convertView;
    }

    public class ViewHolder{
        TextView line;
    }
}
