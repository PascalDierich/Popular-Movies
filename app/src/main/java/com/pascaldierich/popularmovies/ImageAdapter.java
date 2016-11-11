package com.pascaldierich.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends ArrayAdapter<GridItem> {
    private static final String TAG = ImageAdapter.class.getSimpleName();

    private Context context;
    private int layoutResourceId;
    private ArrayList<GridItem> gridData = new ArrayList<GridItem>();

    public ImageAdapter(Context c, int layoutResourceId, ArrayList<GridItem> gridData){
        super(c, layoutResourceId, gridData);
        this.context = c;
        this.layoutResourceId = layoutResourceId;
        this.gridData = gridData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GridItem imgURL = gridData.get(position);
        Picasso.with(context).load(imgURL.getImage()).into(holder.imageView);

        return convertView;
    }

    static class ViewHolder{
        ImageView imageView;
    }
}
