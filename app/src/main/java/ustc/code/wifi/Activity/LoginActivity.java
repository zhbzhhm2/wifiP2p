package ustc.code.wifi.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ustc.code.wifi.R;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private final int LOGIN_SUCCESS=0,LOGIN_FAIL=1;
    private EditText nameEdit,passwordEdit;
    private  Button loginButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }
    private void initView(){
        nameEdit =(EditText)findViewById(R.id.name);
        passwordEdit=(EditText)findViewById(R.id.password);
        loginButton=(Button)findViewById(R.id.name_sign_in_button);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (checkLogin(nameEdit.getText().toString(),passwordEdit.getText().toString())){
                    case LOGIN_SUCCESS:
                        Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                        Intent intent   =new Intent(LoginActivity.this,MainScreen.class);
                        intent.putExtra("name",nameEdit.getText().toString());
                        startActivity(intent);
                        LoginActivity.this.finish();
                        break;
                }
            }
        });
    }
    private int checkLogin(String name,String password){
        if(name.length()==0)
            return LOGIN_FAIL;

        return LOGIN_SUCCESS;
    }


}

