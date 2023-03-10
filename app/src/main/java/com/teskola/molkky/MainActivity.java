package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends OptionsActivity implements ListAdapter.OnItemClickListener {

    private ArrayList<PlayerInfo> playersList = new ArrayList<>();
    private boolean random = false;
    private int start_position = RecyclerView.NO_POSITION;
    private ListAdapter listAdapter;
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private final ImageHandler imageHandler = new ImageHandler(this);
    private TextView firstTextView;
    private RecyclerView recyclerview;
    private EditText editPlayerName;
    private Button startButton;
    private CheckBox randomCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.startButton);
        recyclerview = findViewById(R.id.recyclerView);
        editPlayerName = findViewById(R.id.editTextPlayerName);
        editPlayerName.setImeActionLabel(getResources().getString(R.string.add), EditorInfo.IME_ACTION_DONE);
        firstTextView = findViewById(R.id.firstTextView);
        randomCheckBox = findViewById(R.id.randomCheckBox);
        ImageButton addButton = findViewById(R.id.addButton);
        ImageButton selectButton = findViewById(R.id.selectButton);

        if (savedInstanceState != null) {
            playersList = new ArrayList<>();
            PlayerInfo[] players = new Gson().fromJson(savedInstanceState.getString("PLAYERS"), PlayerInfo[].class);
            Collections.addAll(playersList, players);
            start_position = savedInstanceState.getInt("SELECTED_POSITION");
            random = savedInstanceState.getBoolean("RANDOM");
        }

        preferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        listener = (sharedPreferences, key) -> {
            if (key.equals("SHOW_IMAGES")) {
                createRecyclerView();
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);

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

        selectButton.setOnClickListener(view -> selectPlayers());

        randomCheckBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                randomSelected();

            } else {
                random = false;
                if (listAdapter.getSelected_position() != RecyclerView.NO_POSITION) {
                    showFirstTextView();
                }
            }
        });
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
    }

    @Override
    protected void onResume () {
        super.onResume();
        start_position = getIntent().getIntExtra("SELECTED_POSITION", RecyclerView.NO_POSITION);
        random = getIntent().getBooleanExtra("RANDOM", false);
        createRecyclerView();

        if (random) {
            randomCheckBox.setChecked(true);
            randomSelected();
        } else if (listAdapter.getSelected_position() != RecyclerView.NO_POSITION) {
            setStarter(listAdapter.getSelected_position());
        }
        if (playersList.size() > 1) {
            startButton.setEnabled(true);
        }
    }

    public void createRecyclerView () {
        listAdapter = new ListAdapter(this, playersList, preferences.getBoolean("SHOW_IMAGES", false), ListAdapter.ADD_PLAYER_VIEW, this);
        listAdapter.setSelected_position(start_position);
        recyclerview.setAdapter(listAdapter);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
    }

    protected void onActivityResult(int position, int resultCode, Intent data) {
        super.onActivityResult(position, resultCode, data);
        if (data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageHandler.BitmapToJpg(photo, playersList.get(position).getId());
            listAdapter.notifyItemChanged(position);
        }
    }


    private void startGame() {

        Intent intent = new Intent(this, GameActivity.class);
        String json = new Gson().toJson(playersList);
        intent.putExtra("json", json);
        intent.putExtra("first", start_position);
        intent.putExtra("random", random);
        startActivity(intent);
    }

    private void selectPlayers() {
        Intent intent = new Intent (this, SelectPlayersActivity.class);
        if (!playersList.isEmpty()) {
            String json = new Gson().toJson(playersList);
            intent.putExtra("SELECTED_PLAYERS", json);
        }
        if (LocalDatabaseManager.getInstance(this).getPlayerCount() > 0) {
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.no_saved_players), Toast.LENGTH_SHORT).show();
        }
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

    public void setStarter(int position) {
        start_position = position;
        firstTextView.setText(getString(R.string.first, playersList.get(position).getName()));
        if (!random) showFirstTextView();
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

        String playerId = LocalDatabaseManager.getInstance(this).getPlayerId(newPlayer.getName());
        if (playerId != null)
            newPlayer.setId(playerId);

        playersList.add(0, newPlayer);
        if (playersList.size() > 1) {
            startButton.setEnabled(true);
        }
        editPlayerName.setText("");
        listAdapter.notifyItemInserted(0);
        if (!random && listAdapter.getSelected_position() != RecyclerView.NO_POSITION) {
            listAdapter.setSelected_position(listAdapter.getSelected_position() + 1);
            setStarter(listAdapter.getSelected_position());
        }
        Objects.requireNonNull(recyclerview.getLayoutManager()).scrollToPosition(0);
    }

    public void deletePlayer (int position) {
        playersList.remove(position);
        if (playersList.size() < 2) {
            startButton.setEnabled(false);
        }
        start_position = listAdapter.getSelected_position();
        if (playersList.isEmpty() || start_position == RecyclerView.NO_POSITION) {
            hideFirstTextView();
        }
        listAdapter.notifyItemRemoved(position);
    }

    public void randomSelected() {
        hideFirstTextView();
        random = true;
        int selPos = listAdapter.getSelected_position();
        listAdapter.setSelected_position(RecyclerView.NO_POSITION);
        start_position = RecyclerView.NO_POSITION;
        listAdapter.notifyItemChanged(selPos);
    }

    public void showFirstTextView() {
        firstTextView.setVisibility(View.VISIBLE);
    }

    public void hideFirstTextView() {
        firstTextView.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        String json = new Gson().toJson(playersList);
        savedInstanceState.putString("PLAYERS", json);
        savedInstanceState.putBoolean("RANDOM", random);
        savedInstanceState.putInt("SELECTED_POSITION", start_position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.new_game).setVisible(false);
        return true;
    }
    @Override
    public void onSelectClicked(int position) {
        setStarter(position);
    }

    @Override
    public void onDeleteClicked(int position) {
        deletePlayer(position);
    }

    @Override
    public void onImageClicked(int position) {
        imageHandler.takePicture(position);
    }

    @Override
    public void onBackPressed () {
        moveTaskToBack(true);
    }

}