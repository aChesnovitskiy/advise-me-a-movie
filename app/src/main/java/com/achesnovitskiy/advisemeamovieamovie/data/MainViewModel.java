package com.achesnovitskiy.advisemeamovieamovie.data;

/* ViewModel stores UI-related data that isn't destroyed on app rotations */

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainViewModel extends AndroidViewModel {

    private static MovieDatabase database;

    /* LiveData list of movies and favourite movies */
    private LiveData<List<Movie>> movies;
    private LiveData<List<FavouriteMovie>> favouriteMovies;
    private LiveData<List<ToWatchMovie>> toWatchMovies;
    private LiveData<List<WatchedMovie>> watchedMovies;

    /* ViewModel constructor */
    public MainViewModel(@NonNull Application application) {
        super(application);
        database = MovieDatabase.getInstance(getApplication()); // Get database
        movies = database.movieDao().getAllMovies(); // Get lists of movies from database
        favouriteMovies = database.movieDao().getAllFavouriteMovies(); // Get lists of favourite movies from database
        toWatchMovies = database.movieDao().getAllToWatchMovies(); // Get lists of favourite movies from database
        watchedMovies = database.movieDao().getAllWatchedMovies(); // Get lists of favourite movies from database
    }

    /* Getters for LiveData movies lists */
    public LiveData<List<Movie>> getMovies() { return movies; }
    public LiveData<List<FavouriteMovie>> getFavouriteMovies() { return favouriteMovies; }
    public LiveData<List<ToWatchMovie>> getToWatchMovies() { return toWatchMovies; }
    public LiveData<List<WatchedMovie>> getWatchedMovies() { return watchedMovies; }

    /* Get one movie defined by id */
    public Movie getMovieById(int id) {
        try {
            return new GetMovieTask().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Get one favourite movie defined by id */
    public FavouriteMovie getFavouriteMovieById(int id) {
        try {
            return new GetFavouriteMovieTask().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Get one to watch movie defined by id */
    public ToWatchMovie getToWatchMovieById(int id) {
        try {
            return new GetToWatchMovieTask().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Get one watched movie defined by id */
    public WatchedMovie getWatchedMovieById(int id) {
        try {
            return new GetWatchedMovieTask().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Delete all movies */
    public void deleteAllMovies() { new DeleteMoviesTask().execute(); }

    /* Insert one certain movie */
    public void insertMovie(Movie movie) { new InsertMovieTask().execute(movie); }
    public void insertFavouriteMovie(FavouriteMovie movie) { new InsertFavouriteMovieTask().execute(movie); }
    public void insertToWatchMovie(ToWatchMovie movie) { new InsertToWatchMovieTask().execute(movie); }
    public void insertWatchedMovie(WatchedMovie movie) { new InsertWatchedMovieTask().execute(movie); }

    /* Delete one certain movie */
    public void deleteMovie(Movie movie) { new DeleteMovieTask().execute(movie); }
    public void deleteFavouriteMovie(FavouriteMovie movie) { new DeleteFavouriteMovieTask().execute(movie); }
    public void deleteToWatchMovie(ToWatchMovie movie) { new DeleteToWatchMovieTask().execute(movie); }
    public void deleteWatchedMovie(WatchedMovie movie) { new DeleteWatchedMovieTask().execute(movie); }

    /* AsyncTask for getting one movie defined by id */
    private static class GetMovieTask extends AsyncTask<Integer, Void, Movie> {

        @Override
        protected Movie doInBackground(Integer... integers) {
            if (integers != null && integers.length > 0) {
                return database.movieDao().getMovieById(integers[0]);
            }
            return null;
        }
    }

    /* AsyncTask for getting one favourite movie defined by id */
    private static class GetFavouriteMovieTask extends AsyncTask<Integer, Void, FavouriteMovie> {

        @Override
        protected FavouriteMovie doInBackground(Integer... integers) {
            if (integers != null && integers.length > 0) {
                return database.movieDao().getFavouriteMovieById(integers[0]);
            }
            return null;
        }
    }

    /* AsyncTask for getting one to "watch" movie defined by id */
    private static class GetToWatchMovieTask extends AsyncTask<Integer, Void, ToWatchMovie> {

        @Override
        protected ToWatchMovie doInBackground(Integer... integers) {
            if (integers != null && integers.length > 0) {
                return database.movieDao().getToWatchMovieById(integers[0]);
            }
            return null;
        }
    }

    /* AsyncTask for getting one watched movie defined by id */
    private static class GetWatchedMovieTask extends AsyncTask<Integer, Void, WatchedMovie> {

        @Override
        protected WatchedMovie doInBackground(Integer... integers) {
            if (integers != null && integers.length > 0) {
                return database.movieDao().getWatchedMovieById(integers[0]);
            }
            return null;
        }
    }

    /* AsyncTask for deleting all movies */
    private static class DeleteMoviesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            database.movieDao().deleteAllMovies();
            return null;
        }
    }

    /* AsyncTasks for inserting one certain movie */
    private static class InsertMovieTask extends AsyncTask<Movie, Void, Void> {

        @Override
        protected Void doInBackground(Movie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().insertMovie(movies[0]);
            }
            return null;
        }
    }

    /* AsyncTasks for inserting one certain favourite movie */
    private static class InsertFavouriteMovieTask extends AsyncTask<FavouriteMovie, Void, Void> {

        @Override
        protected Void doInBackground(FavouriteMovie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().insertFavouriteMovie(movies[0]);
            }
            return null;
        }
    }

    /* AsyncTasks for inserting one certain "to watch" movie */
    private static class InsertToWatchMovieTask extends AsyncTask<ToWatchMovie, Void, Void> {

        @Override
        protected Void doInBackground(ToWatchMovie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().insertToWatchMovie(movies[0]);
            }
            return null;
        }
    }

    /* AsyncTasks for inserting one certain watched movie */
    private static class InsertWatchedMovieTask extends AsyncTask<WatchedMovie, Void, Void> {

        @Override
        protected Void doInBackground(WatchedMovie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().insertWatchedMovie(movies[0]);
            }
            return null;
        }
    }

    /* AsyncTasks for deleting one certain movie */
    private static class DeleteMovieTask extends AsyncTask<Movie, Void, Void> {

        @Override
        protected Void doInBackground(Movie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().deleteMovie(movies[0]);
            }
            return null;
        }
    }

    /* AsyncTasks for deleting one certain favourite movie */
    private static class DeleteFavouriteMovieTask extends AsyncTask<FavouriteMovie, Void, Void> {

        @Override
        protected Void doInBackground(FavouriteMovie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().deleteFavouriteMovie(movies[0]);
            }
            return null;
        }
    }

    /* AsyncTasks for deleting one certain "to watch" movie */
    private static class DeleteToWatchMovieTask extends AsyncTask<ToWatchMovie, Void, Void> {

        @Override
        protected Void doInBackground(ToWatchMovie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().deleteToWatchMovie(movies[0]);
            }
            return null;
        }
    }

    /* AsyncTasks for deleting one certain watched movie */
    private static class DeleteWatchedMovieTask extends AsyncTask<WatchedMovie, Void, Void> {

        @Override
        protected Void doInBackground(WatchedMovie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().deleteWatchedMovie(movies[0]);
            }
            return null;
        }
    }
}
