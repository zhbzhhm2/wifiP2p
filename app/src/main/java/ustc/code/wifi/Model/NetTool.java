package ustc.code.wifi.Model;

/**
 * Created by zhb_z on 2016/11/1 0001.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by suxiaocheng on 3/5/15.
 */
public class NetTool {

    private static final String TAG = "NetTool";
    public  static  int PORT=33073;

    public NetTool() {
        super();
    }

    public String getLocAddress() {

        String ipaddress = "";

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所有的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface networks = en.nextElement();

                Enumeration<InetAddress> address = networks.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
                        ipaddress = ip.getHostAddress();
                    }
                }


            }
        } catch (SocketException e) {
            Log.e("", "Get the local ip address fail");
            e.printStackTrace();
        }

        //System.out.println("本机IP:" + ipaddress);
        return ipaddress;
    }

    public boolean pingCmdExec(String str) {
        boolean status = false;
        Process p;
        try {
            //ping -c 3 -w 100  中  ，-c 是指ping的次数 3是指ping 3次 ，-w 100  以秒为单位指定超时间隔，是指超时时间为100秒
            p = Runtime.getRuntime().exec("ping -c 1 -w 20 " + str);
            status = (p.waitFor() == 0) ? true : false;
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
           // Log.d(TAG, String.valueOf(buffer));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return status;
    }

    public boolean ping(String str) {
        return pingCmdExec(str);
    }

    public static int getIPNumber(String ip){
        int result = -1;
        int csEnd;
        String IPTmp;

        csEnd = ip.lastIndexOf('.');
        if (csEnd == -1) {
            return result;
        }
        IPTmp = new String(ip.substring(csEnd+1));

        result = Integer.parseInt(IPTmp);

        return result;
    }

    public static boolean compareSameSubIP(String ip1, String ip2){
        int csEnd1, csEnd2;
        String IPTmp1, IPTmp2;
        csEnd1 = ip1.lastIndexOf('.');
        csEnd2 = ip2.lastIndexOf('.');
        if((csEnd1 == -1) || (csEnd2 == -1)){
            return false;
        }

        IPTmp1 = new String(ip1.substring(0, csEnd1));
        IPTmp2 = new String(ip2.substring(0, csEnd2));
        if(IPTmp1.compareTo(IPTmp2) == 0){
            return true;
        }
        return false;
    }

    public boolean connect(String host, int port) {
        if (port == 0) port = 80;
        Socket connect=new Socket();
        try {
            connect= new Socket(host,port);
            Thread.sleep(1000);
            BufferedWriter out =new BufferedWriter(new OutputStreamWriter( connect.getOutputStream()));
            out.write("test");
            out.flush();
            return true;

        } catch (IOException e) {
            //e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally{
            try {
                connect.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean sendFile(String ip, File file){
        int length = 0;
        double sumL = 0 ;
        byte[] sendBytes = null;

        OutputStream dos = null;
        FileInputStream fis = null;
        Socket socket=null;
        try {
            socket=new Socket(ip,PORT);
            dos = socket.getOutputStream();
            BufferedWriter bf=new BufferedWriter(new OutputStreamWriter(dos));
            BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bf.write("file."+file.getName()+"."+String.valueOf(file.length())+"\n");
            bf.flush();
            while (!"accept".equals(br.readLine()));

//            byte []stringByte=("file."+file.getName()+"."+String.valueOf(file.length())+"\n").getBytes();
//            byte []out=new byte[2048];
//            for(int i=0;i<out.length;i++)
//                if(i<stringByte.length)
//                    out[i]=stringByte[i];
//                else
//                out[i]=-1;
//            dos.write(stringByte);
//            dos.flush();
            long l = file.length();

            fis = new FileInputStream(file);
            sendBytes = new byte[1024];
            while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                sumL += length;
                System.out.println("已传输：" + ((sumL / l) * 100) + "%");
                dos.write(sendBytes, 0, length);
                dos.flush();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dos != null)
                try {
                    dos.close();
                    if (fis != null)
                        fis.close();
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }
}