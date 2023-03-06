package com.teskola.molkky;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FBHandler {
    private RequestQueue mRequestqueue;
    private static FBHandler instance;
    private Context context;

    private static final String FB_URL = "https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/games/";

    private FBHandler(Context context) {
        this.context = context;
        mRequestqueue = Volley.newRequestQueue(context);
    };
    public static FBHandler getInstance(Context context) {
        if (instance == null) instance = new FBHandler(context);
        return instance;
    }



    private JSONObject gameToJson (Game game) {
        String json = new Gson().toJson(game);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            jsonObject.remove("id");
            JSONArray players = jsonObject.getJSONArray("players");
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                player.remove("undoStack");
            }
            Log.d("JSON_OBJECT", jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void addTimestamp (String gameID) {
        JSONObject timestamp = new JSONObject();
        try {
            timestamp.put(".sv", "timestamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, FB_URL + gameID + "/timestamp.json",
                timestamp, response -> {}, error -> {});
        mRequestqueue.add(request);

    }

    public void addGameToFireBase(Game game) {
        JSONObject jsonObject = gameToJson(game);
        String url = FB_URL + game.getId() + ".json";
        if (jsonObject != null) {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    response -> addTimestamp(game.getId()),
                    error -> Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show());
            mRequestqueue.add(request);
        }


    }




}
