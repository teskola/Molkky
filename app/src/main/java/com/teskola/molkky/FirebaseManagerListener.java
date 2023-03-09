package com.teskola.molkky;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface FirebaseManagerListener {

    void onResponseReceived(JSONObject response);

    void onErrorReceived(VolleyError error);

    void onSignInFailed(Exception e);

}


