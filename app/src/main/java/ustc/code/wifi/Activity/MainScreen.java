package ustc.code.wifi.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import ustc.code.wifi.Adapter.RecyclerViewAdapter;
import ustc.code.wifi.Model.NetTool;
import ustc.code.wifi.Model.Tool;
import ustc.code.wifi.Model.User;
import ustc.code.wifi.R;
import ustc.code.wifi.service.ScanAppService;

import static android.R.attr.breadCrumbShortTitle;
import static android.R.attr.path;

public class MainScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerViewAdapter adapter;
    User me;
    List<User> users;
    ArrayList<File> bitmapList=new ArrayList<>();
    final int TAKEPHOTO=1,MANAGEPHOTO=2,FILE_SELECT_CODE=3;
    File currentFile;

    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()){
                case "newUser":
                    User user=(User) intent.getSerializableExtra("user");
                    users.add(user);
                    adapter.notifyDataSetChanged();
                    break;
                case "reciveFile":

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String send = "Select at least one target !";
                for (User user : users) {
                    if (user.isSelected()) {
                        send = "Mass Photo Successful !";
                    }
                }
                Snackbar.make(view, send, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView text=(TextView) ((LinearLayout)navigationView.getHeaderView(0)).getChildAt(1);
        text.setText(getIntent().getExtras().getString("name"));


        initView();
        Intent intent=new Intent(this,ScanAppService.class);
        intent.putExtra("user",me);
        startService(intent);
        registerReceiver(broadcastReceiver,new IntentFilter("newUser"));
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,ScanAppService.class));
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK)
            return;
        Intent intent;
        switch (requestCode) {
            case TAKEPHOTO:
                    bitmapList.add(currentFile);
                    try {
                        MediaStore.Images.Media.insertImage(getContentResolver(),
                                currentFile.getAbsolutePath(), currentFile.getName(), null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    // 最后通知图库更新
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
                break;
            case MANAGEPHOTO:
                bitmapList=(ArrayList<File>) data.getExtras().getSerializable("file");
                break;
            case FILE_SELECT_CODE:

                    String filePath=Tool.getFileAbsolutePath(this,data.getData());
                    File file=new File(filePath);
                    bitmapList.add(file);
                break ;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_SelectAll) {
            for(User user:users){
                user.setSelected(true);
                adapter.notifyDataSetChanged();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent intent=new Intent();
        switch (id){
            case  R.id.nav_camera:
                currentFile=takePhoto();
                break;
            case R.id.local_photo:
                intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,FILE_SELECT_CODE);
                break;
            case R.id.manage_photo:
                Bundle bundle=new Bundle();
                bundle.putSerializable("file",bitmapList);
                intent=new Intent(this,ManagePhotoActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent,MANAGEPHOTO);
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initView(){
        String name=getIntent().getExtras().getString("name");

        me=new User(name,new NetTool().getLocAddress(),new NetTool().getLocAddress());

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recyclerViewMain);
        initDate();
        adapter=new RecyclerViewAdapter(this);
        adapter.setData(users);
        adapter.setOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (Integer) view.getTag();
                Intent intent=new Intent(MainScreen.this,ChatActivity.class);
                Bundle bundle=new Bundle();
                bundle.putSerializable("user",users.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    private void initDate(){
        users =new ArrayList<>();
        String []name=new String[]{"Zhang","Wang","Li"};
        String []message=new String[]{"first","second","third"};
        for(int i=0;i<name.length;i++) {
            User user=new User(name[i],name[i],name[i]);
            users.add(user);
        }
    }


    private File takePhoto(){
        Intent intent = new Intent();
        File appDir = new File(Environment.getExternalStorageDirectory(), "MyPicture");
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
