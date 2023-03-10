package com.teskola.molkky;

import android.annotation.SuppressLint;
import android.content.Context;


import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class FirebaseManager implements FirebaseAuth.IdTokenListener {
    @SuppressLint("StaticFieldLeak")
    private static FirebaseManager instance;
    private final Context context;
    private FirebaseUser user;
    private String token;
    private final ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private final ArrayList<ResponseListener> responseListeners = new ArrayList<>();
    private final RequestQueue mRequestQueue;

    private static final String FB_URL = "https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/";
    public static final int ID_LENGTH = 6; // only even numbers allowed;

    public static FirebaseManager getInstance(Context context) {
        if (instance == null)
            instance = new FirebaseManager(context.getApplicationContext());
        return instance;
    }

    private FirebaseManager(Context context) {
        this.context = context;
        mRequestQueue = Volley.newRequestQueue(context);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            FirebaseAuth.getInstance().signInAnonymously();
            FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        for (UserStatusListener userStatusListener : userStatusListeners) {
                            userStatusListener.onSuccessfulSignIn(firebaseAuth);
                        }
                        FirebaseAuth.getInstance().removeAuthStateListener(this);
                        user.getIdToken(false).addOnSuccessListener(getTokenResult -> token = getTokenResult.getToken());
                    }
                }
            });
        }
    }

    public interface UserStatusListener  {
        void onSuccessfulSignIn(FirebaseAuth firebaseAuth);
        void onFailedSignIn(Exception e);
    }

    public interface ResponseListener {
        void onResponseReceived(JSONObject jsonObject);
        void onErrorReceived(Exception e);
    }

    public FirebaseUser getUser() {
        if (user == null) {
            signIn();
        }
        return user;
    }

    public void addUserStatusListener (UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener (UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addResponseListener (ResponseListener responseListener) {
        responseListeners.add(responseListener);
    }

    public void removeResponseListener (ResponseListener responseListener) {
        responseListeners.remove(responseListener);
    }

    public void signIn() {
        FirebaseAuth.getInstance().signInAnonymously().addOnFailureListener(e -> {
            for (UserStatusListener userStatusListener : userStatusListeners)
                userStatusListener.onFailedSignIn(e);
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
     * Initializes database. If successful, add creator to database users.
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
                    addUser(database);
                },
                error -> {
                    for (ResponseListener listener : responseListeners) {
                        listener.onErrorReceived(error);
                    }
                });
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
                    for (ResponseListener responseListener : responseListeners) {
                        responseListener.onResponseReceived(response);
                    }
                },
                error -> {
                    for (ResponseListener responseListener : responseListeners) {
                        responseListener.onErrorReceived(error);
                    }
                });
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
                    for (ResponseListener listener : responseListeners) {
                        listener.onResponseReceived(response);
                    }
                },
                error -> {
                    for (ResponseListener listener : responseListeners) {
                        listener.onErrorReceived(error);
                    }
                });
        mRequestQueue.add(request);
    }

    /*
     *
     * Adds game to Firebase. If fails, gets a new token and tries again.
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
                    for (ResponseListener listener : responseListeners) {
                        listener.onResponseReceived(response);
                    }
                },
                error -> {
                    for (ResponseListener listener : responseListeners) {
                        listener.onErrorReceived(error);
                    }
                });
        mRequestQueue.add(request);
    }

    /*
     *
     * Checks if database exist. Response includes creation timestamp, "null" if database doesn't exist.
     *
     * */

    public void testDatabase(String database, boolean initialize) {
        if (user == null) {
            signIn();
            return;
        }
        String url = FB_URL + "databases/" + database + "/created.json?auth=" + token;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (!initialize) {
                        JSONObject res = new JSONObject();
                        try {
                            res.put("created", response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        for (ResponseListener listener : responseListeners) {
                            listener.onResponseReceived(res);
                        }
                    } else if (response.equals("null")) {
                        initializeDatabase();
                    }
                },
                error -> {
                    for (ResponseListener listener : responseListeners) {
                        listener.onErrorReceived(error);
                    }
                });
        mRequestQueue.add(request);
    }



    /*
     *
     * Fetches games from firebase and adds them to local database.
     *
     * */

    public void firebaseToLocal(String database) {
        if (user == null) {
            signIn();
            return;
        }
        String url = FB_URL + "databases/" + database + "/games/.json?auth=" + token;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Game[] games = new Gson().fromJson(response, Game[].class);
                    for (Game game : games) {
                        LocalDatabaseManager.getInstance(context).saveGameToDatabase(game);
                    }

                },
                error -> {

                });
        mRequestQueue.add(request);
    }

    @Override
    public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {
        user.getIdToken(false).addOnSuccessListener(getTokenResult -> {
            token = getTokenResult.getToken();
        });

    }
}

