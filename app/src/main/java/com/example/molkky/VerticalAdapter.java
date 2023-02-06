package com.example.molkky;

import android.annotation.SuppressLint;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onSelectClick(int position);
    }

    public void setOnItemClickListener (OnItemClickListener listener) {
        mListener = listener;
    }



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

//        holder.stats_TossesTV.setText(player.getTossesSize());
//        holder.stats_bestTV.setText(Collections.max(player.getTosses()));
//        holder.stats_meanTV.setText(String.valueOf(player.mean()));
//        holder.stats_ZeroesTV.setText(String.valueOf(Collections.frequency(player.getTosses(), 0)));
//        holder.stats_ModeTV.setText(String.valueOf(player.mode()));
//        holder.stats_ExcessesTV.setText(String.valueOf(player.countExcesses()));

    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView playerNameTextView;
        private final TextView totalPointsTextView;
        private final TextView pointsTV;
        private final View playerCardView;

//        private View statsView;
//        private TextView stats_TossesTV;
//        private TextView stats_bestTV;
//        private TextView stats_meanTV;
//        private TextView stats_ZeroesTV;
//        private TextView stats_ModeTV;
//        private TextView stats_ExcessesTV;





        @SuppressLint("ClickableViewAccessibility")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            playerCardView = itemView.findViewById(R.id.playerCardView);
            playerNameTextView = itemView.findViewById(R.id.playerNameTextView);
            totalPointsTextView = itemView.findViewById(R.id.totalPointsTextView);
            pointsTV = itemView.findViewById(R.id.pointsTV);

//            statsView = itemView.findViewById(R.id.statsView);
//            stats_TossesTV = itemView.findViewById(R.id.stats_TossesTV);
//            stats_bestTV = itemView.findViewById(R.id.stats_bestTV);
//            stats_meanTV = itemView.findViewById(R.id.stats_meanTV);
//            stats_ZeroesTV = itemView.findViewById(R.id.stats_ZeroesTV);
//            stats_ModeTV = itemView.findViewById(R.id.stats_ModeTV);
//            stats_ExcessesTV = itemView.findViewById(R.id.stats_ExcessesTV);


           // https://stackoverflow.com/questions/38741787/scroll-textview-inside-recyclerview

            pointsTV.setOnTouchListener((view, motionEvent) -> {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });
            pointsTV.setMovementMethod(new ScrollingMovementMethod());
            playerCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && mListener != null) {
                        mListener.onSelectClick(position);
                    }
                }
            });
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
