package com.teskola.molkky;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {

    public static final int ADD_PLAYER_VIEW = 0;
    public static final int SELECT_PLAYER_VIEW = 1;
    public static final int SAVED_GAMES_ACTIVITY = 2;
    public static final int STATS_ACTIVITY = 3;
    public static final int GAME_ACTIVITY = 4;

    private ArrayList<PlayerInfo> playerInfos;
    private ArrayList<Player> players;
    private ArrayList<GameInfo> games = null;
    private ArrayList<PlayerStats> playerStats = null;

    private Context context;
    private int viewId;
    private int statID = 0;
    private boolean showImages;

    private ArrayList<Boolean> selected = null;
    private int selected_position = RecyclerView.NO_POSITION;
    private boolean showTosses, onlyGray;
    private onItemClickListener mListener;


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
        View view = viewId == GAME_ACTIVITY ? inflater.inflate(R.layout.vertical_player_view, parent, false)
                : inflater.inflate(R.layout.player_card, parent, false);

        return new MyViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ListAdapter.MyViewHolder holder, int position) {

        switch (viewId) {
            case ADD_PLAYER_VIEW:
                holder.nameTV.setText(playerInfos.get(position).getName());
                holder.playerView.setSelected(selected_position == position);
                break;
            case SELECT_PLAYER_VIEW:
                holder.nameTV.setText(playerInfos.get(position).getName());
                if (selected.get(position))
                    holder.playerView.setBackgroundResource(R.drawable.beige_white_background);
                else
                    holder.playerView.setBackgroundResource(R.drawable.gray_background);
                break;
            case SAVED_GAMES_ACTIVITY:
                holder.nameTV.setText(games.get(position).getData());
                break;
            case STATS_ACTIVITY:
                holder.nameTV.setText(playerStats.get(position).getName());
                holder.valueTV.setVisibility(View.VISIBLE);
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
                        int hitsPct = Math.round(100 * playerStats.get(position).getHitsPct());
                        holder.valueTV.setText(String.valueOf(hitsPct));
                        break;
                    case R.string.elimination_percentage:
                        int elimPct = Math.round(100 * playerStats.get(position).getEliminationsPct());
                        holder.valueTV.setText(String.valueOf(elimPct));
                        break;
                    case R.string.excesses_per_game:
                        holder.valueTV.setText(String.format("%.1f", playerStats.get(position).getExcessesPerGame()));
                        break;
                }
            case GAME_ACTIVITY:

                Player player = players.get(position);
                int total = player.countAll();
                holder.playerNameTextView.setText(player.getName());
                holder.totalPointsTextView.setText(String.valueOf(total));
                if (showTosses) {
                    holder.pointsTV.setVisibility(View.VISIBLE);
                    holder.pointsTV.setText(buildTossesString(position));
                }
                else
                    holder.pointsTV.setVisibility(View.GONE);
                holder.playerCardView.setBackgroundResource(GameActivity.selectBackground(player, onlyGray));
        }

        // Add images

        if (showImages && (viewId == ADD_PLAYER_VIEW || viewId == SELECT_PLAYER_VIEW || viewId == STATS_ACTIVITY)) {
            ImageHandler imageHandler = new ImageHandler(context);
            String path = "";
            if (playerInfos != null)
                path = imageHandler.getImagePath(playerInfos.get(position).getId());
            else if (playerStats != null)
                path = imageHandler.getImagePath(playerStats.get(position).getId());
            if (path != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                holder.playerImageView.setImageBitmap(bitmap);
                holder.playerImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                holder.playerImageView.setImageResource(R.drawable.camera);
                holder.playerImageView.setScaleType(ImageView.ScaleType.CENTER);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (playerInfos != null) return playerInfos.size();
        if (games != null) return games.size();
        if (players != null) return players.size();
        return playerStats.size();
    }

    public interface onItemClickListener {
        void onSelectClicked(int position);

        void onDeleteClicked(int position);

        void onImageClicked(int position);
    }

    public void setOnItemClickListener(onItemClickListener listener) {
        mListener = listener;
    }


    public ListAdapter(Context context, ArrayList<?> data, boolean showImages, int viewId) {
        this.context = context;
        this.viewId = viewId;
        this.showImages = showImages;

        this.playerInfos = (ArrayList<PlayerInfo>) data;
        this.playerStats = (ArrayList<PlayerStats>) data;
        this.games = (ArrayList<GameInfo>) data;

    }

    public ListAdapter(Context context, ArrayList<PlayerInfo> playerInfos, ArrayList<Boolean> selected, boolean showImages) {
        this.viewId = SELECT_PLAYER_VIEW;
        this.playerInfos = playerInfos;
        this.selected = selected;
        this.showImages = showImages;
        this.context = context;
    }

    public ListAdapter(ArrayList<Player> playersList, boolean onlyGray, boolean showTosses) {
        this.showTosses = showTosses;
        this.onlyGray = onlyGray;
        this.players = playersList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private  TextView nameTV;
        private  TextView valueTV;
        private  View playerView;
        private  ShapeableImageView playerImageView;

        private  TextView playerNameTextView;
        private  TextView totalPointsTextView;
        private  TextView pointsTV;
        private  View playerCardView;

        @SuppressLint("ClickableViewAccessibility")
        public MyViewHolder(View itemView) {
            super(itemView);

            if (viewId == GAME_ACTIVITY) {

                playerCardView = itemView.findViewById(R.id.playerCardView);
                playerNameTextView = itemView.findViewById(R.id.playerNameTextView);
                totalPointsTextView = itemView.findViewById(R.id.totalPointsTextView);
                pointsTV = itemView.findViewById(R.id.pointsTV);

                // https://stackoverflow.com/questions/38741787/scroll-textview-inside-recyclerview

                pointsTV.setOnTouchListener((view, motionEvent) -> {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                });
                pointsTV.setMovementMethod(new ScrollingMovementMethod());
                playerCardView.setOnClickListener(view -> {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && mListener != null) {
                        mListener.onSelectClicked(position);
                    }
                });
                return;
            }

            nameTV = itemView.findViewById(R.id.nameTV);
            valueTV = itemView.findViewById(R.id.valueTV);
            playerView = itemView.findViewById(R.id.listItemView);
            playerImageView = itemView.findViewById(R.id.playerImageView);
            ImageView removePlayer = itemView.findViewById(R.id.removePlayerButton);

            if (viewId == STATS_ACTIVITY) valueTV.setVisibility(View.VISIBLE);
            if (viewId == ADD_PLAYER_VIEW) removePlayer.setVisibility(View.VISIBLE);
            else removePlayer.setVisibility(View.GONE);

            if (showImages && viewId != SAVED_GAMES_ACTIVITY) {
                playerImageView.setVisibility(View.VISIBLE);
                playerImageView.setOnClickListener(view -> {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        mListener.onImageClicked(position);
                });
            } else playerImageView.setVisibility(View.GONE);


            nameTV.setOnClickListener(view -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (viewId == ADD_PLAYER_VIEW) {
                        notifyItemChanged(selected_position);
                        selected_position = position;
                        notifyItemChanged(selected_position);
                    }
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
                        case MotionEvent.ACTION_DOWN: {
                            playerView.setBackgroundResource(R.drawable.orange_background);
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
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
                        case MotionEvent.ACTION_CANCEL: {
                            playerView.setBackgroundResource(background);
                            break;
                        }
                    }
                    return true;
                });
            }
        }
    }

    public String buildTossesString(int position) {
        ArrayList<Integer> tosses = players.get(position).getTosses();
        StringBuilder sb = new StringBuilder();
        for (Integer toss : tosses) {
            if (toss < 10)
                sb.append(" ").append(toss);
            else
                sb.append(toss);
            sb.append("  ");
        }
        return sb.toString();
    }

}
