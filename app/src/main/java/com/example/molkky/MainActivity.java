package com.example.molkky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Player> playersList = new ArrayList<>();
    private boolean random = false;
    private int start_position = -1;
    private DBHandler dbHandler;

    private TextView firstTextView;
    private RecyclerView recyclerview;
    private EditText editPlayerName;
    private Button startButton;

    // https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
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
/*
*   Adds new player to list. If fails, returns false.
* */
    public void addPlayer (AddPlayersAdapter adapter) {
        Player newPlayer = new Player(editPlayerName.getText().toString());
        for (Player p : playersList) {
            if (p.getName().equals(newPlayer.getName())) {
                Toast.makeText(this, getString(R.string.already_added, newPlayer.getName()), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        playersList.add(0, newPlayer);
        if (playersList.size() > 1) {
            startButton.setEnabled(true);
        }
        editPlayerName.setText("");
        adapter.notifyItemInserted(0);
        if (!random && adapter.getSelected_position() != RecyclerView.NO_POSITION) {
            adapter.setSelected_position(adapter.getSelected_position() + 1);
            setStarter(adapter.getSelected_position());
        }
        Objects.requireNonNull(recyclerview.getLayoutManager()).scrollToPosition(0);
    }

    public void deletePlayer (AddPlayersAdapter adapter, int position) {
        playersList.remove(position);
        if (playersList.size() < 2) {
            startButton.setEnabled(false);
        }
        start_position = adapter.getSelected_position();
        if (playersList.isEmpty() || start_position == RecyclerView.NO_POSITION) {
            hideFirstTextView();
        }
        adapter.notifyItemRemoved(position);
    }

    public void randomSelected(AddPlayersAdapter adapter) {
        hideFirstTextView();
        random = true;
        int selPos = adapter.getSelected_position();
        adapter.setSelected_position(RecyclerView.NO_POSITION);
        start_position = RecyclerView.NO_POSITION;
        adapter.notifyItemChanged(selPos);
    }

    public void showFirstTextView() {
        firstTextView.setVisibility(View.VISIBLE);
    }

    public void hideFirstTextView() {
        firstTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            playersList = new ArrayList<>();
            Player[] players = new Gson().fromJson(savedInstanceState.getString("PLAYERS"), Player[].class);
            Collections.addAll(playersList, players);
            start_position = savedInstanceState.getInt("SELECTED_POSITION");
            random = savedInstanceState.getBoolean("RANDOM");
        }

        if (getIntent().getStringExtra("json") != null) {
            playersList = new ArrayList<>();
            String json = getIntent().getStringExtra("json");
            Player[] players = new Gson().fromJson(json, Player[].class);
            Collections.addAll(playersList, players);
        }

        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.startButton);
        recyclerview = findViewById(R.id.recyclerView);
        editPlayerName = findViewById(R.id.editTextPlayerName);
        editPlayerName.setImeActionLabel(getResources().getString(R.string.add), EditorInfo.IME_ACTION_DONE);
        firstTextView = findViewById(R.id.firstTextView);
        CheckBox randomCheckBox = findViewById(R.id.randomCheckBox);
        ImageButton addButton = findViewById(R.id.addButton);
        ImageButton selectButton = findViewById(R.id.selectButton);

        dbHandler = new DBHandler(MainActivity.this);
        AddPlayersAdapter myAdapter = new AddPlayersAdapter(playersList, AddPlayersAdapter.ADD_PLAYER_VIEW);
        myAdapter.setSelected_position(start_position);
        recyclerview.setAdapter(myAdapter);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        if (random) {
            randomCheckBox.setChecked(true);
            randomSelected(myAdapter);
        } else if (myAdapter.getSelected_position() != RecyclerView.NO_POSITION) {
            setStarter(myAdapter.getSelected_position());
        }
        if (playersList.size() > 1) {
            startButton.setEnabled(true);
        }
        // https://stackoverflow.com/questions/1489852/android-handle-enter-in-an-edittext
        editPlayerName.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE && editPlayerName.getText().length() > 0) {
                addPlayer(myAdapter);
                handled = true;
            }
            return handled;
        });

        addButton.setOnClickListener(view -> {

            if (editPlayerName.getText().length() > 0) {
                addPlayer(myAdapter);
            }
        });

        selectButton.setOnClickListener(view -> selectPlayers());

        randomCheckBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                randomSelected(myAdapter);

            } else {
                random = false;
                if (myAdapter.getSelected_position() != RecyclerView.NO_POSITION) {
                    showFirstTextView();
                }
            }
        });

        startButton.setOnClickListener(view -> startGame());

        myAdapter.setOnItemClickListener(new AddPlayersAdapter.OnItemClickListener() {
            @Override
            public void onSelectClick(int position) {
                setStarter(position);
            }

            @Override
            public void onDeleteClick(int position) {
                deletePlayer(myAdapter, position);
            }
        });

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
            intent.putExtra("json", json);
        }
        if (dbHandler.playersTableSize() > 0) {
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.no_saved_players), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        String json = new Gson().toJson(playersList);
        savedInstanceState.putString("PLAYERS", json);
        savedInstanceState.putBoolean("RANDOM", random);
        savedInstanceState.putInt("SELECTED_POSITION", start_position);
    }
}