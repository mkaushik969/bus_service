package com.pservice.manas.busservice;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Created by HP on 29-11-2016.
 */

public class JSONParser {
    public static String getParsedJson(JSONObject jsonObject) throws Exception
    {
        StringBuilder response=new StringBuilder();
        Iterator<String> iterator=jsonObject.keys();
        Boolean flag=true;

        while (iterator.hasNext())
        {
            String key=iterator.next();
            Object value=jsonObject.get(key);
            if(flag)
                flag=false;
            else
                response.append("&");

            response.append(URLEncoder.encode(key,"UTF-8"));
            response.append("=");
            response.append(URLEncoder.encode(value.toString(),"UTF-8"));

        }

        return response.toString();
    }
}