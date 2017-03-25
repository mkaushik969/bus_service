package com.pservice.manas.busservice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText t1;
    EditText t2;
    Button btn1,btn2;
    Handler handler=new Handler();
    ProgressDialog progressDialog;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().hide();

        t1=(EditText)findViewById(R.id.la_pno);
        t2=(EditText)findViewById(R.id.la_pwd);
        btn1=(Button)findViewById(R.id.la_login_btn);
        btn2=(Button)findViewById(R.id.la_signup_btn);

        sp=getSharedPreferences("BusServices",MODE_PRIVATE);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (MyConnectivityManager.getConnectivity(LoginActivity.this)) {
                        if (t2.getText().toString().isEmpty() || t1.getText().toString().isEmpty()) {
                            Toast.makeText(LoginActivity.this, "field(s) empty", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog=new ProgressDialog(LoginActivity.this);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.setMessage("Logging In...");
                            progressDialog.setIndeterminate(true);
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("busno", t1.getText().toString());
                            jsonObject.put("password", t2.getText().toString());
                            String response = JSONParser.getParsedJson(jsonObject);

                            new LoginActivity.LoginTask().execute(response);
                            //startActivity(new Intent(LoginActivity.this,ServicesActivity.class));
                            //finish();
                        }
                    } else {
                        Snackbar.make(v, "No Internet Connection", Snackbar.LENGTH_LONG).show();
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
                finish();
            }
        });
    }

    class LoginTask extends AsyncTask<String,String,String>
    {
        String response;
        @Override
        protected String doInBackground(String... params) {
            try {

                response = ServletInterface.makeRequest(params[0], "http://www.myprojectshub.com/busbackend/login.php");

                return response;
            }
            catch (final Exception e)
            {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
    //                    Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try
            {
           //     Toast.makeText(LoginActivity.this,"Result:"+s,Toast.LENGTH_SHORT).show();

                if(s==null)
                {
                    Toast.makeText(LoginActivity.this,"Connection Problem",Toast.LENGTH_SHORT).show();
                }
                else
                {

                    JSONObject jsonObject=new JSONObject(s);

                    if(jsonObject.getString("status").equals("failure"))
                    {
                        Toast.makeText(LoginActivity.this,"Incorrect Bus No. or password",Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObject.getString("status").equals("success"))
                    {
                        SharedPreferences.Editor editor=sp.edit();
                        editor.putString("drivername",jsonObject.getString("drivername"));
                        editor.putString("stops",jsonObject.getString("stops"));
                        editor.putString("busno",t1.getText().toString());
                        editor.putString("password",t2.getText().toString());
                        editor.putString("passengers","low");
                        editor.apply();

                        startActivity(new Intent(LoginActivity.this,BusActivity.class));
                        Toast.makeText(LoginActivity.this,"Login Successful",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,"Some error occurred",Toast.LENGTH_SHORT).show();
                    }
                }

                progressDialog.dismiss();

            }
            catch (Exception e)
            {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this,"EXC:"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }

    }


}
