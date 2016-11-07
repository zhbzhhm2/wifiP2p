package ustc.code.wifi.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import ustc.code.wifi.Model.Tool;
import ustc.code.wifi.R;

public class ImageProcessingActivity extends Activity {
    File picture;
    int dpi;
    Bitmap image=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processing);
        intiView();
        DisplayMetrics metric = getResources().getDisplayMetrics();
        dpi=metric.densityDpi;
    }
    ImageView imageView;
    private void intiView(){
        imageView=(ImageView)findViewById(R.id.imageViewDetail);
        Button cut=(Button)findViewById(R.id.buttonImageCut);
        Button save=(Button)findViewById(R.id.buttonImageSave);
        picture=(File)getIntent().getExtras().getSerializable("file");
        final Bitmap bitmap = BitmapFactory.decodeFile(picture.getPath());
        imageView.setImageBitmap(bitmap);

        cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageProcessingActivity.this);
                builder.setTitle("cut to:");
                //    指定下拉列表的显示数据
                final String[] inch = {"1 inch", "2 inch"};
                //    设置一个下拉的列表选择项
                builder.setItems(inch, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        double x=0,y=0;
                        switch (which){
                            case 0:
                                x=1;
                                y=1.5;
                                break;
                            case 1:
                                x=1.5;
                                y=2;
                                break;
                        }
                        Intent intent=new Intent("com.android.camera.action.CROP");
                        intent.setAction("com.android.camera.action.CROP");
                        intent.setDataAndType(Uri.fromFile(picture), "image/*");// mUri是已经选择的图片Uri
                        intent.putExtra("crop", "true");
                        intent.putExtra("aspectX", (int)(x*2));// 裁剪框比例
                        intent.putExtra("aspectY", (int)(y*2));
                        intent.putExtra("outputX", (int)(x*dpi/3));// 输出图片大小
                        intent.putExtra("outputY", (int)(y*dpi/3));
                        intent.putExtra("return-data", true);

                        ImageProcessingActivity.this.startActivityForResult(intent, which);
                        Toast.makeText(ImageProcessingActivity.this, "cut to" + inch[which], Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file=null;
                if(image!=null){
                    file= Tool.saveImage(ImageProcessingActivity.this,image);
                    Toast.makeText(ImageProcessingActivity.this,"Save Successful!",Toast.LENGTH_SHORT).show();
                }
                Intent intent=new Intent();
                intent.putExtra("file",file);
                ImageProcessingActivity.this.setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK)
            return;
        Bitmap bitmap = data.getParcelableExtra("data");
        bitmap=drawPicture(bitmap,requestCode);
        imageView.setImageBitmap(bitmap);
        image=bitmap;

    }

    private Bitmap drawPicture(Bitmap bitmap,int size){
        int width=bitmap.getWidth();
        int high=bitmap.getHeight();
        double sp=210.0/297;
        Bitmap newBitmap=null;
        if(size==0){
            newBitmap=Bitmap.createBitmap(width*5,(int)(width*5*sp), Bitmap.Config.ARGB_8888);
            int wkon=width/5;
            int hkon=(((int)(width*5*sp))-high*2)/3;
            Canvas canvas = new Canvas(newBitmap);
            for(int i=0;i<2;i++){
                for(int j=0;j<4;j++) {
                    canvas.drawBitmap(bitmap, wkon*(j+1)+width*j, hkon*(i+1)+high*i, null);
                }
            }
        }else if(size==1){
            newBitmap=Bitmap.createBitmap(width*3,(int)(width*3/sp),Bitmap.Config.ARGB_8888);
            int wkon=width/4;
            int hkon=(((int)(width*3/sp))-high*2)/4;
            Canvas canvas = new Canvas(newBitmap);
            for(int i=0;i<2;i++){
                for(int j=0;j<2;j++) {
                    canvas.drawBitmap(bitmap, wkon*(j+1)+width*j, hkon*(i+1)+high*i, null);
                }
            }
        }
        return newBitmap;
    }

}
