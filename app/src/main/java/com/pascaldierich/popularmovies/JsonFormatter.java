package com.pascaldierich.popularmovies;

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



        return null;
    }

}
