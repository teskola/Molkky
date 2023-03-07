package com.teskola.molkky;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FBHandler {
    private RequestQueue mRequestqueue;
    private static FBHandler instance;
    private Context context;
    private FirebaseUser user;
    private String token;

    private static final String FB_URL = "https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/";

    private FBHandler(Context context) {
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
                    Toast.makeText(context.getApplicationContext(), "Tapahtui virhe.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            getToken();
        }

    }

    ;

    public static FBHandler getInstance(Context context) {
        if (instance == null) instance = new FBHandler(context);
        return instance;
    }


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

    private void getToken() {
        user.getIdToken(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                token = task.getResult().getToken();
            }
        });
    }

    /*
     *
     * Adds game to Firebase
     *
     * */

    public void addGameToFireBase(Game game) {
        JSONObject jsonObject = gameToJson(game);
        String userId = user.getUid();
        String shortId = userId.substring(0,3) + userId.substring(userId.length() -3);
        Log.d("json", jsonObject.toString());
        String url = FB_URL + "databases/" + shortId + "/games/" + game.getId() + ".json?auth=" + token;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                response -> {
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        mRequestqueue.add(request);
    }
}

