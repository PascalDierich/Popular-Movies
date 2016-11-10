package com.pascaldierich.popularmovies;

import android.content.Intent;
import android.media.Rating;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_beta);

        Intent detailIntent = this.getIntent();

        TextView titleView = (TextView) findViewById(R.id.textViewTitle);
        TextView plotView = (TextView) findViewById(R.id.textViewPlot);
        TextView releaseView = (TextView) findViewById(R.id.textViewReleaseDate);

        ImageView thumbnail = (ImageView) findViewById(R.id.thumbnail);

        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        // Show Details
        String[] detailInfo = detailIntent.getStringArrayExtra("detailInfo");
        titleView.setText(detailInfo[0]);
        Picasso.with(this).load(detailInfo[1]).into(thumbnail);
        plotView.setText(detailInfo[2]);
        releaseView.setText(detailInfo[4]);
        ratingBar.setRating(Float.parseFloat(detailInfo[3]));

    }
}
