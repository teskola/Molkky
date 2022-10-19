package com.example.molkky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private boolean random = false;
    private int start_position = -1;
    private TextView firstTextView;
    private RecyclerView recyclerview;
    private EditText editPlayerName;
    private ArrayList<String> playersList = new ArrayList<>();
    private CheckBox randomCheckBox;
    private Button startButton;
    private ImageButton addButton;

    public void setStarter(int position) {
        start_position = position;
        firstTextView.setText(getString(R.string.first, playersList.get(position)));
        if (!random) showFirstTextView();
    }

    public void addPlayer (AddPlayersAdapter adapter) {
        playersList.add(0, editPlayerName.getText().toString());
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
    public void hideKeyboard(View view) {
        https://stackoverflow.com/questions/1109022/how-to-close-hide-the-android-soft-keyboard-programmatically

        view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
            playersList = savedInstanceState.getStringArrayList("PLAYERS");
            start_position = savedInstanceState.getInt("SELECTED_POSITION");
            random = savedInstanceState.getBoolean("RANDOM");
        }

        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.startButton);
        recyclerview = findViewById(R.id.recyclerView);
        editPlayerName = findViewById(R.id.editTextPlayerName);
        editPlayerName.setImeActionLabel(getResources().getString(R.string.add), EditorInfo.IME_ACTION_DONE);
        firstTextView = findViewById(R.id.firstTextView);
        randomCheckBox = findViewById(R.id.randomCheckBox);
        addButton = findViewById(R.id.addButton);
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
                hideKeyboard(editPlayerName);
                if (editPlayerName.getText().length() > 0) {
                    addPlayer(myAdapter);
                }
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
        intent.putStringArrayListExtra("playersList", playersList);
        intent.putExtra("first", start_position);
        intent.putExtra("random", random);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList("PLAYERS", playersList);
        savedInstanceState.putBoolean("RANDOM", random);
        savedInstanceState.putInt("SELECTED_POSITION", start_position);
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

}