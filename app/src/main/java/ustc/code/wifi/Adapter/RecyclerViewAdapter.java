package ustc.code.wifi.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ustc.code.wifi.Model.User;
import ustc.code.wifi.R;

/**
 * Created by zhb_z on 2016/10/30 0030.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter{

    private List<User> data;
    private LayoutInflater inflater;
    View.OnClickListener onClick;

    public RecyclerViewAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        MyViewHolder holder = new MyViewHolder(inflater.inflate(
                R.layout.item_main_screen, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final User user=data.get(position);
        if(data.get(position).getHead()!=null)
            ((MyViewHolder)holder).imageView.setImageBitmap(data.get(position).getHead());
        ((MyViewHolder)holder).name.setText(user.getUserName());
        ((MyViewHolder)holder).message.setText("message");
        ((MyViewHolder)holder).checkBox.setChecked(user.isSelected());

        ((MyViewHolder)holder).imageView.setTag(position);
        ((MyViewHolder)holder).name.setTag(position);
        ((MyViewHolder)holder).message.setTag(position);
        ((MyViewHolder)holder).checkBox.setTag(position);

        ((MyViewHolder)holder).imageView.setOnClickListener(onClick);
        ((MyViewHolder)holder).name.setOnClickListener(onClick);
        ((MyViewHolder)holder).message.setOnClickListener(onClick);

        ((MyViewHolder)holder).checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                user.setSelected(b);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<User> list){
        data=list;
    }

    public void setOnClick(View.OnClickListener onClick){
        this.onClick=onClick;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public TextView name,message;
        public CheckBox checkBox;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView=(ImageView) itemView.findViewById(R.id.imageViewMainPicture);
            name=(TextView)itemView.findViewById(R.id.textViewMainName);
            message=(TextView)itemView.findViewById(R.id.textViewMainMessage);
            checkBox=(CheckBox)itemView.findViewById(R.id.checkboxMainItem);
        }
    }
}
