package com.teskola.molkky;



import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;


public class FirebaseManager {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseRef = database.getReference("databases");
    private final DatabaseReference usersRef = database.getReference("users");


    public FirebaseManager() {
    }



    public static HashMap<String, String> addTimestamp() {
        HashMap<String, String> timestamp = new HashMap<>();
        timestamp.put(".sv", "timestamp");
        return timestamp;
    }

    public FirebaseManager searchDatabase (String database, OnSuccessListener<String> response, OnFailureListener error) {
        databaseRef.child(database).child("created").get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(String.valueOf(task.getResult().getValue()));
            else error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public FirebaseManager initializeDatabase(String database, OnSuccessListener<Void> response, OnFailureListener error) {
        databaseRef.child(database).child("created").setValue(addTimestamp()).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(null);
            else error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public FirebaseManager addUser(String database, String uid, OnSuccessListener<Void> response, OnFailureListener error) {
        databaseRef.child(database).child("users").child(uid).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(null);
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public FirebaseManager removeUser(String database, String uid, OnSuccessListener<Void> response, OnFailureListener error) {
        databaseRef.child(database).child("users").child(uid).setValue(null).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(null);
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public FirebaseManager addGameToDatabase(Game game, String user, OnSuccessListener<String> response, OnFailureListener error) {
        String gameId;
        if (game.getId() == null)
            gameId = usersRef.child(user).child("games").push().getKey();
        else
            gameId = game.getId();
        Date date = new Date();
        game.setTimestamp(date.getTime());
        usersRef.child(user).child("/games/" + gameId).setValue(game).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(gameId);
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public FirebaseManager addPlayerToDatabase (PlayerInfo player, String user, OnSuccessListener<String> response, OnFailureListener error) {
        String playerId;
        if (player.getId() == null)
            playerId = usersRef.child(user).child("players").push().getKey();
        else
            playerId = player.getId();
        usersRef.child(user).child("/players/" + playerId).setValue(player).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(playerId);
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
     return this;
    }

}

