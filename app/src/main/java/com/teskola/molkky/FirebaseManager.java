package com.teskola.molkky;

import android.content.Context;


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


public class FirebaseManager {
    private final RequestQueue mRequestqueue;
    private FirebaseUser user;
    private onResponseListener mListener;
    private static FirebaseManager instance;
    private String token;
    private Context context;

    private static final String FB_URL = "https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/";
    public static final int ID_LENGTH = 6; // only even numbers allowed;

    public static FirebaseManager getInstance(Context context) {
        if (instance == null)
            instance = new FirebaseManager(context.getApplicationContext());
        return instance;
    }

    private FirebaseManager(Context context) {
        mRequestqueue = Volley.newRequestQueue(context);
        user = FirebaseAuth.getInstance().getCurrentUser();
        this.context = context;
    }

    public FirebaseUser getUser() {
        return user;
    }

    public interface onResponseListener {
        void onResponseReceived(JSONObject response);

        void onErrorReceived(VolleyError error);

        void onSignIn();

        void onTokenRefreshed();

        void onSignInFailed();
    }

    public void setOnResponseListener(onResponseListener listener) {
        mListener = listener;
    }




    /*
     *
     * Returns shorter, 6 character, lowercase, version of userId. If user is not authenticated, returns "".
     *
     * */

    public String getShortId() {
        if (user != null) {
            String userId = user.getUid();
            return ((userId.substring(0, ID_LENGTH / 2) + userId.substring(userId.length() - (ID_LENGTH / 2))).toLowerCase());
        } else return "";
    }

    /*
    *
    * Signs user in.
    *
    * */

    public void signIn() {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                mListener.onSignIn();

            } else {
                mListener.onSignInFailed();
            }
        });
    }

    /*
     *
     *  Updates authentication token.
     *
     * */

    public void refreshToken() {
        if (user != null)
            user.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                token = getTokenResult.getToken();
                mListener.onTokenRefreshed();
            });
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
                    mListener.onErrorReceived(error);
                });
        mRequestqueue.add(request);
    }

    /*
     *
     * Add user to database.
     *
     * */

    public void addUser(String database) {
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
                    mListener.onResponseReceived(response);
                },
                error -> {
                    mListener.onErrorReceived(error);
                });
        mRequestqueue.add(request);
    }

    /*
    *
    **** Disconnect from database.
    *
    * */

    public void disconnect(String database) {
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
                    mListener.onResponseReceived(response);
                },
                error -> {
                    mListener.onErrorReceived(error);
                });
        mRequestqueue.add(request);
    }

    /*
     *
     * Adds game to Firebase. If fails, gets a new token and tries again.
     *
     * */

    public void addGameToFireBase(String database, Game game) {
        JSONObject jsonObject = gameToJson(game);
        String url = FB_URL + "databases/" + database + "/games/" + game.getId() + ".json?auth=" + token;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                response -> {
                    mListener.onResponseReceived(response);
                },
                error -> {
                    mListener.onErrorReceived(error);
                });
        mRequestqueue.add(request);
    }

    /*
     *
     * Checks if database exist. Response includes creation timestamp, "null" if database doesn't exist.
     *
     * */

    public void testDatabase(String database, boolean initialize) {
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
                        mListener.onResponseReceived(res);
                    } else if (response.equals("null")) {
                        initializeDatabase();
                    }
                },
                error -> {
                    mListener.onErrorReceived(error);
                });
        mRequestqueue.add(request);
    }



    /*
     *
     * Fetches games from firebase and adds them to local database.
     *
     * */

    public void firebaseToLocal(String database) {
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
        mRequestqueue.add(request);
    }

}

