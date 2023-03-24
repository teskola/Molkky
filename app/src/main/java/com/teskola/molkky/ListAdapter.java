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
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int ADD_PLAYER_VIEW = 0;
    public static final int SELECT_PLAYER_VIEW = 1;
    public static final int SAVED_GAMES_ACTIVITY = 2;
    public static final int STATS_ACTIVITY = 3;
    public static final int GAME_ACTIVITY = 4;

    private Context context;
    private int viewId;

    private List<? extends PlayerInfo> players;
    private List<GameInfo> games;

    private int statID = 0;
    private boolean showImages, showTosses, onlyGray;
    private ArrayList<Boolean> selected = null;

    private final OnItemClickListener onItemClickListener;

    public void setStatID(int statID) {
        this.statID = statID;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewId == GAME_ACTIVITY) {
            view = inflater.inflate(R.layout.vertical_player_view, parent, false);
            return new GameViewHolder(view);
        }
        view = inflater.inflate(R.layout.player_card, parent, false);
        return new DefaultViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DefaultViewHolder defaultViewHolder = null;
        if (viewId != GAME_ACTIVITY)
            defaultViewHolder = (DefaultViewHolder) holder;

        switch (viewId) {
            case ADD_PLAYER_VIEW:
                defaultViewHolder.nameTV.setText(players.get(position).getName());
                break;
            case SELECT_PLAYER_VIEW:
                defaultViewHolder.nameTV.setText(players.get(position).getName());
                if (selected.get(position))
                    defaultViewHolder.playerView.setBackgroundResource(R.drawable.beige_white_background);
                else
                    defaultViewHolder.playerView.setBackgroundResource(R.drawable.gray_background);
                break;
            case SAVED_GAMES_ACTIVITY:
                defaultViewHolder.nameTV.setText(games.get(position).getData());
                break;
            case STATS_ACTIVITY:
                defaultViewHolder.nameTV.setText(players.get(position).getName());
                defaultViewHolder.valueTV.setVisibility(View.VISIBLE);
                PlayerStats playerStats = (PlayerStats) players.get(position);
                switch (statID) {
                    case R.string.games:
                        defaultViewHolder.valueTV.setText(String.valueOf(playerStats.getGamesCount()));
                        break;
                    case R.string.wins:
                        defaultViewHolder.valueTV.setText(String.valueOf(playerStats.getWins()));
                        break;
                    case R.string.points:
                        defaultViewHolder.valueTV.setText(String.valueOf(playerStats.getPoints()));
                        break;
                    case R.string.tosses:
                        defaultViewHolder.valueTV.setText(String.valueOf(playerStats.getTossesCount()));
                        break;
                    case R.string.points_per_toss:
                        defaultViewHolder.valueTV.setText(String.format("%.1f", playerStats.getPointsPerToss()));
                        break;
                    case R.string.hits_percentage:
                        int hitsPct = Math.round(100 * playerStats.getHitsPct());
                        defaultViewHolder.valueTV.setText(String.valueOf(hitsPct));
                        break;
                    case R.string.elimination_percentage:
                        int elimPct = Math.round(100 * playerStats.getEliminationsPct());
                        defaultViewHolder.valueTV.setText(String.valueOf(elimPct));
                        break;
                    case R.string.excesses_per_game:
                        defaultViewHolder.valueTV.setText(String.format("%.1f", playerStats.getExcessesPerGame()));
                        break;
                }
                break;
            case GAME_ACTIVITY:

                GameViewHolder gameViewHolder = (GameViewHolder) holder;

                Player player = (Player) players.get(position);
                int total = player.countAll();
                gameViewHolder.playerNameTextView.setText(player.getName());
                gameViewHolder.totalPointsTextView.setText(String.valueOf(total));
                if (showTosses) {
                    gameViewHolder.pointsTV.setVisibility(View.VISIBLE);
                    gameViewHolder.pointsTV.setText(buildTossesString(position));
                }
                else
                    gameViewHolder.pointsTV.setVisibility(View.GONE);
                gameViewHolder.playerCardView.setBackgroundResource(Colors.selectBackground(player, onlyGray));
                break;
        }

        // Add images

        if (showImages && (viewId == ADD_PLAYER_VIEW || viewId == SELECT_PLAYER_VIEW || viewId == STATS_ACTIVITY)) {
            Bitmap photo = ImageHandler.getInstance(context).getPhoto(players.get(position).getId());
            defaultViewHolder.playerImageView.setScaleType(ImageView.ScaleType.CENTER);
            if (photo != null) {
                defaultViewHolder.playerImageView.setImageBitmap(photo);
                defaultViewHolder.playerImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            else {
                DefaultViewHolder finalDefaultViewHolder = defaultViewHolder;
                ImageHandler.getInstance(context).downloadFromFirestorage(context, players.get(position).getId(), players.get(position).getName(), new ImageHandler.ImageListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        finalDefaultViewHolder.playerImageView.setImageBitmap(bitmap);
                        finalDefaultViewHolder.playerImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }

                    @Override
                    public void onFailure() {
                        finalDefaultViewHolder.playerImageView.setImageResource(R.drawable.camera);
                        finalDefaultViewHolder.playerImageView.setScaleType(ImageView.ScaleType.CENTER);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        if (viewId == SAVED_GAMES_ACTIVITY)
            return games.size();
        else
            return players.size();
    }

    public interface OnItemClickListener {
        void onSelectClicked(int position);

        void onDeleteClicked(int position);

        void onImageClicked(String id, String name, int position, ImagesActivity.OnImageAdded listener);
    }

    @SuppressWarnings("unchecked")
    public ListAdapter(Context context, List<?> data, boolean showImages, OnItemClickListener listener) {
        this.onItemClickListener = listener;
        this.context = context;
        this.showImages = showImages;
        switch (context.getClass().getName()) {
            case ("com.teskola.molkky.MainActivity"):
                viewId = ADD_PLAYER_VIEW;
                this.players = (List<PlayerInfo>) data;
                break;
            case ("com.teskola.molkky.SelectPlayersActivity"):
                viewId = SELECT_PLAYER_VIEW;
                this.players = (List<PlayerInfo>) data;
                break;
            case ("com.teskola.molkky.SavedGamesActivity"):
                viewId = SAVED_GAMES_ACTIVITY;
                this.games = (List<GameInfo>) data;
                break;
            case ("com.teskola.molkky.AllStatsActivity"):
                viewId = STATS_ACTIVITY;
                this.players = (List<PlayerStats>) data;
                break;
            case ("com.teskola.molkky.GameActivity"):
                viewId = GAME_ACTIVITY;
                this.players = (List<Player>) data;
                break;
        }
    }

    public ListAdapter(Context context, List<PlayerInfo> players, ArrayList<Boolean> selected, boolean showImages, OnItemClickListener listener) {
        this.onItemClickListener = listener;
        this.viewId = SELECT_PLAYER_VIEW;
        this.players = players;
        this.selected = selected;
        this.showImages = showImages;
        this.context = context;
    }

    public ListAdapter(List<Player> playersList, boolean onlyGray, boolean showTosses, OnItemClickListener listener) {
        this.onItemClickListener = listener;
        this.viewId = GAME_ACTIVITY;
        this.showTosses = showTosses;
        this.onlyGray = onlyGray;
        this.players = playersList;
    }

    public class GameViewHolder extends RecyclerView.ViewHolder {
        private final TextView playerNameTextView;
        private final TextView totalPointsTextView;
        private final TextView pointsTV;
        private final View playerCardView;

        @SuppressLint("ClickableViewAccessibility")
        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
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
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onSelectClicked(position);
                }
            });
        }
    }

    public class DefaultViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTV;
        private final TextView valueTV;
        private final View playerView;
        private final ShapeableImageView playerImageView;

        @SuppressLint("ClickableViewAccessibility")
        public DefaultViewHolder(View itemView) {
            super(itemView);
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
                    int position = DefaultViewHolder.this.getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        onItemClickListener.onImageClicked(players.get(position).getId(), players.get(position).getName(), position, null);
                });
            } else playerImageView.setVisibility(View.GONE);


            nameTV.setOnClickListener(view -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (viewId != ADD_PLAYER_VIEW)
                        onItemClickListener.onSelectClicked(position);
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
                            if (onItemClickListener != null) {
                                int position = getAbsoluteAdapterPosition();
                                if (position != RecyclerView.NO_POSITION) {
                                    onItemClickListener.onDeleteClicked(position);
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
        Player player = (Player) players.get(position);
        ArrayList<Integer> tosses =  player.getTosses();
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
