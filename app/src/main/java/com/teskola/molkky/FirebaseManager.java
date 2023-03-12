package com.teskola.molkky;

import android.content.Context;


import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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


public class FirebaseManager implements FirebaseAuth.IdTokenListener {

    private static FirebaseManager instance;
    private FirebaseUser user;
    private String token;
    private final ArrayList<FirebaseListener> listeners = new ArrayList<>();
    private final RequestQueue mRequestQueue;

    private static final String FB_URL = "https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/";
    public static final int ID_LENGTH = 6; // only even numbers allowed;

    public static FirebaseManager getInstance(Context context) {
        if (instance == null)
            instance = new FirebaseManager(context.getApplicationContext());
        return instance;
    }

    private FirebaseManager(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            signIn();
            FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        FirebaseAuth.getInstance().removeAuthStateListener(this);
                        user.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                            token = getTokenResult.getToken();
                            initializeDatabase();
                        });
                    }
                }
            });
        } else {
            user.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                token = getTokenResult.getToken();
            });
        }
    }

    public enum Response {
        DATABASE_FOUND,
        DATABASE_NOT_FOUND,
        DATABASE_CHANGED,
        DATABASE_CREATED,
        DATABASE_CONNECTED,
        DATABASE_DISCONNECTED,
        GAME_ADDED
    }

    public enum Error {
        NETWORK_ERROR,
        UNKNOWN_ERROR,
        ADD_GAME_FAILED
    }


    public void addListener(FirebaseListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FirebaseListener listener) {
        listeners.remove(listener);
    }

    public void signIn() {
        FirebaseAuth.getInstance().signInAnonymously().addOnFailureListener(e -> {
            for (FirebaseListener listener : listeners) listener.onSignInFailed();
        });
    }

    /*
     *
     * Returns shorter, 6 character, lowercase, version of userId. If user is not authenticated, returns "".
     *
     * */

    public String getShortId() {
        if (user == null) {
            signIn();
            return "";
        }
        String userId = user.getUid();
        return ((userId.substring(0, ID_LENGTH / 2) + userId.substring(userId.length() - (ID_LENGTH / 2))).toLowerCase());
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
            jsonObject.remove("id");

            timestamp = new JSONObject();
            timestamp.put(".sv", "timestamp");
            jsonObject.put("timestamp", timestamp);

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

    /*
     *
     * Initializes database for new account. If successful, add creator to database users.
     *
     * */

    public void initializeDatabase() {
        if (user == null) {
            signIn();
            return;
        }
        String database = getShortId();
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
                    for (FirebaseListener listener : listeners)
                        listener.onResponseReceived(Response.DATABASE_CONNECTED, null);
                    addUser(database);
                    for (FirebaseListener listener : listeners)
                        listener.onSignInCompleted();
                },
                this::errorResponse);
        mRequestQueue.add(request);
    }

    /*
     *
     * Add user to database.
     *
     * */

    public void addUser(String database) {
        if (user == null) {
            signIn();
            return;
        }
        String url = FB_URL + "databases/" + database + "/users/" + user.getUid() + ".json?auth=" + token;
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
                    for (FirebaseListener listener : listeners)
                        listener.onResponseReceived(Response.DATABASE_CREATED, null);
                }, this::errorResponse
        );
        mRequestQueue.add(request);
    }

    /*
     *
     **** Disconnect from database.
     *
     * */

    public void disconnect(String database) {
        if (user == null) {
            signIn();
            return;
        }
        String url = FB_URL + "databases/" + database + "/users/" + user.getUid() + ".json?auth=" + token;
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
                    for (FirebaseListener listener : listeners)
                        listener.onResponseReceived(Response.DATABASE_DISCONNECTED, null);
                }, this::errorResponse

        );
        mRequestQueue.add(request);
    }

    /*
     *
     * Adds game to Firebase.
     *
     * */

    public void addGameToFireBase(String database, Game game) {
        if (user == null) {
            signIn();
            return;
        }
        JSONObject jsonObject = gameToJson(game);
        String url = FB_URL + "databases/" + database + "/games/" + game.getId() + ".json?auth=" + token;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                response -> {

                    for (FirebaseListener listener : listeners)
                        listener.onResponseReceived(Response.GAME_ADDED, null);
                }, error -> {
            if (error.networkResponse == null) {
                for (FirebaseListener listener : listeners)
                    listener.onErrorReceived(Error.NETWORK_ERROR);
            } else
                for (FirebaseListener listener : listeners)
                    listener.onErrorReceived(Error.ADD_GAME_FAILED);
        }

        );
        mRequestQueue.add(request);
    }

    /*
     *
     * Fetches games from firebase and adds them to local database.
     *
     * */

    public void fetchDatabase(String id) {
        if (user == null) {
            signIn();
            return;
        }
        String url = FB_URL + "databases/" + id + ".json?auth=" + token;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (Objects.equals(response, "null")) {
                        for (FirebaseListener listener : listeners)
                            listener.onResponseReceived(Response.DATABASE_NOT_FOUND, id);
                    }

                }, this::errorResponse);
        mRequestQueue.add(request);
    }

    @Override
    public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {
        user.getIdToken(false).addOnSuccessListener(getTokenResult -> {
            token = getTokenResult.getToken();
        });

    }

    private void errorResponse(VolleyError error) {
        {
            {
                {
                    if (error.networkResponse == null) {
                        for (FirebaseListener listener : listeners)
                            listener.onErrorReceived(Error.NETWORK_ERROR);
                    } else
                        for (FirebaseListener listener : listeners)
                            listener.onErrorReceived(Error.UNKNOWN_ERROR);
                }
            }
        }
    }
}

