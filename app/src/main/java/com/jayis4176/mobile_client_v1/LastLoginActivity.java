package com.jayis4176.mobile_client_v1;

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
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LastLoginActivity extends ActionBarActivity {
    private Utility tools = new Utility();

    // share static
    private DefaultHttpClient client;
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private SharedPreferences sharedPref;

    //
    private List<NameValuePair> form_data;
    private Cursor cursor_lastUser = null;
    private String lastUser = "";
    private String lastPassword = "";
    private String csrf = "";
    private String Notify = "";
    private Cursor cursor_curUser = null;
    private String cur_username = "";
    private String cur_password = "";
    private String JSON_str = "";
    private JSONObject jsonObject;
    private TextView textView_notSuccess;
    private TextView textView_areYou;
    private Button button_resumeLogin;
    private boolean record_or_not;
    private String dumpMessage;

    // URLs
    private String url_list_json = AutoLoginActivity.url_list_json;
    private String url_login = AutoLoginActivity.url_site + "/accounts/login/";
    private String url_auth = AutoLoginActivity.url_site + "/accounts/auth/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_login);

        // share static initialize
        client = AutoLoginActivity.shareClient();
        database = AutoLoginActivity.shareDB();
        dbHelper = AutoLoginActivity.shareDBHelper();
        //sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        sharedPref = AutoLoginActivity.shareSharePref();

        // initialization
        form_data = new ArrayList<NameValuePair>();
        textView_notSuccess = (TextView) findViewById(R.id.not_success);
        textView_areYou = (TextView) findViewById(R.id.are_you);
        button_resumeLogin = (Button) findViewById(R.id.resume_login);
        dumpMessage = getIntent().getStringExtra(AutoLoginActivity.EXTRA_DUMP);

        // main
        if ( seekLastUser() ) {
            Notify = "Are you " + lastUser + " ?\nIf YES, plz resume Login\nIf NO, plz Re-Login";
            textView_areYou.setText(Notify);
        }
        else {
            Notify = "Please Login~\nThere is no Last User";
            textView_areYou.setText(Notify);
            button_resumeLogin.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_last_login, menu);
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

    public void login (View view) {
        EditText ET_username = (EditText) findViewById(R.id.username);
        cur_username = ET_username.getText().toString();
        EditText ET_password = (EditText) findViewById(R.id.password);
        cur_password = ET_password.getText().toString();

        textView_notSuccess.setText("");

        new BG_Login().execute();
    }

    public void last_login (View view) {
        cur_username = lastUser;
        cur_password = lastPassword;
        new BG_Login().execute();
    }

    private boolean seekLastUser () {
        lastUser = sharedPref.getString("last_user", null);

        if ( lastUser!= null) {
            cursor_lastUser = database.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_USERS + " WHERE " + MySQLiteHelper.COLUMN_USERNAME + " = '" + lastUser + "'", null);

            if (cursor_lastUser.getCount() > 0) {
                // find one
                cursor_lastUser.moveToFirst();

                lastPassword = cursor_lastUser.getString(cursor_lastUser.getColumnIndex(MySQLiteHelper.COLUMN_PASSWORD));
                dumpMessage += ("findolduser" + lastUser + "\nwith password: " + lastPassword);
                return true;
            }
        }

        dumpMessage += ("not finding old users\n");

        return false;
    }

    private void recordLastUser () {
        // write to preference
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("last_user", cur_username);
        editor.commit();

        // new values for column
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PASSWORD, cur_password);

        cursor_curUser = database.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_USERS + " WHERE " + MySQLiteHelper.COLUMN_USERNAME + " = '" + cur_username + "'", null);
        cursor_curUser.moveToFirst();
        if ( cursor_curUser.getCount() > 0 ) {
            // find one
            // see if JSON_string changed
            String tmp_json_str = cursor_curUser.getString(cursor_curUser.getColumnIndex(MySQLiteHelper.COLUMN_JSONSTR));
            if (tmp_json_str.compareTo(JSON_str) == 0) {
                // same JSON str, so no need to refresh Database
                values.put(MySQLiteHelper.COLUMN_NEEDREFRESH, 0);
            }
            else {
                values.put(MySQLiteHelper.COLUMN_NEEDREFRESH, 1);
                values.put(MySQLiteHelper.COLUMN_JSONSTR, JSON_str);
            }
            dbHelper.updateUserTableByUsername(database, cur_username, values);

        }
        else {
            // it's a new user
            values.put(MySQLiteHelper.COLUMN_JSONSTR, JSON_str);
            values.put(MySQLiteHelper.COLUMN_USERNAME, cur_username);
            values.put(MySQLiteHelper.COLUMN_NEEDREFRESH, 1);
            values.put(MySQLiteHelper.COLUMN_SONGLISTTABLE, "null");
            database.insert(
                    MySQLiteHelper.TABLE_USERS,
                    null,
                    values);
        }

    }

    public void goto_MainPage(){
        Intent intent = new Intent(this, MainPageActivity.class);
        startActivity(intent);
    }

    private class BG_Login extends AsyncTask<String, Integer, Integer>
    {
        HttpResponse response;
        HttpEntity resEntity;
        String tmp_response = "";

        @Override
        protected Integer doInBackground (String... param) {
            try {

                // request url_login to get csrf
                HttpGet http_login_request = new HttpGet(url_login);
                response = client.execute(http_login_request);
                getCSRF(client);
                // prepare login parameter
                form_data.add(new BasicNameValuePair("username", cur_username));
                form_data.add(new BasicNameValuePair("password", cur_password));
                form_data.add(new BasicNameValuePair("csrftoken", csrf));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(form_data, HTTP.UTF_8);
                // send post to url_auth
                HttpPost http_auth_request = new HttpPost(url_auth);
                http_auth_request.setEntity(ent);
                response = client.execute(http_auth_request);
                resEntity = response.getEntity();
                // send get to url_list_json to check login and json_string
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
            JSON_str = tmp_response;
            try {
                jsonObject = new JSONObject(JSON_str);
                if (jsonObject.getInt("login") == 1) {
                    //record_user
                    recordLastUser();
                    goto_MainPage();
                }
                else {
                    // fail login
                    textView_notSuccess.setText("login fail..., plz retry");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private boolean getCSRF(DefaultHttpClient httpClient) {
        boolean success = false;

        List<Cookie> cookies = httpClient.getCookieStore().getCookies();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie cookie = cookies.get(i);
            if (cookie.getName() == "csrftoken") {
                csrf = cookie.getValue();
                success = true;
            }
        }

        return success;
    }

}
