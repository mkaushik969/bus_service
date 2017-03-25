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

public class SignupActivity extends AppCompatActivity {

    EditText t1,t2,t3,t4;
    Button button;
    Handler handler=new Handler();
    ProgressDialog progressDialog;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().hide();

        t1=(EditText)findViewById(R.id.sa_name);
        t2=(EditText)findViewById(R.id.sa_pno);
        t3=(EditText)findViewById(R.id.sa_pass);
        t4=(EditText)findViewById(R.id.sa_stops);

        button=(Button)findViewById(R.id.sa_btn_signup);

        sp=getSharedPreferences("BusServices",MODE_PRIVATE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(MyConnectivityManager.getConnectivity(SignupActivity.this))
                    {
                        if (t1.getText().toString().isEmpty() || t2.getText().toString().isEmpty() ||
                                t3.getText().toString().isEmpty() || t4.getText().toString().isEmpty()) {
                            Snackbar.make(v, "Field(s) Empty", Snackbar.LENGTH_LONG).show();
                        } else {
                            progressDialog=new ProgressDialog(SignupActivity.this);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.setMessage("Signing Up...");
                            progressDialog.setIndeterminate(true);
                            progressDialog.setCancelable(false);
                            progressDialog.show();


                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("drivername", t1.getText().toString());
                            jsonObject.put("busno", t2.getText().toString());
                            jsonObject.put("password", t3.getText().toString());
                            jsonObject.put("stops", t4.getText().toString());
                            jsonObject.put("latitude", "");
                            jsonObject.put("longitude", "");
                            String response = JSONParser.getParsedJson(jsonObject);
                            new SignupTask().execute(response);
                        }
                    }
                    else
                    {
                        Snackbar.make(v, "No Internet Connection", Snackbar.LENGTH_LONG).show();
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(SignupActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    class SignupTask extends AsyncTask<String,String,String>
    {
        String response;
        @Override
        protected String doInBackground(String... params) {
            try {

                response = ServletInterface.makeRequest(params[0], "http://www.myprojectshub.com/busbackend/addbus.php");

                return response;
            }
            catch (final Exception e)
            {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SignupActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(SignupActivity.this,"Result:"+s,Toast.LENGTH_SHORT).show();

                if(s==null)
                {
                    Toast.makeText(SignupActivity.this,"Connection Problem",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    JSONObject jsonObject=new JSONObject(s);

                    progressDialog.dismiss();
                    if(jsonObject.getString("status").equals("failure"))
                    {
                        Toast.makeText(SignupActivity.this,"Bus No. already registered",Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObject.getString("status").equals("success"))
                    {
                        SharedPreferences.Editor editor=sp.edit();
                        editor.putString("drivername",t1.getText().toString());
                        editor.putString("busno",t2.getText().toString());
                        editor.putString("stops",t4.getText().toString());
                        editor.putString("password",t3.getText().toString());
                        editor.putString("passengers","low");
                        editor.apply();

                        startActivity(new Intent(SignupActivity.this,BusActivity.class));
                        Toast.makeText(SignupActivity.this,"Signup Successful",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else
                    {
                        Toast.makeText(SignupActivity.this,"Some error occurred",Toast.LENGTH_SHORT).show();
                    }
                }

           }
            catch (Exception e)
            {
                progressDialog.dismiss();
                Toast.makeText(SignupActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

}
