package ustc.code.wifi.Model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.net.Socket;

/**
 * Created by zhb_z on 2016/10/30 0030.
 */

public class User implements Serializable{
    private String userName;
    private String userID;
    private String IP;
    private Bitmap head;
    private boolean isSelected;
    private Socket socket;

    public User(){}

    public User(String userName, String userID, String IP) {
        this.userName = userName;
        this.userID = userID;
        this.IP = IP;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Bitmap getHead() {
        return head;
    }

    public void setHead(Bitmap head) {
        this.head = head;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
