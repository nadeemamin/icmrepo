package org.icm.masjid;

import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Data display class
 */

public class DisplayData {


    SimpleDateFormat df = new SimpleDateFormat("MMMM dd yyyy hh:mm:ss aa");

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    public static final String inputFormat = "HH:mm a";
    SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.US);
    static int val = 1;
    JSONObject dataJson;


    /**
     * Load only and save if there is no error
     */
    public void load() {
        try {
            JSONObject jo = getJSONFromUrl();
            if (jo != null) {
                dataJson = jo;
            }
        } catch (Exception e) {
            Log.e("JSON load", "Error  " + e.toString());
        }


    }

    public String getNow() {
        Calendar c = Calendar.getInstance();
        return df.format(c.getTime());
    }

    public Date parseDate(String date) {
        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }

    public long compareTime(Date time1, Date time2) {

        return (time1.getTime() - time2.getTime()) / (1000 * 60);
    }


    public JSONObject getJSONFromUrl() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        String url = "http://www.icomd.org/rssfeed/salahtiming.asp";
        // Making HTTP request
        Log.e("Line 127", "Beginning method");
        try {


            URLConnection connection = new java.net.URL(url).openConnection();
            is = connection.getInputStream();

        } catch (Exception e) {
            Log.e("Connection failed", "Error  " + e.toString());
        }
        try {
            Log.e("Trying buffered reader", "reading now");
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line ;
            Log.e("begining while loop", "begin");
            while ((line = reader.readLine()) != null) {
                Log.e("line", line);
                sb.append(line);
                sb.append("n");
            }
            Log.e("ending while loop", "end");
            is.close();
            json = sb.toString();
            Log.e("JSON", json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }


}
