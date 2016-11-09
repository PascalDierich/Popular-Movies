package com.pascaldierich.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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


        DownloadData downloadData = new DownloadData();

        downloadData.delegate = this;
        downloadData.execute();
    }

    @Override
    public void processFinish(String Json) {
        ProgressBar progressBar = (ProgressBar) (findViewById(R.id.progressBar));
        progressBar.setVisibility(View.GONE);
        try {
            ArrayList<GridItem> urlData = parseJsonForImageURLs(Json);
            ImageAdapter imageAdapter = new ImageAdapter(this, R.layout.grid_view_layout, urlData);
            GridView gridView = (GridView) (findViewById(R.id.movie_grid));
            gridView.setAdapter(imageAdapter);
        } catch (Exception e){
            Log.i(TAG, e.fillInStackTrace() + "");
            // TODO: Exceptions...
        }
    }

    private ArrayList<GridItem> parseJsonForImageURLs(String json) throws Exception {
        ArrayList<GridItem> urlData = new ArrayList<>();
        GridItem item;

        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("results");

        final String URL = "https://image.tmdb.org/t/p/w500"; // TODO: declare in string.xml

        JSONObject jsonObject1;
        for(int i = 0; i < jsonArray.length(); i++){
            item = new GridItem();
            jsonObject1 = jsonArray.getJSONObject(i);
            item.setImage(URL + jsonObject1.get("poster_path").toString());
            urlData.add(item);
        }
        return urlData;
    }
}

class DownloadData extends AsyncTask<Void, Void, String>{
    private static final String TAG = DownloadData.class.getSimpleName();

    // TODO: move Strings into strings.xml
    private final String URL_STRING = "https://api.themoviedb.org/3/discover/movie?api_key=";
    private final String API_KEY = "5c359398433009bb5d168d4cfb3e5cf3";
    private final String SORT_BY = "&sort_by=popularity.desc";
    private final String LANGUAGE_EN = "&language=en-US";
    private final String PAGE = "&page=";

    public AsyncResponse delegate;

    private String Json;

    @Override
    protected String doInBackground(Void... voids) {

        HttpURLConnection httpConnection = null;
        BufferedReader reader = null;

        try {

            URL url = new URL(URL_STRING + API_KEY + SORT_BY + LANGUAGE_EN + PAGE +1);

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
            Log.i(TAG, "doInBackground: " + e.getLocalizedMessage());
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