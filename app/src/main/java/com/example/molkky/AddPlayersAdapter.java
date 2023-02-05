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
    private final ArrayList<Player> playersList;
    private final int viewId;
    public static final int ADD_PLAYER_VIEW = 0;
    public static final int SELECT_PLAYER_VIEW = 1;


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

    public AddPlayersAdapter(ArrayList<Player> playersList, int viewId) {
        this.playersList = playersList;
        this.viewId = viewId;
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
        if (viewId == SELECT_PLAYER_VIEW) {
            holder.playerView.setBackgroundResource(playersList.get(position).isSelected() ? R.drawable.beige_white_background : R.drawable.gray_background);
        }
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView playerName;
        private final View playerView;

        @SuppressLint("ClickableViewAccessibility")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.playerTextView);
            ImageView removePlayer = itemView.findViewById(R.id.removePlayerButton);
            playerView = itemView.findViewById(R.id.playerView);

            if (viewId == SELECT_PLAYER_VIEW) removePlayer.setVisibility(View.GONE);


            playerName.setOnClickListener(view -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && viewId==SELECT_PLAYER_VIEW) {
                    playersList.get(position).setSelected(!playersList.get(position).isSelected());
                    notifyItemChanged(position);
                }
                if (position != RecyclerView.NO_POSITION && viewId==ADD_PLAYER_VIEW) {
                    notifyItemChanged(selected_position);
                    selected_position = position;
                    notifyItemChanged(selected_position);
                    mListener.onSelectClick(position);
                }
            });
            removePlayer.setOnTouchListener((view, motionEvent) -> {
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
            });
        }
    }
}
