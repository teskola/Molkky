package com.teskola.molkky;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class FBHandler {
    private RequestQueue mRequestqueue;
    private Context context;
    private FirebaseUser user;
    private String token;
    private onResponseListener mListener;

    private static final String FB_URL = "https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/";

    public FBHandler(Context context) {
        this.context = context;
        mRequestqueue = Volley.newRequestQueue(context);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user = auth.getCurrentUser();
                    getToken();
                } else {
                    Toast.makeText(context, R.string.unable_to_connect, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            getToken();
        }
    }

    public interface onResponseListener {
        void onResponseReceived (String response);
    }

    public void setOnResponseListener(onResponseListener listener) {
        mListener = listener;
    }


    /*
    *
    * Returns shorter, 6 characters, version of userId
    *
    * */

    public String getShortId () {
        String userId = user.getUid();
        return userId.substring(0,3) + userId.substring(userId.length() -3);
    }

    /*
    *
    * Get games from database. Returns json, null if database doesn't exist.
    *
    * */

    public void getGamesJson (String id) {
        String url = FB_URL + "databases/" + id + "/games/.json?auth=" + token;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    mListener.onResponseReceived(response);
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        getToken();
                        getGamesJson(id);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.database_read_error), Toast.LENGTH_LONG).show();
                    }
                });
        mRequestqueue.add(request);
    }

    /*
    *
    * Converts game object to json. Returns json.
    *
    * */


    private JSONObject gameToJson(Game game) {
        String json = new Gson().toJson(game);
        JSONObject jsonObject = null;
        JSONObject timestamp;
        try {
            jsonObject = new JSONObject(json);
            timestamp = new JSONObject();
            timestamp.put(".sv", "timestamp");
            jsonObject.remove("id");
            jsonObject.put("timestamp", timestamp);
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

    /*
    *
    * Updates authentication token.
    *
    * */

    private void getToken() {
        user.getIdToken(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
               token = task.getResult().getToken();
            }
        });
    }

    /*
     *
     * Adds game to Firebase. If fails, gets new token and tries again.
     *
     * */

    public void addGameToFireBase(String database, Game game) {
        JSONObject jsonObject = gameToJson(game);
        Log.d("json", jsonObject.toString());
        String url = FB_URL + "databases/" + database + "/games/" + game.getId() + ".json?auth=" + token;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                response -> {
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        getToken();
                        addGameToFireBase(database, game);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.post_to_database_failed), Toast.LENGTH_LONG).show();
                    }
                });
        mRequestqueue.add(request);
    }

    /*
    *
    * Search database
    *
    * */
}

