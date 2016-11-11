package com.pascaldierich.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent detailIntent = this.getIntent();

        // Show Details
        String[] detailInfo = detailIntent.getStringArrayExtra("detailInfo");
        setTitle(detailInfo[0]);
        Picasso.with(this).load(detailInfo[1]).into((ImageView) findViewById(R.id.thumbnail));

        TextView titleView = (TextView) findViewById(R.id.textViewTitle);
        TextView plotView = (TextView) findViewById(R.id.textViewPlot);
        TextView releaseView = (TextView) findViewById(R.id.textViewReleaseDate);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        titleView.setText(detailInfo[0]);
        plotView.setText(detailInfo[2]);
        releaseView.setText(detailInfo[4]);
        ratingBar.setRating(Float.parseFloat(detailInfo[3]));

    }
}
