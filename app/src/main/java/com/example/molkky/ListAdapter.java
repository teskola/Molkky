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

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder>{
    private onItemClickListener mListener;
    private final ArrayList<ListItem> listItems;
    private final boolean showValue;
    private int valueType = INT;

    private int selected_position = RecyclerView.NO_POSITION;
    private final int viewId;
    public static final int ADD_PLAYER_VIEW = 0;
    public static final int SELECT_PLAYER_VIEW = 1;
    public static final int SAVED_GAMES_ACTIVITY = 2;
    public static final int STATS_ACTIVITY = 3;

    public static final int INT = 0;
    public static final int FLOAT = 1;

    public void setSelected_position(int selected_position) {
        this.selected_position = selected_position;
    }
    public int getSelected_position() {
        return selected_position;
    }

    @NonNull
    @Override
    public ListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.player_card, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ListAdapter.MyViewHolder holder, int position) {
        holder.nameTV.setText(listItems.get(position).getName());
        holder.playerView.setSelected(selected_position == position);
        if (viewId == SELECT_PLAYER_VIEW) {
            holder.playerView.setBackgroundResource(listItems.get(position).isSelected() ? R.drawable.beige_white_background : R.drawable.gray_background);
        }
        if (valueType == INT) {
            int valueInt = listItems.get(position).getValueInt();
            holder.valueTV.setText(String.valueOf(valueInt));
        } else {
            float valueFloat = listItems.get(position).getValueFloat();
            holder.valueTV.setText(String.format("%.1f", valueFloat));
        }
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public void setValueType(int valueType) {
        this.valueType = valueType;
    }

    public interface onItemClickListener {
        void onSelectClicked(int position);
        void onDeleteClicked(int position);
    }
    public void setOnItemClickListener (onItemClickListener listener) { mListener = listener;}
    public ListAdapter (ArrayList<ListItem> listItems, boolean showValue, int viewId) {
        this.viewId = viewId;
        this.showValue = showValue;
        this.listItems = listItems;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTV;
        private final TextView valueTV;
        private final View playerView;

        @SuppressLint("ClickableViewAccessibility")
        public MyViewHolder (View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.nameTV);
            valueTV = itemView.findViewById(R.id.valueTV);
            playerView = itemView.findViewById(R.id.listItemView);
            ImageView removePlayer = itemView.findViewById(R.id.removePlayerButton);
            if (showValue) valueTV.setVisibility(View.VISIBLE);
            if (viewId == ADD_PLAYER_VIEW) removePlayer.setVisibility(View.VISIBLE);
            else removePlayer.setVisibility(View.GONE);
            nameTV.setOnClickListener(view -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && viewId > 1) {
                    mListener.onSelectClicked(position);
                }
                if (position != RecyclerView.NO_POSITION && viewId==SELECT_PLAYER_VIEW) {
                    listItems.get(position).setSelected(!listItems.get(position).isSelected());
                    notifyItemChanged(position);
                }
                if (position != RecyclerView.NO_POSITION && viewId==ADD_PLAYER_VIEW) {
                    notifyItemChanged(selected_position);
                    selected_position = position;
                    notifyItemChanged(selected_position);
                    mListener.onSelectClicked(position);
                }
            });
            if (removePlayer.getVisibility() == View.VISIBLE) {
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
                                mListener.onDeleteClicked(position);
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
}
