package com.teskola.molkky;

import com.google.firebase.auth.FirebaseAuth;


public interface FirebaseListener {

    void onSignInCompleted();
    void onSignInFailed();
    void onResponseReceived(FirebaseManager.Response response, String data);
    void onErrorReceived(FirebaseManager.Error error);


}
