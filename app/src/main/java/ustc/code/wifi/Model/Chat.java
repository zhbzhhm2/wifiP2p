package ustc.code.wifi.Model;

/**
 * Created by zhb_z on 2016/10/31 0031.
 */

public class Chat {
    private String content;
    private int flag;
    public final static int SEND=0, RECEIVE =1;
    String type;
    public Chat(String content, int flag) {
        this.content = content;
        this.flag = flag;
        setType();
    }
    private void setType(){
        String []sp=content.split("\\.");
        switch (sp[sp.length-1]){
            case "png":
            case "PNG":
            case "jpg":
            case "JPG":
                type="picture";
                break;
            default:
                type="file";
                break;
        }
    }
    public String getType(){
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        setType();
    }
    public int getFlag() {
        return flag;
    }
    public void setFlag(int flag) {
        this.flag = flag;
    }
}
