package com.teskola.molkky;

public interface FirebaseListener {

    void onSignInCompleted();
    void onSignInFailed();
    void onResponseReceived(FirebaseManager.Response response, String data);
    void onErrorReceived(FirebaseManager.Error error);


}
