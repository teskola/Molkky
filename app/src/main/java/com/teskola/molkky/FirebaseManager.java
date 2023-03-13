package com.teskola.molkky;

import android.content.Context;


import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;


public class FirebaseManager {

    private String token;
    private OnSuccessListener onSuccessListener;
    private OnFailureListener onFailureListener;
    private final RequestQueue mRequestQueue;

    public static final int NETWORK_ERROR = 600;
    private static final String FB_URL = "https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/";

    public FirebaseManager(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public interface OnSuccessListener {
        void onSuccess(String response);
    }

    public interface OnFailureListener {
        void onFailure(int errorCode);
    }

    public FirebaseManager addOnSuccessListener (OnSuccessListener listener) {
        onSuccessListener = listener;
        return this;
    }

    public FirebaseManager addOnFailureListener (OnFailureListener listener) {
        onFailureListener = listener;
        return this;
    }

    public void close() {
        mRequestQueue.stop();
    }

    public void setToken(String token) {
        this.token = token;
    }

    /*
     *
     * Initializes database for new account. If successful, add creator to database users.
     *
     * */

    public FirebaseManager initializeDatabase(String database) {

        String url = FB_URL + "databases/" + database + ".json?auth=" + token;
        JSONObject jsonObject = new JSONObject();
        JSONObject timestamp = new JSONObject();
        try {
            timestamp.put(".sv", "timestamp");
            jsonObject.put("created", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                response -> {
                    if (onSuccessListener != null)
                        onSuccessListener.onSuccess(response.toString());
                }, this::errorResponse);
        mRequestQueue.add(request);
        return this;
    }

    /*
     *
     * Add user to database.
     *
     * */

    public void addUser(String database, String uid) {
        String url = FB_URL + "databases/" + database + "/users/" + uid + ".json?auth=" + token;
        JSONObject jsonObject = new JSONObject();
        JSONObject timestamp = new JSONObject();
        try {
            timestamp.put(".sv", "timestamp");
            jsonObject.put("joined", timestamp);
            jsonObject.put("lastConnect", timestamp);
            jsonObject.put("connected", true);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                response -> {
                    if (onSuccessListener != null)
                        onSuccessListener.onSuccess(response.toString());
                }, this::errorResponse
        );
        mRequestQueue.add(request);
    }

    /*
     *
     **** Disconnect from database.
     *
     * */

    public void disconnect(String database, String user) {
        String url = FB_URL + "databases/" + database + "/users/" + user + ".json?auth=" + token;
        JSONObject jsonObject = new JSONObject();
        JSONObject timestamp = new JSONObject();
        try {
            timestamp.put(".sv", "timestamp");
            jsonObject.put("lastConnect", timestamp);
            jsonObject.put("connected", false);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url, jsonObject,
                response -> {
                    if (onSuccessListener != null)
                        onSuccessListener.onSuccess(response.toString());
                }, this::errorResponse);
        mRequestQueue.add(request);
    }

    /*
     *
     * Adds game to Firebase.
     *
     * */

    public void addGameToFireBase(String database, Game game, String user) {
        JSONObject jsonObject = gameToJson(game, user);
        String url = FB_URL + "databases/" + database + "/games/" + game.getId() + ".json?auth=" + token;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                response -> {
                    if (onSuccessListener != null)
                        onSuccessListener.onSuccess(response.toString());
                }, this::errorResponse);
        mRequestQueue.add(request);
    }

    /*
     *
     * Fetches database.
     *
     * */

    public FirebaseManager fetchDatabase(String database) {

        String url = FB_URL + "databases/" + database + ".json?auth=" + token;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (onSuccessListener != null)
                        onSuccessListener.onSuccess(response);
                }, this::errorResponse);
        mRequestQueue.add(request);
        return this;
    }

    private void errorResponse(VolleyError error)
    {
        if (onFailureListener != null) {
            if (error.networkResponse == null)
                onFailureListener.onFailure(NETWORK_ERROR);
            else
                onFailureListener.onFailure(error.networkResponse.statusCode);
        }
    }


    /*
     *
     * Converts game object to json. Returns json.
     *
     * */


    private JSONObject gameToJson(Game game, String user) {
        String json = new Gson().toJson(game);
        JSONObject jsonObject = null;
        JSONObject timestamp, addedBy;
        try {
            jsonObject = new JSONObject(json);
            jsonObject.remove("id");

            timestamp = new JSONObject();
            timestamp.put(".sv", "timestamp");
            jsonObject.put("timestamp", timestamp);

            addedBy = new JSONObject();
            addedBy.put("addedBy", user);

            JSONArray players = jsonObject.getJSONArray("players");
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                player.remove("undoStack");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}

