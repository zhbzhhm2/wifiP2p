package ustc.code.wifi.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ustc.code.wifi.Adapter.ChatAdapter;
import ustc.code.wifi.Model.Chat;
import ustc.code.wifi.Model.NetTool;
import ustc.code.wifi.Model.Tool;
import ustc.code.wifi.Model.User;
import ustc.code.wifi.R;

import static android.content.ContentValues.TAG;

public class ChatActivity extends Activity implements View.OnClickListener{
    User obtianUser;
    List<Chat> chatList;
    final int FILE_SELECT_CODE=1,TAKEPHOTO=0;
    ChatAdapter chatAdapter;
    File currentFile;
    BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("reciveFile")){
                if(obtianUser.getIP().equals(intent.getExtras().getString("ip")))
                    chatList.add(new Chat(intent.getExtras().getString("file"),Chat.RECEIVE));
                chatAdapter.notifyDataSetChanged();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        obtianUser=(User) getIntent().getExtras().getSerializable("user");
        initDate();
        initView();
        registerReceiver(receiver,new IntentFilter("reciveFile"));
    }
    private  void initDate(){
        chatList=new ArrayList<>();
        chatList.add(new Chat("A.ppt",Chat.RECEIVE));
        chatList.add(new Chat("B.avi",Chat.SEND));
        chatList.add(new Chat("WhosYourDaddy.ppt",Chat.RECEIVE));
        chatList.add(new Chat("GreedIsGood.avi",Chat.SEND));

    }
    private void initView(){
        ((TextView)findViewById(R.id.textViewHeadName)).setText(obtianUser.getUserName());
        ((Button)findViewById(R.id.buttonChatSendFile)).setOnClickListener(this);
        ((Button)findViewById(R.id.buttonChatSendPicture)).setOnClickListener(this);
        ((Button)findViewById(R.id.buttonChatTakePhoto)).setOnClickListener(this);
         RecyclerView recyclerView=((RecyclerView)findViewById(R.id.recyclerViewChat));
        chatAdapter=new ChatAdapter(this);
        chatAdapter.setList(chatList);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.smoothScrollToPosition(chatList.size());
    }
    @Override
    public void onClick(View view) {
        Intent intent= new Intent(Intent.ACTION_GET_CONTENT);
        switch (view.getId()){

            case R.id.buttonChatSendFile:
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,FILE_SELECT_CODE);
                break;
            case R.id.buttonChatSendPicture:
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,FILE_SELECT_CODE);
                break;

            case R.id.buttonChatTakePhoto:
                currentFile=takePhoto();
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
//                    String name = getName(this, uri);
                    final String name= Tool.getFileAbsolutePath(this,uri);
                    String[] split=name.split("/");
                    new Thread() {
                        @Override
                        public void run() {
                            NetTool.sendFile(obtianUser.getIP(),new File(name));
                        }
                    }.start();
                    chatList.add(new Chat(split[split.length-1],Chat.SEND));
                    chatAdapter.notifyDataSetChanged();
                    Log.i(TAG, "onActivityResult: sendFile"+name);
                }
                break;
            case TAKEPHOTO:
                if (resultCode == RESULT_OK) {
                    chatList.add(new Chat(currentFile.getName(),Chat.SEND));
                    chatAdapter.notifyDataSetChanged();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private File takePhoto(){
        Intent intent = new Intent();
        File appDir = new File(Environment.getExternalStorageDirectory(), "/AWIFI/File");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        Uri uri = Uri.fromFile(file);
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, TAKEPHOTO);
        return file;
    }
}
