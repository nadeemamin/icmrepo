package org.icm.masjid;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;


public class DisplayTime extends AppCompatActivity {
    static long REFRESH_INTERVEL = 30 * 60 * 1000;

    static float size = 40;
    static int HIJRI_YEAR = 13;
    static int HIJRI_MONTH = 14;

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    String spacesize = "  ";
    Hashtable<String, TextView> viewMap = new Hashtable<>();
    Hashtable<String, Integer> prayerTimes = new Hashtable<>();
    public static final String inputFormat = "HH:mm a";
    SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.US);
    static int val = 1;
    DisplayData displayData = new DisplayData();
    String sideSpace = spacesize + spacesize + spacesize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_display_time);


        setTitle("                                                                  Islamic Center of Maryland Masjid Salah and Jummah Timing");
        displayData.load();
        launchTimeThread();

        createMainTable(displayData.dataJson);
        createSideTable(displayData.dataJson);
    }

    private void launchTimeThread() {
        Thread thread = new Thread() {
            long timer = System.currentTimeMillis();

            @Override
            public void run() {
                Log.e(" thread", " launch");
                try {
                    while (true) {
                        Thread.sleep(1000);
                        if (timer + REFRESH_INTERVEL < System.currentTimeMillis()) {
                            Log.d("Time thread", "update time");
                            timer = System.currentTimeMillis();
                            displayData.load();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                            refreshPrayTIme(displayData.dataJson);
                            refreshSideTable(displayData.dataJson);
                                }
                            });
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView dateDisplay = findViewById(R.id.dateDisplay);
                                dateDisplay.setText(displayData.getNow());
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("Time thread", "Error ", e);
                }
            }
        };
        thread.start();
    }


    public void refreshPrayTIme(JSONObject jo) {
        try {
            JSONArray jummah , icmSalah;
            jummah = jo.getJSONArray("Jummah");
            icmSalah = jo.getJSONArray("DailySalah");

            for (int i = 0; i < icmSalah.length(); i++) {
                JSONObject item = icmSalah.getJSONObject(i);
                String Salah = item.getString("Salah");
                String starts = item.getString("starts");
                String iqama = item.getString("iqama");
                int starts_id = (i + 1) * 10;
                int iqama_id = starts_id + i + 1;
                TextView startsView = findViewById(starts_id);
                TextView iqamaView = findViewById(iqama_id);

                Log.e("Salah Val", Salah);
                startsView.setText(starts);
                iqamaView.setText(iqama);
                Log.e("End refresh", "Refreshing complete");
            }
        } catch (Exception e) {
            Log.e("display", "Error  " + e.toString());
        }
    }

    public void refreshSideTable(JSONObject jo) {
        String year = "2018";
        String month = "10";
        try {
            year = jo.getString("HijriYear");
            month = jo.getString("HijriMonth");
            TextView hyearv = findViewById(HIJRI_YEAR);
            TextView hmonthv = findViewById(HIJRI_MONTH);
            hyearv.setText(year);
            hmonthv.setText(month);
        } catch (Exception e) {
            Log.e("hijri date refresh ", "Error  " + e.toString());
        }
    }


    public void emailAlert(String message) {

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"nadeem@icomd.org"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Error Occurred in display-time ");
        i.putExtra(Intent.EXTRA_TEXT, message);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e("email ", "Error  " + ex.toString());
        }

    }


    private TextView createCell(String text, TableRow row) {
        return createCell(text, row, false);

    }


    private TextView createCell(String text, TableRow row, Boolean reverse) {

        TextView view = new TextView(this);
        view.setText(text);
        if (reverse) {
            view.setTextColor(getResources().getColor(R.color.charcoal));
            view.setAllCaps(true);
            view.setTypeface(view.getTypeface(), Typeface.BOLD);
            view.setBackgroundColor(getResources().getColor(R.color.lightblue));
        } else {
            view.setTextColor(getResources().getColor(R.color.charcoal));
            view.setBackgroundColor(getResources().getColor(R.color.white));

        }
        view.setTextSize(size);
        row.addView(view);
        viewMap.put(text, view);

        return view;
    }


    public void createMainTable(JSONObject jo) {

        //TableLayout ll = (TableLayout) findViewById(R.id.table_main);
        Log.e("Start create Main","");

        TableLayout table = findViewById(R.id.table_main);

        TableRow row = new TableRow(this);
        createCell(spacesize, row, Boolean.FALSE);
        createCell("Prayer", row, Boolean.TRUE);
        createCell(spacesize, row, Boolean.FALSE);
        createCell("Starts", row, Boolean.TRUE);
        createCell(spacesize, row, Boolean.FALSE);
        createCell("Iqama", row, Boolean.TRUE);
        table.addView(row);

        JSONArray jummah = null, icmSalah = null;
        try {
            jummah = jo.getJSONArray("Jummah");
            icmSalah = jo.getJSONArray("DailySalah");
            Calendar now = Calendar.getInstance();


            SimpleDateFormat df = new SimpleDateFormat("MMMM dd yyyy hh:mm:ss aa");
            int hour = now.get(Calendar.HOUR);
            int minute = now.get(Calendar.MINUTE);
            int apm = now.get(Calendar.AM_PM);
            Date date = displayData.parseDate(hour + ":" + minute + apm);


            int distance = 0;
            for (int i = 0; i < icmSalah.length(); i++) {
                try {
                    Log.e("Start create Main","working on "+i);
                    JSONObject item = icmSalah.getJSONObject(i);

                    row = new TableRow(this);
                    createCell(spacesize, row, Boolean.FALSE);
                    String Salah = item.getString("Salah");
                    String starts = item.getString("starts");
                    String iqama = item.getString("iqama");

                    createCell(Salah, row, Boolean.FALSE);
                    createCell(spacesize, row, Boolean.FALSE);
                    int starts_id = (i + 1) * 10;
                    int iqama_id = starts_id + i + 1;


                    // Date namazTime = displayData.parseDate(starts);

                    //prayerTimes.put(Salah, (i + 1));
                    //Log.e("Begin Comparison", starts);
                    //Log.e("Comparing", namazTime + " " + namazTime + " vs " + df.format(now.getTime()));
                    //compareTime(df.format(now.getTime()), namazTime);

                    createCell(starts, row).setId(starts_id);
                    Log.e("IDS", starts + " id: " + starts_id);
                    createCell(spacesize, row, Boolean.FALSE);
                    createCell(iqama, row).setId(iqama_id);
                    Log.e("IDS", iqama + " id: " + iqama_id);
                    table.addView(row);
                    Log.d("icmSalah # " + i + "=", Salah + "-" + starts + "-" + iqama);
                } catch (Exception e) {
                    Log.e("Error in salah loop", e.getMessage());
                }

            }


            table = findViewById(R.id.table_main);
            row = new TableRow(this);
            createCell(spacesize, row, Boolean.FALSE);
            createCell("Jummah Site", row, Boolean.TRUE);
            createCell(spacesize, row, Boolean.FALSE);
            createCell("Khatib", row, Boolean.TRUE);
            createCell(spacesize, row, Boolean.FALSE);
            createCell("Starts", row, Boolean.TRUE);
            table.addView(row);
            for (int i = 0; i < jummah.length(); i++) {
                try {
                    JSONObject item = jummah.getJSONObject(i);
                    Log.e("Start create Main","Jummah on "+i);
                    String location = item.getString("location");
                    String khatib = item.getString("khatib");
                    String time = item.getString("time");
                    if (location != null && location.toUpperCase().contains("ICM") ) {
                        row = new TableRow(this);
                        createCell(spacesize, row, Boolean.FALSE);
                        createCell(location, row);
                        createCell(spacesize, row, Boolean.FALSE);
                        createCell(khatib, row);
                        createCell(spacesize, row, Boolean.FALSE);
                        createCell(time, row);
                        table.addView(row);
                    }
                    Log.d("jummah # " + i + "=", location + "-" + khatib + "-" + time);
                } catch (Exception e) {
                    Log.e("Error in jummah loop", e.getMessage());
                }

            }
        } catch (Exception e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }


    }


    private void createSideTable(JSONObject jo) {
        String year = "2018";
        String month = "10";
        Log.e("Start side table","started");
        try {
            year = jo.getString("HijriYear");
            month = jo.getString("HijriMonth");
        } catch (Exception e) {
            Log.e("hijri date load ", "Error parsing data " + e.toString());
        }

        TableLayout table = findViewById(R.id.table_side);
        Log.e("Start side table","create table");

        TableRow row = new TableRow(this);
        table.addView(row);
        createSideSpaceCell(row, 3);
        row = new TableRow(this);
        table.addView(row);
        createSideSpaceCell(row, 3);

        row = new TableRow(this);
        table.addView(row);

        createCell(sideSpace, row, Boolean.FALSE);
        createCell("Islamic Year", row, Boolean.TRUE);
        createCell(sideSpace, row, Boolean.FALSE);

        row = new TableRow(this);
        table.addView(row);
        createCell(sideSpace, row, Boolean.FALSE);
        int id = 13;
        createCell(year, row, Boolean.FALSE).setId(id);
        createCell(sideSpace, row, Boolean.FALSE);

        row = new TableRow(this);
        table.addView(row);
        createCell(sideSpace, row, Boolean.FALSE);

        createCell("Month", row, Boolean.TRUE);
        createCell(sideSpace, row, Boolean.FALSE);

        row = new TableRow(this);
        table.addView(row);
        createCell(sideSpace, row, Boolean.FALSE);

        id = 14;
        createCell(month, row, Boolean.FALSE).setId(id);
        createCell(sideSpace, row, Boolean.FALSE);

        row = new TableRow(this);
        table.addView(row);
        createSideSpaceCell(row, 3);
        row = new TableRow(this);
        table.addView(row);
        createSideSpaceCell(row, 3);

        row = new TableRow(this);
        table.addView(row);
        createSideSpaceCell(row, 3);

    }

    private void createSideSpaceCell(TableRow row, int l) {
        for (int i = 0; i < l; ++i) {
            createCell(sideSpace, row, Boolean.FALSE);
        }
    }

}
