package com.achesnovitskiy.advisemeamovieamovie.adapters;

/* Adapter for binding poster image to RecycleView element */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.android.mymovies.R;
import com.achesnovitskiy.advisemeamovieamovie.data.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movies; // List of movies

    /* Tools for movies list */
    public MovieAdapter() {
        movies = new ArrayList<>();
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public void addMovies(List<Movie> movies) {
        this.movies.addAll(movies);
        notifyDataSetChanged();
    }

    public void addMovie(Movie movie) {
        this.movies.add(movie);
        notifyDataSetChanged();
    }

    public void clear() {
        this.movies.clear();
        notifyDataSetChanged();
    }

    /*  Set listeners:
        of clicking on the poster image
        and of reaching of end of movie list from one page */
    private OnPosterClickListener onPosterClickListener;
    private OnReachEndListener onReachEndListener;

    public interface OnPosterClickListener {
        void onPosterClick (int position);
    }

    public interface OnReachEndListener {
        void onReachEnd();
    }

    public void setOnPosterClickListener(OnPosterClickListener onPosterClickListener) {
        this.onPosterClickListener = onPosterClickListener;
    }
    public void setOnReachEndListener(OnReachEndListener onReachEndListener) {
        this.onReachEndListener = onReachEndListener;
    }

    @NonNull
    @Override // Create a new ViewHolder
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false); // Inflate view from XML resource
        return new MovieViewHolder(view);
    }

    @Override // Binding of data to the ViewHolder
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        /* Working out reaching the end of movies list from one page */
//        if (movies.size() >= 20 && position > movies.size() - 4 && onReachEndListener != null) {
        if (position > movies.size() - 4 && onReachEndListener != null) {
            onReachEndListener.onReachEnd();
        }

        Movie movie = movies.get(position);
        Picasso.get().load(movie.getPosterPath()).resize(200, 0).into(holder.imageViewSmallPoster);
        holder.textViewRatingOnTheStar.setText(String.valueOf(movie.getVoteAverage()));
    }

    @Override // The total number of items in this adapter
    public int getItemCount() {
        return movies.size();
    }

    /* ViewHolder - class, representing one element (item) of RecyclerView */
    class MovieViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewSmallPoster;
        private TextView textViewRatingOnTheStar;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSmallPoster = itemView.findViewById(R.id.imageViewSmallPoster);
            textViewRatingOnTheStar = itemView.findViewById(R.id.textViewRatingOnTheStar);

            /* Working out clicking the element */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onPosterClickListener != null) {
                        onPosterClickListener.onPosterClick(getAdapterPosition());
                    }
                }
            });
        }
    }
}
