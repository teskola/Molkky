package com.teskola.molkky;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder>{
    private onItemClickListener mListener;
    private ArrayList<PlayerInfo> players = null;
    private ArrayList<GameInfo> games = null;
    private ArrayList<PlayerStats> playerStats = null;
    private int statID;
    private ArrayList<Boolean> selected = null;
    private int selected_position = RecyclerView.NO_POSITION;
    private Context context;

    private boolean showImages;
    private int viewId;
    public static final int ADD_PLAYER_VIEW = 0;
    public static final int SELECT_PLAYER_VIEW = 1;
    public static final int SAVED_GAMES_ACTIVITY = 2;
    public static final int STATS_ACTIVITY = 3;

    public void setSelected_position(int selected_position) {
        this.selected_position = selected_position;
    }
    public int getSelected_position() {
        return selected_position;
    }

    public void setStatID(int statID) {
        this.statID = statID;
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
        if (showImages) {
            ImageHandler imageHandler = new ImageHandler(context);
            String path = "";
            if (players != null)
                path = imageHandler.getImagePath(players.get(position).getName());
            else if (playerStats != null)
                path = imageHandler.getImagePath(playerStats.get(position).getName());
            if (path != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                holder.playerImageView.setImageBitmap(bitmap);
                holder.playerImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            else {
                holder.playerImageView.setImageResource(R.drawable.camera);
                holder.playerImageView.setScaleType(ImageView.ScaleType.CENTER);
            }
        }

        if (viewId == ADD_PLAYER_VIEW) {
            holder.nameTV.setText(players.get(position).getName());
            holder.playerView.setSelected(selected_position == position);
        }
        if (viewId == SELECT_PLAYER_VIEW) {
            holder.nameTV.setText(players.get(position).getName());
            if (selected.get(position)) holder.playerView.setBackgroundResource(R.drawable.beige_white_background);
            else
                holder.playerView.setBackgroundResource(R.drawable.gray_background);
        }

        if (viewId == SAVED_GAMES_ACTIVITY) {
            holder.nameTV.setText(games.get(position).getData());
        }

        if (viewId == STATS_ACTIVITY) {
            holder.valueTV.setVisibility(View.VISIBLE);
            holder.nameTV.setText(playerStats.get(position).getName());
            switch (statID) {
                case R.string.games:
                    holder.valueTV.setText(String.valueOf(playerStats.get(position).getGamesCount()));
                    break;
                case R.string.wins:
                    holder.valueTV.setText(String.valueOf(playerStats.get(position).getWins()));
                    break;
                case R.string.points:
                    holder.valueTV.setText(String.valueOf(playerStats.get(position).getPoints()));
                    break;
                case R.string.tosses:
                    holder.valueTV.setText(String.valueOf(playerStats.get(position).getTossesCount()));
                    break;
                case R.string.points_per_toss:
                    holder.valueTV.setText(String.format("%.1f", playerStats.get(position).getPointsPerToss()));
                    break;
                case R.string.hits_percentage:
                    int hitsPct = Math.round(100*playerStats.get(position).getHitsPct());
                    holder.valueTV.setText(String.valueOf(hitsPct));
                    break;
                case R.string.elimination_percentage:
                    int elimPct = Math.round(100*playerStats.get(position).getEliminationsPct());
                    holder.valueTV.setText(String.valueOf(elimPct));
                    break;
                case R.string.excesses_per_game:
                    holder.valueTV.setText(String.format("%.1f", playerStats.get(position).getExcessesPerGame()));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (players != null) return players.size();
        if (games != null) return games.size();
        return playerStats.size();
    }

    public interface onItemClickListener {
        void onSelectClicked(int position);
        void onDeleteClicked(int position);
        void onImageClicked(int position);
    }
    public void setOnItemClickListener (onItemClickListener listener) { mListener = listener;}


    public ListAdapter (Context context, ArrayList<PlayerInfo> players, ArrayList<PlayerStats> playerStats, ArrayList<GameInfo> gameInfos, boolean showImages) {
        if (players != null) {
            this.players = players;
            this.viewId = ADD_PLAYER_VIEW;
        }
        else if (playerStats != null) {
            this.playerStats = playerStats;
            this.viewId = STATS_ACTIVITY;
            this.statID = 0;
        }
        else if (gameInfos != null) {
            this.games = gameInfos;
            this.viewId = SAVED_GAMES_ACTIVITY;
        }
        this.showImages = showImages;
        this.context = context;
    }

    public ListAdapter (Context context, ArrayList<PlayerInfo> players, ArrayList<Boolean> selected, boolean showImages) {
        this.viewId = SELECT_PLAYER_VIEW;
        this.players = players;
        this.selected = selected;
        this.showImages = showImages;
        this.context =context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTV;
        private final TextView valueTV;
        private final View playerView;
        private final ShapeableImageView playerImageView;

        @SuppressLint("ClickableViewAccessibility")
        public MyViewHolder (View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.nameTV);
            valueTV = itemView.findViewById(R.id.valueTV);
            playerView = itemView.findViewById(R.id.listItemView);
            playerImageView = itemView.findViewById(R.id.playerImageView);
            ImageView removePlayer = itemView.findViewById(R.id.removePlayerButton);

            if (viewId == SAVED_GAMES_ACTIVITY) playerImageView.setVisibility(View.GONE);
            if (viewId == STATS_ACTIVITY) valueTV.setVisibility(View.VISIBLE);
            if (viewId == ADD_PLAYER_VIEW) removePlayer.setVisibility(View.VISIBLE);
            else removePlayer.setVisibility(View.GONE);

            if (showImages) {
                playerImageView.setVisibility(View.VISIBLE);
                playerImageView.setOnClickListener(view -> {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        mListener.onImageClicked(position);
                });
            }
            else playerImageView.setVisibility(View.GONE);


            nameTV.setOnClickListener(view -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && (viewId == SAVED_GAMES_ACTIVITY || viewId == STATS_ACTIVITY)) {
                    mListener.onSelectClicked(position);
                }
                if (position != RecyclerView.NO_POSITION && viewId==SELECT_PLAYER_VIEW) {
                    selected.set(position, !selected.get(position));
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
