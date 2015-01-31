package com.jayis4176.mobile_client_v1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by JAYIS4176 on 2015/1/30.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    // ----------------------------USER DATABASE--------------------------------
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_JSONSTR = "json_string";
    public static final String COLUMN_SONGLISTTABLE = "private_table";
    public static final String COLUMN_NEEDREFRESH = "needRefresh";

    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE =
            "create table " + TABLE_USERS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_USERNAME + " text," +
                    COLUMN_PASSWORD + " text," +
                    COLUMN_JSONSTR + " text," +
                    COLUMN_SONGLISTTABLE + " text," +
                    COLUMN_NEEDREFRESH + " integer" +
                    ")";
    // ----------------------------PRIVATE SONG DATABASE--------------------------------
    //public static final String COLUMN_ID = "_id"; (already created by USER DATABASE)
    public static final String COLUMN_ALBUM = "album";
    public static final String COLUMN_GENRE = "genre";
    public static final String COLUMN_SERVERID = "serverID";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_UPLOADDATE = "uploadDate";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_LOCALURI = "localURL";
    public static final String COLUMN_ONLIST = "onlist";

    private static final String PRIVATE_SONG_DATABASE_CREATE =
            "(" + COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_ALBUM + " text," +
                    COLUMN_GENRE + " text," +
                    COLUMN_SERVERID + " integer," +
                    COLUMN_TITLE + " text," +
                    COLUMN_URL + " text," +
                    COLUMN_ARTIST + " text," +
                    COLUMN_UPLOADDATE + " text," +
                    COLUMN_FILENAME + " text," +
                    COLUMN_LOCALURI + " text," +
                    COLUMN_ONLIST + " text" +
                    ")";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public void createPrivateTable (SQLiteDatabase db, String private_songListName) {
        db.execSQL("create table " + private_songListName + PRIVATE_SONG_DATABASE_CREATE);
    }

    public int updateUserTableByUsername (SQLiteDatabase db, String cur_username, ContentValues values) {
        // selection
        String selection = COLUMN_USERNAME + " LIKE ?";
        String[] selectionArgs = { String.valueOf(cur_username) };
        // update
        int count = db.update(
                MySQLiteHelper.TABLE_USERS,
                values,
                selection,
                selectionArgs);

        return count;
    }

    public int updateSongTableByServerID (SQLiteDatabase db, String tableName, int serverID, ContentValues values) {
        // selection
        String selection = COLUMN_SERVERID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(serverID) };
        // update
        int count = db.update(
                tableName,
                values,
                selection,
                selectionArgs);

        return count;
    }

    public String showSongAllInfo (Cursor cursor) {
        String SongInfo =
                "Album: " + cursor.getString(cursor.getColumnIndex(COLUMN_ALBUM)) + "\n" +
                        "Genre: " + cursor.getString(cursor.getColumnIndex(COLUMN_GENRE)) + "\n" +
                        "Artist: " + cursor.getString(cursor.getColumnIndex(COLUMN_ARTIST)) + "\n" +
                        "FileName: " + cursor.getString(cursor.getColumnIndex(COLUMN_FILENAME));

        return SongInfo;
    }

    public String emptyStringChecker (String in) {
        if (in.compareTo("") == 0) {
            return "You Never Told Me...";
        }
        else {
            return in;
        }

    }
}
