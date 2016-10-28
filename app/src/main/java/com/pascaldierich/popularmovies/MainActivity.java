package com.pascaldierich.popularmovies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements AsyncResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView movieGrid = (GridView) (findViewById(R.id.movie_grid)); // initialize GridView

        DownloadData downloadData = new DownloadData();

        downloadData.delegate = this;
        downloadData.execute();


/*
        movieGrid.setAdapter(new ImageAdapter(this));

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(MainActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
*/
    }

    @Override
    public void processFinish(String Json) {
        Log.v("MainAcitivty", "processFinish");
        ProgressBar progressBar = (ProgressBar) (findViewById(R.id.progressBar));
        progressBar.setVisibility(View.INVISIBLE); // download is finished
        // TODO: start ImageAdapter

    }
}

class DownloadData extends AsyncTask<Void, Void, String>{

    // TODO: move Strings into strings.xml
    private final String URL_STRING = "https://api.themoviedb.org/3/movie/550?api_key=";
    private final String API_KEY = "5c359398433009bb5d168d4cfb3e5cf3";
    public AsyncResponse delegate;

    private String Json;

    @Override
    protected String doInBackground(Void... voids) {

        HttpURLConnection httpConnection = null;
        BufferedReader reader = null;

        try {

            URL url = new URL(URL_STRING + API_KEY);

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
            Log.i("Exception", "Exception in AsyncTask" + e.fillInStackTrace());
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