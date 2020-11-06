package com.example.vf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener{

    Button mButton;
    EditText mailtext, mailtext2, speedtext;

    public static final String PRESFNAME = "prefssname";
    SharedPreferences sp;
    SharedPreferences.Editor editor;


    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        checkPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE,STORAGE_PERMISSION_CODE);

        Calendar d = Calendar.getInstance();
        int currentMinute = d.getTime().getMinutes() ;

        for(int i =0;i< 61;i++){
            if( i != currentMinute){
                ContextWrapper cw1x = new ContextWrapper(getApplicationContext());
                File directory2x = cw1x.getDir("imageDir"+ i, Context.MODE_PRIVATE);

                deleteRecursive(directory2x);
            }
        }


        mButton = (Button)findViewById(R.id.startButton);
        mailtext   = (EditText)findViewById(R.id.mail1);
        mailtext2   = (EditText)findViewById(R.id.mail2);
        speedtext   = (EditText)findViewById(R.id.speedtext);


        sp = getApplicationContext().getSharedPreferences(PRESFNAME, 0);
        editor = sp.edit();

        mailtext.setText(sp.getString("mail1",""));
        mailtext2.setText(sp.getString("mail2",""));
        mButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v==mButton){

            if (mailtext.getText().toString().matches("") & mailtext2.getText().toString().matches("")){
                Toast.makeText(getApplicationContext(),"You did not enter an email",Toast.LENGTH_LONG).show();
            }
            else{
                if( isEmailValid(mailtext.getText().toString()) ){
                    editor.putString("mail1",mailtext.getText().toString());
                    editor.putString("mail2",mailtext2.getText().toString());
                    editor.apply();

                    Intent intent   = new Intent(MainActivity2.this,MainActivity.class);
                    intent.putExtra("mail", mailtext.getText().toString());
                    intent.putExtra("speed",speedtext.getText().toString());
                    startActivity(intent);
                }
                else
                    Toast.makeText(getApplicationContext(),"Invalid Email adress",Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (email.matches("")) {
            return true;
        }
        if(matcher.matches())
            return true;
        else
            return false;
    }

    // Function to check and request permission
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity2.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity2.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(MainActivity2.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }
}