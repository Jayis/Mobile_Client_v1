package com.jayis4176.mobile_client_v1;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SongListActivity extends ActionBarActivity {

    // static variable
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private SharedPreferences sharedPref;

    //
    private String SongTableName;
    private ListView listView_SongList;
    private List<Map<String, Object>> cur_SongList;
    private MyAdapter myAdapter;
    private String DownloadURL;
    private Utility tools = new Utility();
    private Activity this_act = this;
    private DownloadManager dm;
    private List<Map<String, Object>> QedSongList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        // tmp
        Intent intent = getIntent();

        // static variable initialization
        database = AutoLoginActivity.shareDB();
        dbHelper = AutoLoginActivity.shareDBHelper();
        sharedPref = AutoLoginActivity.shareSharePref();
        dm = AutoLoginActivity.shareDM();
        //
        QedSongList = new ArrayList<Map<String, Object>>();
        SongTableName = intent.getStringExtra(MainPageActivity.EXTRA_SONGTABLENAME);
        listView_SongList = (ListView) findViewById(R.id.SongList);

        this.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        //showAllSong();

        //
        cur_SongList = generateList();
        myAdapter = new MyAdapter(this, cur_SongList);
        listView_SongList.setAdapter(myAdapter);

        listView_SongList.setOnItemClickListener(
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                    String SongInfo =
                            "Album: " + cur_SongList.get(position).get("Album") + "\n" +
                                "Genre: " + cur_SongList.get(position).get("Genre") + "\n" +
                                "Artist: " + cur_SongList.get(position).get("Artist") + "\n" +
                                "FileName: " + cur_SongList.get(position).get("FileName");
                    Toast.makeText(getApplicationContext(), SongInfo, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(onComplete);
    }

    private List<Map<String, Object>> generateList () {
        Cursor cursor = database.rawQuery("SELECT * FROM " + SongTableName, null);
        cursor.moveToFirst();

        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < cursor.getCount(); i++) {
            int status;
            if (cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_LOCALURI)).compareTo("null") == 0) {
                status = 0;
            }
            else {
                status = 1;
            }
            Map<String, Object> item = new HashMap<String, Object>();

            item.put("ServerID", cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_SERVERID)));
            item.put("Album", cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ALBUM)));
            item.put("Artist", cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ARTIST)));
            item.put("Genre", cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_GENRE)));
            item.put("FileName", cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_FILENAME)));
            item.put("SongInfo", cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_TITLE)));
            item.put("status", status);
            item.put("DownLoadURI", cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_URL)));

            items.add(item);

            cursor.moveToNext();
        }

        return items;
    }

    public void showAllSong () {
        String allSong = "";

        Cursor cursor = database.rawQuery("SELECT * FROM " + SongTableName, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            allSong += ( cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_SERVERID))  + "\n");

            cursor.moveToNext();
        }

        tools.showString(allSong, this_act);
    }

    public void DL_the_song (Button this_butt, int position) {
        DownloadURL = "http://106.187.36.145:3000" + cur_SongList.get(position).get("DownLoadURI");

        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadURL));
        File destination = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(destination, (String) cur_SongList.get(position).get("FileName"));
        request.setDestinationUri(Uri.parse(file.toURI().toString()));
        long QedSong = dm.enqueue(request);

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_LOCALURI, file.getAbsolutePath() + cur_SongList.get(position).get("FileName"));
        dbHelper.updateSongTableByServerID(database, SongTableName, (Integer) cur_SongList.get(position).get("ServerID"), values );

        //tools.showString(file.getAbsolutePath(), this_act);

        Map<String, Object> item = new HashMap<String, Object>();
        item.put("queryID", QedSong);
        item.put("DL_butt", this_butt);

        QedSongList.add(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_song_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            // your code
            //this_button.setText("in Local");
            //tools.showString("complete", this_act);
            long tmp_query;
            Cursor tmp_cursor;
            int tmp_status;
            Button tmp_butt;

            for (int i = 0; i < QedSongList.size(); i++ ) {
                tmp_query = (long) QedSongList.get(i).get("queryID");
                tmp_cursor = dm.query(new DownloadManager.Query().setFilterById(tmp_query));

                if (tmp_cursor!=null) {
                    tmp_cursor.moveToFirst();

                    tmp_status = tmp_cursor.getInt(tmp_cursor.getColumnIndex(dm.COLUMN_STATUS));

                    if (tmp_status == dm.STATUS_SUCCESSFUL) {
                        //-----FUTURE WORK-----
                        // this is also where we can set local URI
                        tmp_butt = (Button) QedSongList.get(i).get("DL_butt");
                        tmp_butt.setText("in LOCAL");

                        QedSongList.remove(i);
                        break;
                    }
                }
            }
        }
    };
}
