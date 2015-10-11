package com.github.ziyao.myplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity{



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if( id == R.id.action_settings ){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void playAction( View v ){
        EditText ipText = (EditText)findViewById( R.id.ip_text );
        String ip = ipText.getText().toString();
        String uri;
        if( ip!=null && !"".equals(ip) ){
            uri = "http://192.168.1."+ip + ":8080/source/car-20120827-manifest.mpd";
        }else{
            uri = "http://192.168.1.107:8080/source/car-20120827-manifest.mpd";
        }

        String contentId = "youtube";
        int type = PlayerActivity.TYPE_DASH;
        Intent intent = new Intent( MainActivity.this, PlayerActivity.class )
                .setData(Uri.parse(uri))
                .putExtra( PlayerActivity.CONTENT_ID_EXTRA, contentId)
                .putExtra(PlayerActivity.CONTENT_TYPE_EXTRA,type );

        startActivity( intent  );
    }
}
