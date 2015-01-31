package com.jayis4176.mobile_client_v1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;


public class MainPageActivity extends ActionBarActivity {

    // share static
    private DefaultHttpClient client;
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private SharedPreferences sharedPref;

    // private
    private Utility tools = new Utility();
    private String lastUser;
    private Cursor cursor_lastUser;
    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private String private_SongList;
    private Button button_SongList;
    private Activity this_act = this;
    private String dumpMessage = "";

    //
    public final static String EXTRA_SONGTABLENAME = "com.jayis4176.my_first_app.songTableName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // static initialization
        client = AutoLoginActivity.shareClient();
        database = AutoLoginActivity.shareDB();
        dbHelper = AutoLoginActivity.shareDBHelper();
        sharedPref = AutoLoginActivity.shareSharePref();

        // initialization
        lastUser = sharedPref.getString("last_user", null);
        button_SongList = (Button) findViewById(R.id.seeSongList);

        dumpMessage += ("lastUser: " + lastUser + "\n");

        //
        button_SongList.setEnabled(false);

        //
        new BG_CheckSongList().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
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

    public void seeSongList (View view) {
        Intent intent = new Intent(this, SongListActivity.class);

        intent.putExtra(EXTRA_SONGTABLENAME, private_SongList);

        startActivity(intent);
    }

    private class BG_CheckSongList extends AsyncTask<String, Integer, Integer>
    {
        int needRefresh;

        @Override
        protected Integer doInBackground (String... params) {
            cursor_lastUser = database.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_USERS + " WHERE " + MySQLiteHelper.COLUMN_USERNAME + " = '" + lastUser + "'", null);
            cursor_lastUser.moveToFirst();
            dumpMessage += ("cursorLastUser: " + cursor_lastUser.getCount() + "\n");
            needRefresh = cursor_lastUser.getInt(cursor_lastUser.getColumnIndex(MySQLiteHelper.COLUMN_NEEDREFRESH));
            if (needRefresh == 1)  {
                dumpMessage += ("needRefresh: " + needRefresh + "\n");
                // need to refresh
                ContentValues values = new ContentValues();
                if (cursor_lastUser.getString(cursor_lastUser.getColumnIndex(MySQLiteHelper.COLUMN_SONGLISTTABLE)).compareTo("null") == 0) {
                    // also need to create table
                    private_SongList = lastUser + "_SongList";
                    dbHelper.createPrivateTable(database, private_SongList);
                    // update table name (SongList)
                    values.put(MySQLiteHelper.COLUMN_SONGLISTTABLE, private_SongList);
                    dumpMessage += ("nullTable\n");
                }
                else {
                    // use old table name
                    private_SongList = cursor_lastUser.getString(cursor_lastUser.getColumnIndex(MySQLiteHelper.COLUMN_SONGLISTTABLE));
                }

                // parse in JSONObject
                try {
                    jsonObject = new JSONObject(cursor_lastUser.getString(cursor_lastUser.getColumnIndex(MySQLiteHelper.COLUMN_JSONSTR)));
                    jsonArray = jsonObject.getJSONArray("file_list");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                // start refreshing SongList
                refreshSongList();

                values.put(MySQLiteHelper.COLUMN_NEEDREFRESH, 0);
                dbHelper.updateUserTableByUsername(database, lastUser, values);
            }
            else {
                dumpMessage += ("noneedRefresh\n");
                private_SongList = cursor_lastUser.getString(cursor_lastUser.getColumnIndex(MySQLiteHelper.COLUMN_SONGLISTTABLE));
            }

            return null;
        }

        @Override
        protected void onPostExecute (Integer result) {
            dumpMessage += ("privateSongList: " + private_SongList + "\n");

            button_SongList.setEnabled(true);

            //tools.showString(dumpMessage, this_act);

        }
    }

    private void refreshSongList () {
        Cursor cursor_curSong;
        JSONObject curSong;
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                dumpMessage += ("privateSongList: " + private_SongList + "\n");
                curSong = jsonArray.getJSONObject(i);
                cursor_curSong = database.rawQuery("SELECT * FROM " + private_SongList + " WHERE " + MySQLiteHelper.COLUMN_SERVERID + " = " + curSong.getInt("server_id"), null);
                cursor_curSong.moveToFirst();
                ContentValues values = new ContentValues();
                // -- attribute on json.list
                values.put(MySQLiteHelper.COLUMN_ALBUM, dbHelper.emptyStringChecker(curSong.getString("album")));
                values.put(MySQLiteHelper.COLUMN_GENRE, curSong.getString("genre"));
                values.put(MySQLiteHelper.COLUMN_SERVERID, curSong.getInt("server_id"));
                values.put(MySQLiteHelper.COLUMN_TITLE, dbHelper.emptyStringChecker(curSong.getString("title")));
                values.put(MySQLiteHelper.COLUMN_URL, curSong.getString("url"));
                values.put(MySQLiteHelper.COLUMN_ARTIST, dbHelper.emptyStringChecker(curSong.getString("artist")));
                values.put(MySQLiteHelper.COLUMN_UPLOADDATE, curSong.getString("upload_date"));
                values.put(MySQLiteHelper.COLUMN_FILENAME, curSong.getString("filename"));
                // -- attribute add by myself
                values.put(MySQLiteHelper.COLUMN_ONLIST, 1);            // to see if this song is still on the list this time
                values.put(MySQLiteHelper.COLUMN_LOCALURI, "null");    // "null" means haven't download yet

                if (cursor_curSong.getCount() <= 0) {
                    // not found on local SongList
                    database.insert(
                            private_SongList,
                            null,
                            values);
                }
                else {
                //else if (curSong.getString("upload_date").compareTo(cursor_curSong.getString(cursor_lastUser.getColumnIndex(MySQLiteHelper.COLUMN_UPLOADDATE))) != 0) {
                    // the song is not the same
                    // update
                    dbHelper.updateSongTableByServerID(database, private_SongList, curSong.getInt("server_id"), values);
                    // ---------FUTURE WORK----------
                    // delete the old file, if old localURI is not null
                }
                /**/
            }
            // delete those are not on the list
            String selection = MySQLiteHelper.COLUMN_ONLIST + " LIKE ?";
            String[] selectionArgs = { String.valueOf(0) };
            database.delete(private_SongList, selection, selectionArgs);

            // re-assign 0 to ONLIST of songs in list
            database.execSQL("UPDATE " + private_SongList + " SET " + MySQLiteHelper.COLUMN_ONLIST + " = 0");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
