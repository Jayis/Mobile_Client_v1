package com.jayis4176.mobile_client_v1;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by JAYIS4176 on 2015/1/31.
 */
public class MyAdapter extends BaseAdapter {
    public int TextOrButton = 0;

    private LayoutInflater adapterLayoutInflater;
    private List<Map<String, Object>> contentList;
    private Activity parentAct;

    public MyAdapter(Activity act, List<Map<String, Object>> list) {
        Context c = act.getBaseContext();
        adapterLayoutInflater = LayoutInflater.from(c);
        parentAct = act;

        contentList = list;
    }

    public int getCount() {
        return contentList.size();
    }

    public View getView (int position, View view, ViewGroup parent) {
        TagView tag;
        if(view == null){
            view = adapterLayoutInflater.inflate(R.layout.listview_element, null);
            tag = new TagView ((TextView) view.findViewById(R.id.text_SongName), (TextView) view.findViewById(R.id.status), (Button) view.findViewById(R.id.button_DL));
            view.setTag(tag);
        }
        else{
            tag = (TagView)view.getTag();
        }
        tag.DL.setOnClickListener(new ItemButton_Click(parentAct, position));

        tag.songname.setText((String) contentList.get(position).get("SongInfo"));
        if ((int) contentList.get(position).get("status") == 0) {
            // not at local
            tag.status.setText("Status: not at local");
            tag.DL.setEnabled(true);
        }
        else {
            // has local copy
            tag.status.setText("Status: local copy");
            tag.DL.setEnabled(false);
        }

        return view;
    }

    public long getItemId (int position) {

        return position;
    }

    public Object getItem (int position) {

        return null;
    }

    public class TagView{
        TextView songname;
        TextView status;
        Button DL;

        public TagView(TextView text1, TextView text2, Button button){
            this.songname = text1;
            this.status = text2;
            this.DL = button;
        }
    }

    class ItemButton_Click implements View.OnClickListener {
        private int position;

        ItemButton_Click(Activity context, int pos) {
            position = pos;
        }

        public void onClick(View v) {
            //-------FUTURE WORK-------
            // disable button
            // change status

            ((SongListActivity) parentAct).DL_the_song(v, position);
        }
    }

}
