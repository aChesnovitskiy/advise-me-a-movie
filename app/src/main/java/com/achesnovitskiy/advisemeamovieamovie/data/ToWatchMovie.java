package com.achesnovitskiy.advisemeamovieamovie.data;

/*  Class containing information about "to watch" movie.
    Also it is Room Entity, that represents a table within the database */

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity (tableName = "to_watch_movies")
public class ToWatchMovie extends Movie {
    ToWatchMovie(int uniqueId, int id, int voteCount, String title, String originalTitle, String overview, String posterPath, String bigPosterPath, String backdropPath, double voteAverage, String releaseDate) {
        super(uniqueId, id, voteCount, title, originalTitle, overview, posterPath, bigPosterPath, backdropPath, voteAverage, releaseDate);
    }

    /* Constructor for downcast the Movie to the ToWatchMovie */
    @Ignore // Ignore, because Entity should have only one constructor for database
    public ToWatchMovie(Movie movie) {
        super(movie.getUniqueId(), movie.getId(), movie.getVoteCount(), movie.getTitle(), movie.getOriginalTitle(), movie.getOverview(), movie.getPosterPath(), movie.getBigPosterPath(), movie.getBackdropPath(), movie.getVoteAverage(), movie.getReleaseDate());
    }
}
