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

        final String URl = "https://image.tmdb.org/t/p/w500"; // + poster/path.jpg

        JSONArray jsonArray = jsonObject.getJSONArray("results");

        JSONObject jsonObject1 = jsonArray.getJSONObject(0);
        String a = jsonObject1.get("poster_path").toString();

        Log.i("JSONArray.getString = ", a);
        return null;
    }

}
