package com.example.molkky;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
/*
*  https://youtu.be/18VcnYN5_LM
*  https://youtu.be/__OMnFR-wZU
*  https://youtu.be/HtwDXRWjMcU
*  https://www.youtube.com/watch?v=bhhs4bwYyhc
*  https://stackoverflow.com/questions/27194044/how-to-properly-highlight-selected-item-on-recyclerview
* */
public class AddPlayersAdapter extends RecyclerView.Adapter<AddPlayersAdapter.MyViewHolder>{

    private int selected_position = RecyclerView.NO_POSITION;
    private OnItemClickListener mListener;
    private ArrayList<Player> playersList;

    public interface OnItemClickListener {
        void onSelectClick(int position);
        void onDeleteClick(int position);
    }
    public void setSelected_position(int selected_position) {
        this.selected_position = selected_position;
    }
    public int getSelected_position() {
        return selected_position;
    }

    public void setOnItemClickListener (OnItemClickListener listener) {
        mListener = listener;
    }

    public AddPlayersAdapter(ArrayList<Player> playersList) {
        this.playersList = playersList;
    }

    @NonNull
    @Override
    public AddPlayersAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.player_card, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddPlayersAdapter.MyViewHolder holder, int position) {
        holder.playerName.setText(playersList.get(position).getName());
        holder.playerView.setSelected(selected_position == position);
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView playerName;
        private ImageView removePlayer;
        private View playerView;

        @SuppressLint("ClickableViewAccessibility")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.playerTextView);
            removePlayer = itemView.findViewById(R.id.removePlayerButton);
            playerView = itemView.findViewById(R.id.playerView);
            playerName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        notifyItemChanged(selected_position);
                        selected_position = position;
                        notifyItemChanged(selected_position);
                        mListener.onSelectClick(position);
                    }
                }
            });
            removePlayer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int background;
                    if (playerView.isSelected())
                        background = R.drawable.yellow_background;
                    else
                        background = R.drawable.beige_white_background;
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        {
                            playerView.setBackgroundResource(R.drawable.orange_background);
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        {
                            if (mListener != null) {
                                int position = getAbsoluteAdapterPosition();
                                if (selected_position == position) {
                                    selected_position = RecyclerView.NO_POSITION;
                                } else if (selected_position > position) {
                                    selected_position -= 1;
                                }
                                if (position != RecyclerView.NO_POSITION) {
                                    mListener.onDeleteClick(position);
                                }
                            }
                            playerView.setBackgroundResource(R.drawable.beige_white_background);
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL:
                        {
                            playerView.setBackgroundResource(background);
                            break;
                        }
                    }
                    return true;
                }
            });
        }
    }
}
