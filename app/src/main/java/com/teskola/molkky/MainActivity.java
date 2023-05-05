package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends OptionsActivity implements ListAdapter.OnItemClickListener, PlayerHandler.OnEmptyDatabaseListener, PlayerHandler.OnPlayersAddedListener {

    private List<PlayerInfo> playersList = new ArrayList<>();
    private ListAdapter listAdapter;
    private RecyclerView recyclerview;
    private EditText editPlayerName;
    private Button startButton;
    private CheckBox randomCheckBox;
    private SharedPreferences saved_state;
    private SharedPreferences.Editor editor;
    private int draggedItemIndex;
    private PlayerHandler playerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        playerHandler = PlayerHandler.getInstance(this);
        playerHandler.setEmptyDatabaseListener(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.startButton);
        recyclerview = findViewById(R.id.recyclerView);
        editPlayerName = findViewById(R.id.editTextPlayerName);
        editPlayerName.setImeActionLabel(getResources().getString(R.string.add), EditorInfo.IME_ACTION_DONE);
        randomCheckBox = findViewById(R.id.randomCheckBox);
        ImageButton addButton = findViewById(R.id.addButton);
        ImageButton selectButton = findViewById(R.id.selectButton);

        saved_state = getSharedPreferences("SAVED_STATE", Context.MODE_PRIVATE);
        if (saved_state.getString("SAVED_SELECTION", null) != null) {
            playersList = new ArrayList<>();
            PlayerInfo[] players = new Gson().fromJson(saved_state.getString("SAVED_SELECTION", null), PlayerInfo[].class);
            Collections.addAll(playersList, players);
            randomCheckBox.setChecked(saved_state.getBoolean("RANDOM", false));
            editor = saved_state.edit();
            editor.remove("SAVED_SELECTION");
            editor.apply();
        }
        if (saved_state.getString("SAVED_STATE", null) != null) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("SAVED_STATE", saved_state.getString("SAVED_STATE", null));
            editor = saved_state.edit();
            editor.remove("SAVED_STATE");
            editor.apply();
            startActivity(intent);
            return;
        }

        if (savedInstanceState != null) {
            playersList = new ArrayList<>();
            PlayerInfo[] players = new Gson().fromJson(savedInstanceState.getString("PLAYERS"), PlayerInfo[].class);
            Collections.addAll(playersList, players);
            randomCheckBox.setChecked(savedInstanceState.getBoolean("RANDOM"));
        }

        // https://www.youtube.com/watch?v=yua1exHtFB4&t=391s

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.Callback() {
                    @Override
                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        draggedItemIndex = viewHolder.getAbsoluteAdapterPosition();
                        int targetIndex = target.getAbsoluteAdapterPosition();
                        Collections.swap(playersList, draggedItemIndex, targetIndex);
                        listAdapter.notifyItemMoved(draggedItemIndex, targetIndex);
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                    }
                }
        );
        itemTouchHelper.attachToRecyclerView(recyclerview);

        // https://stackoverflow.com/questions/1489852/android-handle-enter-in-an-edittext
        editPlayerName.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE && editPlayerName.getText().length() > 0) {
                addPlayer();
                handled = true;
            }
            return handled;
        });

        addButton.setOnClickListener(view -> {

            if (editPlayerName.getText().length() > 0) {
                addPlayer();
            }
        });

        createRecyclerView();
        selectButton.setOnClickListener(view -> selectPlayers());
        startButton.setOnClickListener(view -> startGame());
    }

    @Override
    protected void onNewIntent (Intent intent) {
        super.onNewIntent(intent);
        if (intent.getStringExtra("PLAYERS") != null) {
            playersList = new ArrayList<>();
            String json = intent.getStringExtra("PLAYERS");
            PlayerInfo[] players = new Gson().fromJson(json, PlayerInfo[].class);
            Collections.addAll(playersList, players);
        }
        if (intent.getBooleanExtra("NEW_GAME", false))
            randomCheckBox.setChecked(false);
        createRecyclerView();
    }

    @Override
    protected void onResume () {
        super.onResume();
        startButton.setEnabled(playersList.size() > 1);
        // playerHandler.start();
        if (playerHandler.noSavedPlayers()) {
            noGamesInDatabase = true;
            playerHandler.registerOnPlayersAddedListener(this);
        }
    }

    @Override
    protected void onPause () {
        super.onPause();
        // playerHandler.stop();
    }

    public void createRecyclerView () {
        listAdapter = new ListAdapter(this, playersList, getPreferences().getBoolean("SHOW_IMAGES", false), this);
        recyclerview.setAdapter(listAdapter);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

    }

    private void startGame() {

        Intent intent = new Intent(this, GameActivity.class);
        String json = new Gson().toJson(playersList);
        intent.putExtra("PLAYERS", json);
        intent.putExtra("RANDOM", randomCheckBox.isChecked());
        startActivity(intent);
    }

    private void selectPlayers() {
        if (playerHandler.noSavedPlayers()) {
            Toast.makeText(this, getString(R.string.no_saved_players), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent (this, SelectPlayersActivity.class);
        if (!playersList.isEmpty()) {
            String addedJson = new Gson().toJson(playersList);
            intent.putExtra("SELECTED_PLAYERS", addedJson);
        }

        List<PlayerInfo> otherPlayers = playerHandler.getPlayers(playersList);
        String allJson = new Gson().toJson(otherPlayers);
        intent.putExtra("OTHER_PLAYERS", allJson);
        startActivity(intent);

    }


    // https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null && ev.getAction() == MotionEvent.ACTION_UP) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void addPlayer () {

        PlayerInfo newPlayer = new PlayerInfo(editPlayerName.getText().toString());

        // Check if name is already added

        for (PlayerInfo player : playersList) {
            if (player.getName().equals(newPlayer.getName())) {
                Toast.makeText(this, getString(R.string.already_added, newPlayer.getName()), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Check if name is already in database. If not, give player unique id.

        if(playerHandler.addPlayer(newPlayer))
            Toast.makeText(this, getString(R.string.fetched_from_database, newPlayer.getName()), Toast.LENGTH_SHORT).show();

        playersList.add(0, newPlayer);
        if (playersList.size() > 1) {
            startButton.setEnabled(true);
        }

        editPlayerName.setText(null);
        listAdapter.notifyItemInserted(0);
        Objects.requireNonNull(recyclerview.getLayoutManager()).scrollToPosition(0);
    }

    public void deletePlayer (int position) {
        playersList.remove(position);
        if (playersList.size() < 2) {
            startButton.setEnabled(false);
        }
        listAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String json = new Gson().toJson(playersList);
        savedInstanceState.putString("PLAYERS", json);
        savedInstanceState.putBoolean("RANDOM", randomCheckBox.isChecked());
    }


    @Override
    public void onSelectClicked(int position) {
        Toast.makeText(this, R.string.move_player_instructions, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDeleteClicked(int position) {
        deletePlayer(position);
    }

    @Override
    public void onBackPressed () {
        if (!playersList.isEmpty()) {
            String json = new Gson().toJson(playersList);
            editor = saved_state.edit();
            editor.putString("SAVED_SELECTION", json);
            editor.putBoolean("RANDOM", randomCheckBox.isChecked());
            editor.apply();
        }
        super.onBackPressed();
        finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("SHOW_IMAGES")) {
            createRecyclerView();
        }
    }

    @Override
    public void onEmpty() {
        noGamesInDatabase = true;
    }

    @Override
    public void onAdded() {
        noGamesInDatabase = false;
        playerHandler.unRegisterOnPlayersAddedLister();
    }
}