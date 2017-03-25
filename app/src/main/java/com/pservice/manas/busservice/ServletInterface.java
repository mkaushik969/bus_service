package com.pservice.manas.busservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by HP on 29-11-2016.
 */

public class ServletInterface {

    public static String makeRequest(String request,String SERVER_URL) throws Exception
    {
        String response = "",line="";

        URL url=new URL(SERVER_URL);
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept-Charset","UTF-8");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        OutputStream os= connection.getOutputStream();
        BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(request);
        writer.flush();
        writer.close();
        os.close();

        BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while ((line=reader.readLine())!=null)
        {
            response+=line;
        }
        reader.close();

        return response;
    }

    public static String uploadFile(final String selectedFilePath,String surveyid) throws Exception{

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];


        FileInputStream fileInputStream = new FileInputStream(selectedFile);
        URL url = new URL("https://visualai.io/App/index.php");
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);//Allow Inputs
        connection.setDoOutput(true);//Allow Outputs
        connection.setUseCaches(false);//Don't use a cached Copy
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("ENCTYPE", "multipart/form-data");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        connection.setRequestProperty("uploaded_file",selectedFilePath);
        // connection.setRequestProperty("action","image_upload");
        //  connection.setRequestProperty("survey_id",surveyid);

        //creating new dataoutputstream

        dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + selectedFilePath + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        //returns no. of bytes present in fileInputStream
        bytesAvailable = fileInputStream.available();
        //selecting the buffer size as minimum of available bytes or 1 MB
        bufferSize = Math.min(bytesAvailable,maxBufferSize);
        //setting the buffer as byte array of size of bufferSize
        buffer = new byte[bufferSize];

        //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
        bytesRead = fileInputStream.read(buffer,0,bufferSize);

        //loop repeats till bytesRead = -1, i.e., no bytes are left to read
        while (bytesRead > 0){
            //write the bytes read from inputstream
            dataOutputStream.write(buffer,0,bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable,maxBufferSize);
            bytesRead = fileInputStream.read(buffer,0,bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        serverResponseCode = connection.getResponseCode();
        String serverResponseMessage = connection.getResponseMessage();

                /*response code of 200 indicates the server status OK
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvFileName.setText("File Upload completed.\n\n You can see the uploaded file here: \n\n" + "http://coderefer.com/extras/uploads/"+ fileName);
                        }
                    });
                }*/

        //closing the input and output streams
        fileInputStream.close();
        dataOutputStream.flush();
        dataOutputStream.close();

        String response="",line="";
        BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while ((line=reader.readLine())!=null)
        {
            response+=line;
        }
        reader.close();

        return response;



    }

}
