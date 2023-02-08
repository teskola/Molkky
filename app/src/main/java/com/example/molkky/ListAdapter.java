package com.example.molkky;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder>{
    private onItemClickListener mListener;
    private final ArrayList<ListItem> listItems;
    private final boolean showValue;
    private int valueType = INT;

    public static final int INT = 0;
    public static final int FLOAT = 1;

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
        void onItemClicked (int position);
    }
    public void setOnItemClickListener (onItemClickListener listener) { mListener = listener;}
    public ListAdapter (ArrayList<ListItem> listItems, boolean showValue) {
        this.showValue = showValue;
        this.listItems = listItems;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTV;
        private final TextView valueTV;

        public MyViewHolder (View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.nameTV);
            valueTV = itemView.findViewById(R.id.valueTV);
            if (showValue) valueTV.setVisibility(View.VISIBLE);
            nameTV.setOnClickListener(view -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClicked(position);
                }
            });

        }
    }

}
