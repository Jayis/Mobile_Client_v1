package com.jayis4176.mobile_client_v1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


public class AutoLoginActivity extends ActionBarActivity {

    // static variable
    private static DefaultHttpClient client = new DefaultHttpClient();
    public static Boolean isThereNetwork;
    private static SQLiteDatabase database;
    private static MySQLiteHelper dbHelper;
    private static SharedPreferences sharedPref;

    //
    private JSONObject jsonObject;
    private Utility tools = new Utility();
    private Activity this_act = this;
    private String lastUser = "";
    private Cursor cursor_lastUser = null;

    // URLs
    public static String url_site = "http://106.187.36.145:3000";
    public static String url_list_json = url_site + "/list.json";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isThereNetwork = isConnected();
        dbHelper = new MySQLiteHelper(this);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        if ( isThereNetwork ) new BG_IfLogin().execute();
        else tools.showString("No Network!!", this_act);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_auto_login, menu);
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

    public void goto_LastLogin(){
        Intent intent = new Intent(this, LastLoginActivity.class);
        startActivity(intent);
    }

    private class BG_IfLogin extends AsyncTask<String, Integer, Integer>
    {
        HttpResponse response;
        HttpEntity resEntity;
        String tmp_response = "";

        @Override
        protected Integer doInBackground (String... params) {
            database = dbHelper.getWritableDatabase();
            try {
                HttpGet request_IfLogin = new HttpGet(url_list_json);
                response = client.execute(request_IfLogin);
                resEntity = response.getEntity();
                if (resEntity != null) {
                    tmp_response = EntityUtils.toString(resEntity);
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute (Integer result) {
            try {
                jsonObject = new JSONObject(tmp_response);
                if ( jsonObject.getInt("login")==1 ) {
                    lastUser = sharedPref.getString("last_user", null);
                    cursor_lastUser = database.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_USERS + " WHERE " + MySQLiteHelper.COLUMN_USERNAME + " = '" + lastUser + "'", null);
                    cursor_lastUser.moveToFirst();
                    String tmp_json_str = cursor_lastUser.getString(cursor_lastUser.getColumnIndex(MySQLiteHelper.COLUMN_JSONSTR));
                    ContentValues values = new ContentValues();
                    if (tmp_json_str.compareTo(tmp_response) != 0) {
                        values.put(MySQLiteHelper.COLUMN_NEEDREFRESH, 1);
                        values.put(MySQLiteHelper.COLUMN_JSONSTR, tmp_response);
                        dbHelper.updateUserTableByUsername(database, lastUser, values);
                    }
                }
                else goto_LastLogin();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static MySQLiteHelper shareDBHelper () { return  dbHelper; }
    public static SQLiteDatabase shareDB () { return  database; }
    public static DefaultHttpClient shareClient () {
        return client;
    }
    public static SharedPreferences shareSharePref () {
        return sharedPref;
    }
}
