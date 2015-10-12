package com.github.ziyao.myplayer.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ziyao.myplayer.entity.VideoUnit;
import com.github.ziyao.myplayer.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Ziyao on 2015/9/14.
 */
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder >{

    private Context mContext;
    private ArrayList<VideoUnit > mDataList;
    private int mImageWidth;
    private RecyclerView.LayoutParams mLayoutParams;
    private OnVideoClickListener mOnClickListener;

    public VideoListAdapter(Context context, ArrayList< VideoUnit > list){
        mContext = context;
        mDataList = list;
    }

    public void setImageWidth( int width ){
        mImageWidth = width;

        //mLayoutParams = new RecyclerView.LayoutParams( width, width );
    }

    public void setOnImageClickListener( OnVideoClickListener listener ){
        mOnClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i){
        View view = LayoutInflater.from( mContext ).inflate( R.layout.video_item_layout, viewGroup, false );

        return new ViewHolder( view );
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i){
        VideoUnit unit = mDataList.get( i );

        /*
        Picasso.with(mContext)
                .load(new File(unit.getPath()))
                .placeholder(R.mipmap.default_error)
                .resize(mImageWidth, mImageWidth)
                .centerCrop()
                .into(viewHolder.placeImage);
        */
        viewHolder.textView.setText( unit.getDisplayName() );

    }

    @Override
    public int getItemCount(){
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView placeImage;
        private CardView cardView;
        private TextView textView;
        public ViewHolder( View itemView ){
            super( itemView );
            placeImage = (ImageView)itemView.findViewById(R.id.place_image);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            textView = ( TextView )itemView.findViewById( R.id.item_text );
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            if( mOnClickListener!=null ){
                mOnClickListener.onItemClick(v, getPosition());
            }
        }
    }


    public interface OnVideoClickListener{
        void onItemClick(View v, int position);
    }
}
