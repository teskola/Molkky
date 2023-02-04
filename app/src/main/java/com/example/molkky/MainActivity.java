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

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Player> playersList = new ArrayList<>();
    private boolean random = false;
    private int start_position = -1;

    private TextView firstTextView;
    private RecyclerView recyclerview;
    private EditText editPlayerName;
    private CheckBox randomCheckBox;
    private Button startButton;
    private ImageButton selectButton;
    private ImageButton addButton;

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

    public void addPlayer (AddPlayersAdapter adapter) {
        playersList.add(0, new Player(editPlayerName.getText().toString()));
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
        randomCheckBox = findViewById(R.id.randomCheckBox);
        addButton = findViewById(R.id.addButton);
        selectButton = findViewById(R.id.selectButton);

        AddPlayersAdapter myAdapter = new AddPlayersAdapter(playersList);
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
        editPlayerName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE && editPlayerName.getText().length() > 0) {
                    addPlayer(myAdapter);
                    handled = true;
                }
                return handled;
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editPlayerName.getText().length() > 0) {
                    addPlayer(myAdapter);
                }
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        randomCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    randomSelected(myAdapter);

                } else {
                    random = false;
                    if (myAdapter.getSelected_position() != RecyclerView.NO_POSITION) {
                        showFirstTextView();
                    }
                }
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        String json = new Gson().toJson(playersList);
        savedInstanceState.putString("PLAYERS", json);
        savedInstanceState.putBoolean("RANDOM", random);
        savedInstanceState.putInt("SELECTED_POSITION", start_position);
    }
}