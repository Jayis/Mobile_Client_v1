package com.jayis4176.mobile_client_v1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class SongListActivity extends ActionBarActivity {

    // static variable
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private SharedPreferences sharedPref;

    //
    private String SongTableName;

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

        //
        SongTableName = intent.getStringExtra(MainPageActivity.EXTRA_SONGTABLENAME);
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
}
