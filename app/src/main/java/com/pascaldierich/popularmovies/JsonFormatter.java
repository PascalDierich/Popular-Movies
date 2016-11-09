package com.pascaldierich.popularmovies;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by pascaldierich on 28.10.16.
 */

public class JsonFormatter {
    public JsonFormatter(){

    }

    public String[] getImageURLS(String jsonString) throws Exception { // TODO: Exceptions
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONArray("results");

        String[] imageURLs = new String[jsonArray.length()];
        final String URL = "https://image.tmdb.org/t/p/w500"; // TODO: declare in string.xml

        for(int i = 0; i < jsonArray.length(); i++){
            jsonObject = jsonArray.getJSONObject(i);
            imageURLs[i] = URL + jsonObject.get("poster_path").toString();
        }

        return imageURLs;
    }
}
