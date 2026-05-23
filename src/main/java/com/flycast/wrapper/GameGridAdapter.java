package com.flycast.wrapper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GameGridAdapter extends RecyclerView.Adapter<GameGridAdapter.GameViewHolder> {

    public interface OnGameClickListener {
        void onGameClick(GameEntry entry);
        void onGameLongClick(GameEntry entry, int position);
    }

    private final List<GameEntry> games;
    private final OnGameClickListener listener;

    public GameGridAdapter(@NonNull List<GameEntry> games,
                           @NonNull OnGameClickListener listener) {
        this.games = games;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameEntry entry = games.get(position);
        holder.bind(entry, listener, position);
    }

    @Override
    public int getItemCount() {
        return games == null ? 0 : games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivGameCover;
        private final TextView tvGameTitle;

        GameViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGameCover = itemView.findViewById(R.id.ivGameCover);
            tvGameTitle = itemView.findViewById(R.id.tvGameTitle);
        }

        void bind(final GameEntry entry,
                  final OnGameClickListener listener,
                  final int position) {

            tvGameTitle.setText(entry.getTitle());
            ivGameCover.setImageResource(R.drawable.dc_swirl);

            itemView.setOnClickListener(v -> listener.onGameClick(entry));
            itemView.setOnLongClickListener(v -> {
                listener.onGameLongClick(entry, position);
                return true;
            });
        }
    }
}

