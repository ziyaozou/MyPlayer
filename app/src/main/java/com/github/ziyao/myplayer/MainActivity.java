package com.github.ziyao.myplayer;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.github.ziyao.fablibrary.FloatingActionButton;
import com.github.ziyao.myplayer.adapter.VideoListAdapter;
import com.github.ziyao.myplayer.entity.VideoUnit;
import com.github.ziyao.myplayer.utils.ContentUtils;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity{

    private ArrayList< VideoUnit > list;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;

    private ContentUtils mContentUtils;

    private FloatingActionButton fButton;
    private FloatingActionButton cButton;
    private ProgressBarCircularIndeterminate loadingView;

    private AsyncTask myTask;

    private ImageView realImage;
    private EditText editText;

    private boolean isPlay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        findView();
    }

    public void init(){
        list = new ArrayList< VideoUnit >();
        mContentUtils = new ContentUtils(this);
        mContentUtils.getVideoFile(list, Environment.getExternalStorageDirectory());

        myTask = new MyTask();
        myTask.execute();
    }

    public void findView(){
        Resources res = getResources();
        fButton = ( FloatingActionButton ) findViewById(R.id.buttonFloat);
        cButton = ( FloatingActionButton ) findViewById(R.id.cFloat);
        fButton.setColor(res.getColor(R.color.floating_button_normal), res.getColor(R.color.floating_button_press));
        cButton.setColor(res.getColor(R.color.cancle_button_normal), res.getColor(R.color.floating_button_press));
        fButton.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v){
                if( isPlay ){
                    playAction(v);
                }else{
                    showInput(v);
                }
            }
        });

        cButton.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v){
                fButton.setImageResource( R.mipmap.ic_add_white_36dp );
                editText.setVisibility(View.INVISIBLE);
                realImage.setBackgroundColor(getResources().getColor(R.color.background_color));
                cButton.setVisibility( View.INVISIBLE );
                isPlay = false;
            }
        });

        loadingView = ( ProgressBarCircularIndeterminate ) findViewById(R.id.loading_progress);
        realImage = (ImageView)findViewById( R.id.real_image );
        editText = ( EditText )findViewById(R.id.ip_text);
        recyclerView = ( RecyclerView ) findViewById(R.id.recycler_view);
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mStaggeredGridLayoutManager);
        recyclerView.setHasFixedSize(true);




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

    public void playAction(View v){

        String ip = editText.getText().toString();
        String uri;
        if( ip != null && !"".equals(ip) ){
            uri = "http://192.168.1." + ip + ":8080/source/car-20120827-manifest.mpd";
        }else{
            uri = "http://192.168.1.107:8080/source/car-20120827-manifest.mpd";
        }

        String contentId = "youtube";
        int type = PlayerActivity.TYPE_DASH;
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class)
                .setData(Uri.parse(uri))
                .putExtra(PlayerActivity.CONTENT_ID_EXTRA, contentId)
                .putExtra(PlayerActivity.CONTENT_TYPE_EXTRA, type);

        startActivity(intent);
    }

    public void showInput(View v){
        editText.setVisibility(View.VISIBLE);
        cButton.setVisibility( View.VISIBLE );
        fButton.setImageResource(R.mipmap.ic_play_arrow_white_36dp);
        realImage.setBackgroundColor(getResources().getColor(R.color.background_color2));
        isPlay = true;
    }

    public class MyTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params){
            mContentUtils.getVideoFile(list, Environment.getExternalStorageDirectory());
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values){
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o){
            super.onPostExecute(o);
            loadingView.setVisibility(View.INVISIBLE);
            VideoListAdapter adapter = new VideoListAdapter(MainActivity.this, list);
            int imageWidth = getResources().getDimensionPixelSize(R.dimen.image_size);
            adapter.setImageWidth(imageWidth);


            VideoListAdapter.OnVideoClickListener mOnClickListener = new VideoListAdapter.OnVideoClickListener(){

                @Override
                public void onItemClick(View v, int position){
                    //Toast.makeText(ImageActivity.this, "" + position, Toast.LENGTH_SHORT).show();

                    VideoUnit unit = list.get(position);
                    Intent intent = new Intent(MainActivity.this, PlayerActivity.class)
                            .setData(Uri.parse(unit.getPath()))
                            .putExtra(PlayerActivity.CONTENT_ID_EXTRA, "local")
                            .putExtra(PlayerActivity.CONTENT_TYPE_EXTRA, PlayerActivity.TYPE_OTHER);
                    startActivity(intent);
                }
            };
            adapter.setOnImageClickListener(mOnClickListener);

            recyclerView.setAdapter(adapter);
        }
    }
}
