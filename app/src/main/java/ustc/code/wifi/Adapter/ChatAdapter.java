package ustc.code.wifi.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ustc.code.wifi.Model.Chat;
import ustc.code.wifi.R;

/**
 * Created by zhb_z on 2016/10/31 0031.
 */

public class ChatAdapter extends RecyclerView.Adapter {

    List<Chat> list;
    private LayoutInflater inflater;
    private Context context;
    public ChatAdapter(Context context){
        inflater = LayoutInflater.from(context);
        this.context=context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(inflater.inflate(R.layout.item_chat, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(list==null)return;
        ((MyViewHolder)holder).textView.setText(list.get(position).getContent());

        if(list.get(position).getFlag()==Chat.SEND)
            ((MyViewHolder)holder).linearLayout.setGravity(Gravity.RIGHT);
        else
            ((MyViewHolder)holder).linearLayout.setGravity(Gravity.LEFT);

        ((MyViewHolder)holder).textView.setTag(position);


        if(list.get(position).getFlag()==Chat.RECEIVE)
            ((MyViewHolder)holder).textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(),"Recive File Successful!",Toast.LENGTH_SHORT).show();
                    switch (list.get((Integer)view.getTag()).getType()){
                        case "picture":
                            String chosenPicFile= Environment.getExternalStorageDirectory()+"/AWIFI/File/"
                                    +list.get((Integer)view.getTag()).getContent();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse("file://"+chosenPicFile), "image/*");
                            context.startActivity(intent);
                            break;
                    }
                }
            });


    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<Chat> list) {
        this.list = list;
    }
    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        LinearLayout linearLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            textView=(TextView)itemView.findViewById(R.id.textViewChat);
            linearLayout=(LinearLayout)itemView.findViewById(R.id.linearLayoutChat);
        }
    }
}
