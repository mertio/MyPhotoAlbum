package me.mebubi.myalbum.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.mebubi.myalbum.database.model.Album;
import me.mebubi.myalbum.view.AlbumView;
import me.mebubi.myalbum.view.GoalView;

public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Album> albumList;
    private Context context;

    public AlbumAdapter(List<Album> albumList, Context context) {
        this.albumList = albumList;
        this.context = context;
    }

    public List<Album> getAlbumList() {
        return albumList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        AlbumViewHolder albumViewHolder;
        AlbumView albumView = new AlbumView(viewGroup.getContext());
        albumViewHolder = new AlbumViewHolder(albumView);
        return albumViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((AlbumViewHolder) viewHolder).getAlbumView().init(albumList.get(i));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }


    private class AlbumViewHolder extends RecyclerView.ViewHolder {

        private AlbumView albumView;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumView = (AlbumView) itemView;
        }

        public AlbumView getAlbumView() {
            return albumView;
        }

    }



}
