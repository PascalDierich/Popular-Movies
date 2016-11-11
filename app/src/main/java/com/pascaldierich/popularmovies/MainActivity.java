package com.pascaldierich.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

    /*
     * TODO: ConnectionCheck in onCreate()
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadData downloadData;

        Intent intent = getIntent();
        if(intent.getBooleanExtra(getString(R.string.sort_by_boolean), true) == true ){
            downloadData = new DownloadData(getApplicationContext(), true);
        } else {
            downloadData = new DownloadData(getApplicationContext(), false);
        }



        downloadData.delegate = this;
        downloadData.execute();

    }

    @Override
    public void processFinish(final String Json) {
        ProgressBar progressBar = (ProgressBar) (findViewById(R.id.progressBar));
        progressBar.setVisibility(View.GONE);
        try {
            ArrayList<GridItem> urlData = parseJsonForImageURLs(Json);
            ImageAdapter imageAdapter = new ImageAdapter(this, R.layout.grid_view_layout, urlData);
            GridView gridView = (GridView) (findViewById(R.id.movie_grid));
            gridView.setAdapter(imageAdapter);

            /*
            ClickListener
             */
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                    try {
                        intent.putExtra("detailInfo", parseJsonForDetailInfo(Json, position));
                        startActivity(intent); // New Activity starts...
                    } catch (Exception e){
                        Log.i(TAG, "processFinish -> onClickListener: " + e.fillInStackTrace());
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
                            .putExtra(getString(R.string.sort_by_boolean), true));
                }
            });

            ratingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.sort_by_rat_toast), Toast.LENGTH_SHORT);
                    toast.show();

                    startActivity(new Intent(MainActivity.this, MainActivity.class)
                            .putExtra(getString(R.string.sort_by_boolean), false));
                }
            });


        } catch (Exception e){
            Log.e(TAG, "Exception in processFinish() = " + e.fillInStackTrace());
            // TODO: Exceptions...
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

    private String[] parseJsonForDetailInfoSpecificMovie(String json) throws Exception {
        // TODO


        return null;
    }
}

class DownloadData extends AsyncTask<Void, Void, String>{
    private static final String TAG = DownloadData.class.getSimpleName();

    public AsyncResponse delegate;
    private Context context;
    private boolean pop_rat;

    private String Json;

    public DownloadData(Context c, boolean pop_rat){
        super();
        this.context = c;
        this.pop_rat = pop_rat;
    }

    @Override
    protected String doInBackground(Void... voids) {

        HttpURLConnection httpConnection = null;
        BufferedReader reader = null;

        Json = null;
        try {
            URL url = new URL(context.getString(R.string.url_string)
                    + context.getString(R.string.api_key)
                    + context.getString(R.string.sort_by_pop)
                    + context.getString(R.string.language_en)
                    + context.getString(R.string.page)
                    + 1);

            if(pop_rat == false){
                Log.e(TAG, "pop_rat == false");
                url = new URL("" + context.getString(R.string.url_string)
                        + context.getString(R.string.api_key)
                        + context.getString(R.string.sort_by_rat)
                        + context.getString(R.string.language_en)
                        + context.getString(R.string.page)
                        + 1);
            }

            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            InputStream inputStream = httpConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            Json = buffer.toString();


        } catch (Exception e){ // TODO: Exception sauber abfangen
            Log.i(TAG, "doInBackground: " + e.fillInStackTrace());
        } finally {
            if(httpConnection != null){
                httpConnection.disconnect();
            }
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.v("Exception: ", "Exception in finally");
                    e.printStackTrace();
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

class SearchForMovie extends AsyncTask<String, Void, Void>{

    @Override
    protected Void doInBackground(String... url) {
        // TODO: search for one specific movie and return json
        // gets called by FloatingActionButton

        return null;
    }
}