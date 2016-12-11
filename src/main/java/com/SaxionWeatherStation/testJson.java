package com.SaxionWeatherStation;


import org.json.JSONObject;

/**
 * Created by German on 08.12.2016.
 */
public class testJson {
    public static void main (String args[]) {

        String jsonStr = "{'port': 1, 'counter' : 0, 'payload_raw': 'AQ==', 'payload_fields' : { 'led': 'on' }}";
        JSONObject jsonObject = new JSONObject(jsonStr);
        if(jsonObject.has("payload_fields")){
            JSONObject jsonObject2 = jsonObject.getJSONObject("payload_fields");
            if(jsonObject2.has("led")){
                String str = jsonObject2.getString("led");
                System.out.println(str);

            } else {
                // It doesn't exist, do nothing
            }
        } else {
            // It doesn't exist, do nothing
        }
    }
}
