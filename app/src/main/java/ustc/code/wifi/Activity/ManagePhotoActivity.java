package ustc.code.wifi.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ustc.code.wifi.R;

public class ManagePhotoActivity extends Activity {
    ArrayList<File> bitmapsList;
    SimpleAdapter simpleAdapter;
    final List<Map<String,Object>> list=new ArrayList<>();
    final MyHandler myHandler=new MyHandler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_photo);
        try {
            bitmapsList = (ArrayList<File>) getIntent().getExtras().getSerializable("file");
        }catch (Exception e){
            e.printStackTrace();
            finish();
        }
        initView();
    }

    private void initView(){
        GridView gridView=(GridView)findViewById(R.id.gridViewManage);
        String []from=new String[]{"image"};
        int []to=new int[]{R.id.imageViewManage};

        new Thread() {
            @Override
            public void run() {
                for(File file:bitmapsList) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    Map map = new HashMap();
                    map.put("image", bitmap);
                    list.add(map);
                }
                myHandler.sendEmptyMessage(1);
            }
        }.start();
        simpleAdapter=new SimpleAdapter(this,list,R.layout.item_manage_photo,from,to);
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                // TODO Auto-generated method stub
                if(view instanceof ImageView && data instanceof Bitmap){
                    ImageView iv = (ImageView)view;
                    iv.setImageBitmap((Bitmap) data);
                    return true;
                }else{
                    return false;
                }
            }
        });
        gridView.setAdapter(simpleAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle=new Bundle();
                bundle.putSerializable("file",(File)bitmapsList.get(i));
                Intent intent=new Intent();
                intent.putExtras(bundle);
                intent.setClass(ManagePhotoActivity.this,ImageProcessingActivity.class);
                startActivityForResult(intent,i);
            }
        });
    }
    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1)
                simpleAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK)
            return;
        final File file=(File) data.getExtras().getSerializable("file");
        bitmapsList.set(requestCode,file);
        new Thread(){
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                Map map = new HashMap();
                map.put("image", bitmap);
                list.set(requestCode,map);
                myHandler.sendEmptyMessage(1);
            }
        }.start();

        Intent intent=new Intent();
        intent.putExtra("file",bitmapsList);
        setResult(RESULT_OK,intent);
    }
}
