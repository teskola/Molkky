package com.example.molkky;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VerticalAdapter extends RecyclerView.Adapter<VerticalAdapter.MyViewHolder> {
    private final ArrayList<Player> players;
    private final boolean onlyGray;
    private final boolean showTosses;
    

    public VerticalAdapter(ArrayList<Player> playersList, boolean onlyGray, boolean showTosses) {
        this.showTosses = showTosses;
        this.onlyGray = onlyGray;
        this.players = playersList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.vertical_player_view, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        int total = players.get(position).countAll();
        holder.playerNameTextView.setText(players.get(position).getName());
        holder.totalPointsTextView.setText(String.valueOf(total));
        if (showTosses) {
            int size = players.get(position).getTossesSize();
            if (size > 0) {
                int value = players.get(position).getToss(size - 1);
                holder.points0TV.setText(String.valueOf(value));
            } else {
                holder.points0TV.setText("");
            }
            if (size > 1) {
                int value = players.get(position).getToss(size - 2);
                holder.points1TV.setText(String.valueOf(value));
            } else {
                holder.points1TV.setText("");
            }
            if (size > 2) {
                int value = players.get(position).getToss(size - 3);
                holder.points2TV.setText(String.valueOf(value));
            } else {
                holder.points2TV.setText("");
            }
        }
        holder.playerCardView.setBackgroundResource(MyViewHolder.selectBackground(players.get(position), onlyGray));

    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView playerNameTextView;
        TextView totalPointsTextView;
        TextView points0TV;
        TextView points1TV;
        TextView points2TV;
        View playerCardView;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            playerCardView = itemView.findViewById(R.id.playerCardView);
            playerNameTextView = itemView.findViewById(R.id.playerNameTextView);
            totalPointsTextView = itemView.findViewById(R.id.totalPointsTextView);
            points0TV = itemView.findViewById(R.id.points0TV);
            points1TV = itemView.findViewById(R.id.points1TV);
            points2TV = itemView.findViewById(R.id.points2TV);
        }

        public static int selectBackground (Player player, boolean onlyGray) {
            final int PURPLE = R.drawable.purple_background;
            final int GRAY = R.drawable.gray_background;
            final int GREEN = R.drawable.green_background;
            final int GREEN_YELLOW = R.drawable.green_yellow_background;
            final int GREEN_ORANGE = R.drawable.green_orange_background;
            final int YELLOW = R.drawable.yellow_background;
            final int ORANGE = R.drawable.orange_background;
            final int BEIGE = R.drawable.beige_white_background;

            int size = player.getTossesSize();
            if (size > 2 && player.getToss(size -1) == 0
                    && player.getToss(size -2) == 0
                    && player.getToss(size -3) == 0)
                return GRAY;
            if (!onlyGray) {
                int misses = 0;
                if (size > 1 && player.getToss(size - 1) == 0 && player.getToss(size - 2) == 0)
                    misses = 2;
                else if (size > 0 && player.getToss(size - 1) == 0)
                    misses = 1;
                if (size > 4 && player.countAll() == 25 && player.getToss(size - 1) != 0)
                    return PURPLE;
                if (player.pointsToWin() == 0) {
                    if (misses == 2)
                        return ORANGE;
                    if (misses == 1)
                        return YELLOW;
                    else
                        return BEIGE;
                } else {
                    if (misses == 2)
                        return GREEN_ORANGE;
                    if (misses == 1)
                        return GREEN_YELLOW;
                }
                return GREEN;
            }
            return BEIGE;
        }
    }
}
