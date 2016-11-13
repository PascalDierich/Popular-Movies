package com.pascaldierich.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AsyncResponse {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Intent intent;

    private boolean checkConnection(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkConnection() == false){
            final TextView error = (TextView) findViewById(R.id.textView_search);
            error.setText(getString(R.string.editText_connection_error));
            error.setVisibility(View.VISIBLE);

            GridView gridView = (GridView) findViewById(R.id.movie_grid);
            gridView.setVisibility(View.INVISIBLE);
        }

        DownloadData downloadData;

        this.intent = getIntent();
        if(intent.getIntExtra(getString(R.string.info_download), 0) == 0){
            downloadData = new DownloadData(getApplicationContext(), 0); // download by popularity
            downloadData.delegate = this;
            downloadData.execute();
        } else if(intent.getIntExtra(getString(R.string.info_download), 0) == 1) {
            downloadData = new DownloadData(getApplicationContext(), 1); // downoload by rating
            downloadData.delegate = this;
            downloadData.execute();
        } else {
            downloadData = new DownloadData(getApplicationContext(), 2); // search for one movie
            downloadData.delegate = this;
            downloadData.execute(intent.getStringExtra(getString(R.string.movieName)));
        }
    }

    @Override
    public void processFinish(final String Json) {
        ProgressBar progressBar = (ProgressBar) (findViewById(R.id.progressBar));
        progressBar.setVisibility(View.GONE);

        if(this.intent.getIntExtra(getString(R.string.info_download), 0) == 2){
            try {
                startActivity(new Intent(MainActivity.this, DetailActivity.class)
                        .putExtra(
                                getString(R.string.detailInfo),
                                parseJsonForDetailInfo(Json, 0)
                        )
                );
            } catch (Exception e){
                final TextView error = (TextView) findViewById(R.id.textView_search);
                error.setText(getString(R.string.editText_error));
                error.setVisibility(View.VISIBLE);

                GridView gridView = (GridView) findViewById(R.id.movie_grid);
                gridView.setVisibility(View.INVISIBLE);

                return;
            }

        }

        try {
            ArrayList<GridItem> urlData = parseJsonForImageURLs(Json);
            ImageAdapter imageAdapter = new ImageAdapter(this, R.layout.grid_view_layout, urlData);
            GridView gridView = (GridView) (findViewById(R.id.movie_grid));
            gridView.setAdapter(imageAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                    try {
                        intent.putExtra(getString(R.string.detailInfo), parseJsonForDetailInfo(Json, position));
                        startActivity(intent); // New Activity starts...
                    } catch (Exception e){
                        Log.v(TAG, "GridView -> OnClickListener -> " + e.fillInStackTrace());
                    }
                }
            });

            ImageButton popularityButton = (ImageButton) findViewById(R.id.toolbar_button_sort_popularity);
            ImageButton ratingButton = (ImageButton) findViewById(R.id.toolbar_button_sort_rating);

            popularityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.sort_by_pop_toast), Toast.LENGTH_SHORT);
                    toast.show();

                    startActivity(new Intent(MainActivity.this, MainActivity.class)
                            .putExtra(getString(R.string.info_download), 0)); // 0 --> download by popularity
                    finish(); // TODO: finish "old" Activity,
                }
            });

            ratingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.sort_by_rat_toast), Toast.LENGTH_SHORT);
                    toast.show();

                    startActivity(new Intent(MainActivity.this, MainActivity.class)
                            .putExtra(getString(R.string.info_download), 1)); // 1 --> download by rating
                    finish(); // TODO: for keeping the navigation clean
                }
            });

            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.searchActionButton);
            final EditText searchEditText = (EditText) findViewById(R.id.editText_search);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast toast = Toast.makeText(getApplicationContext(), "search...", Toast.LENGTH_SHORT);
                    toast.show();

                    searchEditText.setVisibility(View.VISIBLE);

                    GridView gridView = (GridView) findViewById(R.id.movie_grid);
                    gridView.setVisibility(View.INVISIBLE);

                    fab.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            String movie = String.valueOf(searchEditText.getText());
                            movie = movie.replaceAll("\\s", "%20");

                            startActivity(new Intent(MainActivity.this, MainActivity.class)
                                    .putExtra(getString(R.string.info_download), 2)
                                    .putExtra(getString(R.string.movieName), movie)
                            );
                            finish(); // TODO:
                        }
                    });
                }
            });
        } catch (Exception e1){
            Log.v(TAG, "Exception in processFinish() -> " + e1.fillInStackTrace());
            final TextView error = (TextView) findViewById(R.id.textView_search);
            error.setText(getString(R.string.editText_error));
            error.setVisibility(View.VISIBLE);

            GridView gridView = (GridView) findViewById(R.id.movie_grid);
            gridView.setVisibility(View.INVISIBLE);
        }



    }

    private ArrayList<GridItem> parseJsonForImageURLs(String json) throws Exception {
        ArrayList<GridItem> urlData = new ArrayList<>();
        GridItem item;

        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("results");

        JSONObject jsonObject1;
        for(int i = 0; i < jsonArray.length(); i++){
            item = new GridItem();
            jsonObject1 = jsonArray.getJSONObject(i);
            item.setImage(getString(R.string.poster_path) + jsonObject1.get("poster_path").toString());
            urlData.add(item);
        }
        return urlData;
    }

    private String[] parseJsonForDetailInfo(String json, int position) throws Exception {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("results");

        jsonObject = jsonArray.getJSONObject(position);

        return new String[] {
                jsonObject.getString("original_title"),
                getString(R.string.poster_path) + jsonObject.getString("poster_path"),
                jsonObject.getString("overview"),
                jsonObject.getString("vote_average"),
                jsonObject.getString("release_date")
        };
    }
}

class DownloadData extends AsyncTask<String, Void, String>{
    private static final String TAG = DownloadData.class.getSimpleName();

    public AsyncResponse delegate;
    private Context context;
    private int pop_rat;

    private String Json;

    public DownloadData(Context c, int pop_rat){
        super();
        this.context = c;
        this.pop_rat = pop_rat;
    }

    @Override
    protected String doInBackground(String... urlString) {
        HttpURLConnection httpConnection = null;
        BufferedReader reader = null;

        Json = null;
        try {
            URL url = new URL(context.getString(R.string.download_popular)
                    + context.getString(R.string.api_key)
                    + context.getString(R.string.language_en)
                    + context.getString(R.string.page)
                    + 1);

            if (pop_rat == 1){
                url = new URL(context.getString(R.string.download_rating)
                        + context.getString(R.string.api_key)
                        + context.getString(R.string.language_en)
                        + context.getString(R.string.page)
                        + 1);

            } else if (pop_rat == 2){
                url = new URL(context.getString(R.string.search)
                        + context.getString(R.string.api_key)
                        + context.getString(R.string.search_for)
                        + urlString[0] );
            }

            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            InputStream inputStream = httpConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            Json = buffer.toString();


        } catch (Exception e){
            return null;
        } finally {
            if(httpConnection != null){
                httpConnection.disconnect();
            }
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.v(TAG, "Exception in BufferedReader.close() -> " + e.fillInStackTrace());
                }
            }

        }
        return Json;
    }

    @Override
    protected void onPostExecute(String json){
        delegate.processFinish(json);
    }
}
