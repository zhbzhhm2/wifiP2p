package ustc.code.wifi.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import ustc.code.wifi.Model.NetTool;
import ustc.code.wifi.Model.Tool;
import ustc.code.wifi.Model.User;
/**
 * Created by zhb_z on 2016/11/2 0002.
 */
public class ScanAppService extends Service {
    ArrayList<User> users=new ArrayList<>();
    User me;
    boolean flag=true;
    Handler handler;
    String localIP;
    ServerSocket serverSocket=null;
    final int FIND_USER=1,RECIVE_FILE=2;
    final int port=33073;
    @Override
    public void onCreate() {
        handler =new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case FIND_USER:
                        final User user = new User();
                        user.setIP(msg.getData().getString("ip"));
                        user.setUserName(msg.getData().getString("name"));
                        new Thread(){
                            @Override
                            public void run() {
                                addUsers(user);
                            }
                        }.start();
                        break;
                    case RECIVE_FILE:
                        Intent intent=new Intent("reciveFile");
                        intent.putExtras(msg.getData());
                        sendBroadcast(intent);
                        break;
                }
            }
        };
        localIP=new NetTool().getLocAddress();
        new SocketServerThread().start();
        new UDPServiceThread().start();
        new UDPSendThread().start();
        //new ScanThread().start();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        me=(User) intent.getExtras().getSerializable("user");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        try {
            if(serverSocket!=null)
             serverSocket.close();
            if(UDPService!=null)
                UDPService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    DatagramSocket UDPService =null;
    class UDPServiceThread extends Thread{

        @Override
        public void run() {

            DatagramPacket recive=null;
            byte []buffer=new byte[2048];
            try {
                UDPService =new DatagramSocket(port);
                recive=new DatagramPacket(buffer,buffer.length);

                while (flag) {
                    UDPService.receive(recive);
                    String out=new String(buffer,0,5);
                    InetAddress ip=recive.getAddress();
                    Message msg=new Message();
                    Bundle bundle=new Bundle();
                    bundle.putString("ip",ip.toString().replace("/",""));
                    msg.setData(bundle);
                    msg.what=FIND_USER;
                    if("login".equals(out)) {
                        UDPService.send(new DatagramPacket("aline".getBytes(),"aline".getBytes().length,ip,port));
                        handler.sendMessage(msg);
                    }else if("aline".equals(out))
                        handler.sendMessage(msg);

                   // System.out.println(out);
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(UDPService!=null)
                    UDPService.close();
            }
        }
    }
    class UDPSendThread extends Thread{
        @Override
        public void run() {
            DatagramPacket send=null;
            byte []buf=new byte[2048];
            String IP="255.255.255.255";
            String msg="login";
            try {
                send=new DatagramPacket(msg.getBytes(),msg.length(),InetAddress.getByName(IP),port);
                while (flag) {
                    if(UDPService!=null)
                         UDPService.send(send);
                    sleep(10000);
                }

            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    class ScanThread extends Thread{
        @Override
        public void run() {
            flag=true;
            NetTool netTool=new NetTool();
            localIP=netTool.getLocAddress();
            String []iparr=localIP.split("\\.");
            String net=iparr[0]+"."+iparr[1]+"."+iparr[2];
            while (flag){
                for(int i=0;i<32;i++){
                    for(int j=0;j<8;j++){
                        Ping ping=new Ping();
                        ping.ip=net+"."+(8*i+j);
                        ping.start();
                    }
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    sleep(600000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Ping extends Thread{
        public String ip;
        @Override
        public void run() {
           // if(new NetTool().ping(ip)){
            if(new NetTool().connect(ip,port)){
                Bundle bundle=new Bundle();
                bundle.putSerializable("ip",ip);
                Message msg=new Message();
                msg.what=1;
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }
    }
    class SocketServerThread extends Thread{
        @Override
        public void run() {
            flag=true;
            try {
                serverSocket=new ServerSocket(port);
                while (flag){
                    Socket client=serverSocket.accept();
                    new ReciveFromClint(client).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    String readLine(byte []in){
        StringBuffer re=new StringBuffer();
        char []ret=new String(in).toCharArray();
        for(int i=0;i<ret.length;i++)
            if(ret[i]!='\n')
                re.append(ret[i]);
            else
                break;
        return re.toString();
    }
    class ReciveFromClint extends Thread{
        Socket socket;
        public ReciveFromClint(Socket ip){
            this.socket=ip;
        }
        @Override
        public void run() {
            try {
//                InputStream in=socket.getInputStream();
//                byte []stringByte=new byte[2048];
//                System.out.println(in.read(stringByte,0,stringByte.length));
//                String line=readLine(stringByte);

                BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line=in.readLine();

                //PrintWriter out=new PrintWriter(socket.getOutputStream());

                    if(line!=null&&line.length()>0) {
                        String []split=line.split("\\.");
                        switch (split[0]){
                            case "test":
                                break;
                            case "askName":
                                Socket ret=new Socket(socket.getInetAddress().toString().replace("/",""),port);
                                BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(ret.getOutputStream()));
                                bw.write("name."+me.getUserName()+"\n");
                                bw.flush();
                                bw.close();
                                break;
                            case "name":
                                Bundle bu=new Bundle();
                                bu.putString("ip",socket.getInetAddress().toString().replace("/",""));
                                bu.putString("name",split[1]);
                                Message msg1=new Message();
                                msg1.setData(bu);
                                msg1.what=FIND_USER;
                                handler.sendMessage(msg1);
                                break;
                            case "file":
                                BufferedWriter tempWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                tempWriter.write("accept\n");
                                tempWriter.flush();
                                String filename=split[1];
                                for(int i=2;i<split.length-1;i++)
                                    filename+="."+split[i];
                                long length=Long.valueOf(split[split.length-1]);
                                long len,lensum=0;
                                InputStream dis = socket.getInputStream();
                                byte[] inputByte = new byte[1024];
                                System.out.println("开始接收数据...");
                              //  Date start=new Date();
                                new Tool().deleteFile(ScanAppService.this,filename);
                                File appDir = new File(Environment.getExternalStorageDirectory(), "AWIFI/File");
                                if (!appDir.exists()) {
                                    appDir.mkdirs();
                                }
                                File fileSave=new File(appDir,filename);
                                FileOutputStream fo=null;
                                fo =new FileOutputStream(fileSave,true);
                                while (true) {
                                    while ((len = dis.read(inputByte, 0, inputByte.length)) > 0) {
                                        //new Tool().saveFile(ScanAppService.this, inputByte, filename,len);
                                        fo.write(inputByte,0,(int)len);
                                        fo.flush();
                                        lensum+=len;
                                    }
                                    if(lensum>=length)
                                        break;
                                }
                                fo.close();
                                System.out.println("Recive File End!");
                                Bundle bundle=new Bundle();
                                bundle.putString("ip",socket.getInetAddress().toString().replace("/",""));
                                bundle.putString("file",filename);
                                Message msg=new Message();
                                msg.setData(bundle);
                                msg.what=RECIVE_FILE;
                                handler.sendMessage(msg);
                                break;
                        }
                        System.out.println(line);
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private  boolean addUsers(User user){
        if(user.getIP().equals(localIP))
            return  false;
        for(User u:users){
            if(user.getIP().equals(u.getIP()))
                return false;
        }
        if(user.getUserName()==null){
            Socket socket = null;
            try {
                socket=new Socket(user.getIP(),port);
                BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                bw.write("askName\n");
                bw.flush();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        users.add(user);
        Intent intent=new Intent("newUser");
        Bundle bundle=new Bundle();
        bundle.putSerializable("user",user);
        intent.putExtras(bundle);
        sendBroadcast(intent);
        return true;
    }

}
