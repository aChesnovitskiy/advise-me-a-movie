package com.achesnovitskiy.advisemeamovieamovie.data;

/* Contains the database holder and serves as the main access point for the underlying connection to your app's persisted, relational data */

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/* ATTENTION! Using fallbackToDestructiveMigration().
* If database version will be changed after Google.Play release, change type of migration! */
@Database(entities = {Movie.class, FavouriteMovie.class, ToWatchMovie.class, WatchedMovie.class}, version = 4, exportSchema = false)
public abstract class MovieDatabase extends RoomDatabase {

    private static final String DB_NAME = "movies.db";

    private static final Object LOCK = new Object(); // Block key
    private static MovieDatabase database;

    /* Perform Singleton pattern with block */
    static MovieDatabase getInstance(Context context) {
        synchronized (LOCK) {
            if (database == null) {
                /* Create database */
                database = Room.databaseBuilder(context, MovieDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
            }
        }
        return database;
    }

    public abstract MovieDao movieDao();
}
