package com.pservice.manas.busservice;

import android.app.ProgressDialog;
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

public class EditProfileActivity extends AppCompatActivity {

    EditText t1,t2,t3,t4;
    Button button;
    Handler handler=new Handler();
    ProgressDialog progressDialog;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_edit_profile);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            getSupportActionBar().setTitle("Edit Profile");

            sp=getSharedPreferences("BusServices",MODE_PRIVATE);

            t1 = (EditText) findViewById(R.id.edp_name);
            t2 = (EditText) findViewById(R.id.edp_bno);
            t3 = (EditText) findViewById(R.id.edp_pass);
            t4 = (EditText) findViewById(R.id.edp_stops);

            button = (Button) findViewById(R.id.edp_btn_signup);


            t1.setText(sp.getString("drivername",""));
            t2.setText(sp.getString("busno",""));
            t3.setText(sp.getString("password",""));
            t4.setText(sp.getString("stops",""));

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (MyConnectivityManager.getConnectivity(EditProfileActivity.this)) {
                            if (t1.getText().toString().isEmpty() || t2.getText().toString().isEmpty() ||
                                    t3.getText().toString().isEmpty() || t4.getText().toString().isEmpty()) {
                                Snackbar.make(v, "Field(s) Empty", Snackbar.LENGTH_LONG).show();
                            } else {
                                progressDialog = new ProgressDialog(EditProfileActivity.this);
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.setMessage("Please Wait...");
                                progressDialog.setIndeterminate(true);
                                progressDialog.setCancelable(false);
                                progressDialog.show();


                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("drivername", t1.getText().toString());
                                jsonObject.put("busno", t2.getText().toString());
                                jsonObject.put("password", t3.getText().toString());
                                jsonObject.put("stops", t4.getText().toString());
                                jsonObject.put("latitude", sp.getString("latitude",""));
                                jsonObject.put("longitude", sp.getString("longitude",""));
                                String response = JSONParser.getParsedJson(jsonObject);
                                new EditProfileActivity.EditProfileTask().execute(response);
                            }
                        } else {
                            Snackbar.make(v, "No Internet Connection", Snackbar.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
        catch (Exception e)
        {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    class EditProfileTask extends AsyncTask<String,String,String>
    {
        String response;
        @Override
        protected String doInBackground(String... params) {
            try {

                response = ServletInterface.makeRequest(params[0], "http://www.myprojectshub.com/busbackend/updatebus.php");

                return response;
            }
            catch (final Exception e)
            {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
                return "EXCP";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try
            {
                progressDialog.dismiss();
     //           Toast.makeText(EditProfileActivity.this,"RES:"+s,Toast.LENGTH_SHORT).show();
                JSONObject jsonObject=new JSONObject(s);

                if(jsonObject.getString("status").equals("failure"))
                {
                    Toast.makeText(EditProfileActivity.this,"Some error occurred",Toast.LENGTH_SHORT).show();
                }
                else if(jsonObject.getString("status").equals("success"))
                {
                    Toast.makeText(EditProfileActivity.this,"Saved Changes",Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor=sp.edit();
                    editor.putString("drivername",t1.getText().toString());
                    editor.putString("stops",t4.getText().toString());
                    editor.putString("busno",t2.getText().toString());
                    editor.putString("password",t3.getText().toString());
                    editor.apply();
                    finish();
                }
                else
                {
                    Toast.makeText(EditProfileActivity.this,"Some error occurred",Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e)
            {
                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }


    }

}
