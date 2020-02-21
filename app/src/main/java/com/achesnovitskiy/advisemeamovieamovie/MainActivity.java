package com.achesnovitskiy.advisemeamovieamovie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.achesnovitskiy.advisemeamovieamovie.adapters.MovieAdapter;
import com.achesnovitskiy.advisemeamovieamovie.data.FavouriteMovie;
import com.achesnovitskiy.advisemeamovieamovie.data.MainViewModel;
import com.achesnovitskiy.advisemeamovieamovie.data.Movie;
import com.achesnovitskiy.advisemeamovieamovie.data.ToWatchMovie;
import com.achesnovitskiy.advisemeamovieamovie.data.WatchedMovie;
import com.achesnovitskiy.advisemeamovieamovie.utils.JSONUtils;
import com.achesnovitskiy.advisemeamovieamovie.utils.NetworkUtils;
import com.android.mymovies.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {

    private Button buttonPopular;
    private Button buttonTopRated;
    private Button buttonToWatch;
    private Button buttonWatched;
    private Button buttonFavourite;
    private ImageView popularIconBlue;
    private ImageView popularIconWhite;
    private ImageView topRatedIconBlue;
    private ImageView topRatedIconWhite;
    private ImageView toWatchIconBlue;
    private ImageView toWatchIconWhite;
    private ImageView watchedIconBlue;
    private ImageView watchedIconWhite;
    private ImageView favouriteIconBlue;
    private ImageView favouriteIconWhite;
    private ImageView circleFilterOn;
    private FloatingActionButton floatingActionButtonFilter;
    private TextView textViewNoMovies;
    private ProgressBar progressBarLoading;
    private RecyclerView recyclerViewPosters;

    private MovieAdapter movieAdapter;
    private MainViewModel viewModel;

    private static final int LOADER_ID = 226; // ID for the Loader
    private LoaderManager loaderManager;

    private static int page = 1;
    private static int sortMethod;
    private static int activeButton;
    private static final int ACTIVE_BUTTON_POPULAR_OR_TOP_RATED = 1;
    private static final int ACTIVE_BUTTON_TO_WATCH = 2;
    private static final int ACTIVE_BUTTON_WATCHED = 3;
    private static final int ACTIVE_BUTTON_FAVOURITE = 4;
    private static final int RATING_MIN_DEFAULT = 0;
    private static final int RATING_MAX_DEFAULT = 10;
    private static final int YEAR_MIN_DEFAULT = 1900;
    private static final int YEAR_MAX_DEFAULT = Calendar.getInstance().get(Calendar.YEAR);
    private static final int REQUEST_CODE_FILTER = 1;

    private static int ratingMin = RATING_MIN_DEFAULT;
    private static int ratingMax = RATING_MAX_DEFAULT;
    private static int yearMin = YEAR_MIN_DEFAULT;
    private static int yearMax = YEAR_MAX_DEFAULT;
    private static boolean isHideWatched = false;
    private static boolean isLoading = false;
    private static String lang;

    private List<Movie> favouriteMovies;
    private List<Movie> toWatchMovies;
    private List<Movie> watchedMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialization of View variables */
        buttonPopular = findViewById(R.id.buttonPopular);
        buttonTopRated = findViewById(R.id.buttonTopRated);
        buttonToWatch = findViewById(R.id.buttonToWatch);
        buttonWatched = findViewById(R.id.buttonWatched);
        buttonFavourite = findViewById(R.id.buttonFavourite);
        floatingActionButtonFilter = findViewById(R.id.floatingActionButtonFilter);
        textViewNoMovies = findViewById(R.id.textViewNoMovies);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        popularIconBlue = findViewById(R.id.popularIconBlue);
        popularIconWhite = findViewById(R.id.popularIconWhite);
        topRatedIconBlue = findViewById(R.id.topRatedIconBlue);
        topRatedIconWhite = findViewById(R.id.topRatedIconWhite);
        toWatchIconBlue = findViewById(R.id.toWatchIconBlue);
        toWatchIconWhite = findViewById(R.id.toWatchIconWhite);
        watchedIconBlue = findViewById(R.id.watchedIconBlue);
        watchedIconWhite = findViewById(R.id.watchedIconWhite);
        favouriteIconBlue = findViewById(R.id.favouriteIconBlue);
        favouriteIconWhite = findViewById(R.id.favouriteIconWhite);
        circleFilterOn = findViewById(R.id.circleFilterOn);

        lang = Locale.getDefault().getLanguage(); // Set default language

        loaderManager = LoaderManager.getInstance(this); // Gets a LoaderManager associated with the given owner

        favouriteMovies = new ArrayList<>(); // List of favourite movies
        toWatchMovies = new ArrayList<>(); // List of "to watch" movies
        watchedMovies = new ArrayList<>(); // List of watched movies

        /* Work with RecyclerView and MovieAdapter */
        movieAdapter = new MovieAdapter(); // Initialization of MovieAdapter
        recyclerViewPosters = findViewById(R.id.recycleViewPosters);
        recyclerViewPosters.setLayoutManager(new GridLayoutManager(this, getColumnCount())); // Set Layout to the RecycleView
        recyclerViewPosters.setAdapter(movieAdapter); // Set Adapter to the RecycleView

        movieAdapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override // Work out clicking the poster
            public void onPosterClick(int position) {
                Movie movie = movieAdapter.getMovies().get(position); // Get clicked movie from list of movies
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("id", movie.getId());
                intent.putExtra("active_button", activeButton);
                startActivity(intent);
            }
        });

        movieAdapter.setOnReachEndListener(new MovieAdapter.OnReachEndListener() {
            @Override // Work out reaching the end of movies list from one page
            public void onReachEnd() {
                if (!isLoading && activeButton == ACTIVE_BUTTON_POPULAR_OR_TOP_RATED) {
                    downloadData(sortMethod, page);
                }
            }
        });

        /* Work with ViewModel and LiveData */
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class); // Get ViewModel instance
        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies(); // Get LiveData list of movies from ViewModel
        LiveData<List<FavouriteMovie>> favouriteMoviesFromLiveData = viewModel.getFavouriteMovies(); // Get LiveData list of favourite movies from ViewModel
        LiveData<List<ToWatchMovie>> toWatchMoviesFromLiveData = viewModel.getToWatchMovies(); // Get LiveData list of "to watch" movies from ViewModel
        LiveData<List<WatchedMovie>> watchedMoviesFromLiveData = viewModel.getWatchedMovies(); // Get LiveData list of watched movies from ViewModel
        moviesFromLiveData.observe(this, new Observer<List<Movie>>() { // Subscribe on changes in the LiveData list of movies
            @Override // Do in the case of changes
            public void onChanged(List<Movie> movies) {
                if (page == 1) {
                    movieAdapter.setMovies(movies);
                }
            }
        });
        favouriteMoviesFromLiveData.observe(this, new Observer<List<FavouriteMovie>>() { // Subscribe on changes in the LiveData list of favourite movies
            @Override // Do in case of changes
            public void onChanged(List<FavouriteMovie> favouriteMoviesFromLiveData) {
                /* Update favouriteMovies */
                if (favouriteMoviesFromLiveData != null) {
                    favouriteMovies.clear();
                    favouriteMovies.addAll(favouriteMoviesFromLiveData);
                    if (activeButton == ACTIVE_BUTTON_FAVOURITE) {
                        setMyCollectionMovies(favouriteMovies);
                    }
                }
            }
        });
        toWatchMoviesFromLiveData.observe(this, new Observer<List<ToWatchMovie>>() { // Subscribe on changes in the LiveData list of "to watch" movies
            @Override // Do in case of changes
            public void onChanged(List<ToWatchMovie> toWatchMoviesFromLiveData) {
                /* Update toWatchMovies */
                if (toWatchMoviesFromLiveData != null) {
                    toWatchMovies.clear();
                    toWatchMovies.addAll(toWatchMoviesFromLiveData);
                    if (activeButton == ACTIVE_BUTTON_TO_WATCH) {
                        setMyCollectionMovies(toWatchMovies);
                    }
                }
            }
        });
        watchedMoviesFromLiveData.observe(this, new Observer<List<WatchedMovie>>() { // Subscribe on changes in the LiveData list of watched movies
            @Override // Do in case of changes
            public void onChanged(List<WatchedMovie> watchedMoviesFromLiveData) {
                /* Update watchedMovies */
                if (watchedMoviesFromLiveData != null) {
                    watchedMovies.clear();
                    watchedMovies.addAll(watchedMoviesFromLiveData);
                    if (activeButton == ACTIVE_BUTTON_WATCHED) {
                        setMyCollectionMovies(watchedMovies);
                    }
                }
            }
        });


        clickPopularOrTopRatedButtons(R.id.buttonPopular);
    }


    /* Go to the filter activity */
    public void onClickFilterButton(View view) {
        Intent intent = new Intent(this, FilterActivity.class);
        intent.putExtra("ratingMin", ratingMin);
        intent.putExtra("ratingMax", ratingMax);
        intent.putExtra("yearMin", yearMin);
        intent.putExtra("yearMax", yearMax);
        intent.putExtra("isHideWatched", isHideWatched);
        startActivityForResult(intent, REQUEST_CODE_FILTER);
    }

    /* Return from the filter activity */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_FILTER) {
                if (data != null && data.hasExtra("ratingMin") && data.hasExtra("ratingMax") && data.hasExtra("yearMin")
                        && data.hasExtra("yearMax") && data.hasExtra("isHideWatched")) {
                    ratingMin = data.getIntExtra("ratingMin", RATING_MIN_DEFAULT);
                    ratingMax = data.getIntExtra("ratingMax", RATING_MAX_DEFAULT);
                    yearMin = data.getIntExtra("yearMin", YEAR_MIN_DEFAULT);
                    yearMax = data.getIntExtra("yearMax", YEAR_MAX_DEFAULT);
                    isHideWatched = data.getBooleanExtra("isHideWatched", false);

                    if (ratingMin != RATING_MIN_DEFAULT || ratingMax != RATING_MAX_DEFAULT ||
                            yearMin != YEAR_MIN_DEFAULT || yearMax != YEAR_MAX_DEFAULT || isHideWatched) {
                        circleFilterOn.setVisibility(View.VISIBLE);
                    } else {
                        circleFilterOn.setVisibility(View.GONE);
                    }

                    switch (activeButton) {
                        case ACTIVE_BUTTON_POPULAR_OR_TOP_RATED:
                            page = 1;
                            downloadData(sortMethod, page);
                            break;
                        case ACTIVE_BUTTON_TO_WATCH:
                            setMyCollectionMovies(toWatchMovies);
                            break;
                        case ACTIVE_BUTTON_WATCHED:
                            setMyCollectionMovies(watchedMovies);
                            break;
                        case ACTIVE_BUTTON_FAVOURITE:
                            setMyCollectionMovies(favouriteMovies);
                            break;
                    }
                }
            }
        } else {
            Toast.makeText(this, "Wrong result", Toast.LENGTH_SHORT).show();
        }
    }

    /* Column count calculation */
    private int getColumnCount() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics); // Get display metrics that describe the size and density of this display
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return width / 185 > 2 ? width / 185 : 2;
    }

    /* Download movie data from network and set it */
    public void downloadData(int sortMethod, int page) {
        URL url = NetworkUtils.buildURL(sortMethod, page, lang, ratingMin, ratingMax);
        Bundle bundle = new Bundle();
        bundle.putString("url", url.toString());
        loaderManager.restartLoader(LOADER_ID, bundle, this);
    }

    /* Click on top buttons */
    public void onClickTopButtons(View view) {
        int buttonId = view.getId();
        switch (buttonId) {
            case R.id.buttonPopular:
                clickPopularOrTopRatedButtons(R.id.buttonPopular);
                break;
            case R.id.buttonTopRated:
                clickPopularOrTopRatedButtons(R.id.buttonTopRated);
                break;
            case R.id.buttonToWatch:
                clickToWatchButton();
                break;
            case R.id.buttonWatched:
                clickWatchedButton();
                break;
            case R.id.buttonFavourite:
                clickFavouriteButton();
                break;
        }
    }

    /* Click on "popular" or "top rated" button */
    private void clickPopularOrTopRatedButtons(int buttonId) {
        if (buttonId == R.id.buttonPopular) {
            Toast.makeText(this, getString(R.string.most_popular), Toast.LENGTH_SHORT).show();
            sortMethod = NetworkUtils.POPULARITY;
            setButtonsStyle(R.id.buttonPopular);
        } else {
            Toast.makeText(this, getString(R.string.top_rated), Toast.LENGTH_SHORT).show();
            sortMethod = NetworkUtils.TOP_RATED;
            setButtonsStyle(R.id.buttonTopRated);
        }
        page = 1;
        activeButton = ACTIVE_BUTTON_POPULAR_OR_TOP_RATED;
        downloadData(sortMethod, page);
    }

    /* Click on "to watch" button */
    private void clickToWatchButton() {
        Toast.makeText(this, getString(R.string.movies_to_watch), Toast.LENGTH_SHORT).show();
        setMyCollectionMovies(toWatchMovies);
        activeButton = ACTIVE_BUTTON_TO_WATCH;
        setButtonsStyle(R.id.buttonToWatch);
    }

    /* Click on "watched" button */
    private void clickWatchedButton() {
        Toast.makeText(this, getString(R.string.watched_movies), Toast.LENGTH_SHORT).show();
        setMyCollectionMovies(watchedMovies);
        activeButton = ACTIVE_BUTTON_WATCHED;
        setButtonsStyle(R.id.buttonWatched);
    }

    /* Click on "favourite" button */
    private void clickFavouriteButton() {
        Toast.makeText(this, getString(R.string.favourite_movies), Toast.LENGTH_SHORT).show();
        setMyCollectionMovies(favouriteMovies);
        activeButton = ACTIVE_BUTTON_FAVOURITE;
        setButtonsStyle(R.id.buttonFavourite);
    }

    /* Set movies in user's collections */
    private void setMyCollectionMovies(List<Movie> movies) {
        movieAdapter.clear();
        for (Movie movie : movies) {
            if (filter(movie, ratingMin, ratingMax, yearMin, yearMax, isHideWatched, watchedMovies)) {
                movieAdapter.addMovie(movie);
            }
        }
        if (movieAdapter.getMovies().size() == 0) {
            textViewNoMovies.setVisibility(View.VISIBLE);
        } else {
            textViewNoMovies.setVisibility(View.GONE);
        }
    }

    /* Filter */
    private boolean filter(Movie movie, int ratingMin, int ratingMax, int yearMin, int yearMax, boolean isHideWatched, List<Movie> watchedMovies) {
        boolean isWatched = false;
        if (isHideWatched && watchedMovies != null) {
            int movieId = movie.getId();
            for (Movie movieForId : watchedMovies) {
                if (movieForId.getId() == movieId) {
                    isWatched = true;
                    break;
                }
            }
        }
        double rating = movie.getVoteAverage();
        String releaseDate = movie.getReleaseDate();
        int year = Integer.parseInt(releaseDate.substring(0, 4));
        return rating >= ratingMin && rating <= ratingMax && year >= yearMin && year <= yearMax && !isWatched;
    }

    /* Set style of buttons */
    private void setButtonsStyle(int buttonId) {
        switch (buttonId) {
            case R.id.buttonPopular:
                setButtonPopular(true);
                setButtonTopRated(false);
                setButtonToWatch(false);
                setButtonWatched(false);
                setButtonFavourite(false);
                break;
            case R.id.buttonTopRated:
                setButtonPopular(false);
                setButtonTopRated(true);
                setButtonToWatch(false);
                setButtonWatched(false);
                setButtonFavourite(false);
                break;
            case R.id.buttonToWatch:
                setButtonPopular(false);
                setButtonTopRated(false);
                setButtonToWatch(true);
                setButtonWatched(false);
                setButtonFavourite(false);
                break;
            case R.id.buttonWatched:
                setButtonPopular(false);
                setButtonTopRated(false);
                setButtonToWatch(false);
                setButtonWatched(true);
                setButtonFavourite(false);
                break;
            case R.id.buttonFavourite:
                setButtonPopular(false);
                setButtonTopRated(false);
                setButtonToWatch(false);
                setButtonWatched(false);
                setButtonFavourite(true);
                break;
        }
    }

    private void setButtonPopular(boolean isActive) {
        int blue = getResources().getColor(R.color.blue_color);
        int white = getResources().getColor(R.color.white_color);

        if (isActive) {
            buttonPopular.setBackgroundColor(blue);
            popularIconBlue.setVisibility(View.GONE);
            popularIconWhite.setVisibility(View.VISIBLE);
        } else {
            buttonPopular.setBackgroundColor(white);
            popularIconBlue.setVisibility(View.VISIBLE);
            popularIconWhite.setVisibility(View.GONE);
        }
    }

    private void setButtonTopRated(boolean isActive) {
        int blue = getResources().getColor(R.color.blue_color);
        int white = getResources().getColor(R.color.white_color);

        if (isActive) {
            buttonTopRated.setBackgroundColor(blue);
            topRatedIconBlue.setVisibility(View.GONE);
            topRatedIconWhite.setVisibility(View.VISIBLE);
        } else {
            buttonTopRated.setBackgroundColor(white);
            topRatedIconBlue.setVisibility(View.VISIBLE);
            topRatedIconWhite.setVisibility(View.GONE);
        }
    }

    private void setButtonToWatch(boolean isActive) {
        int blue = getResources().getColor(R.color.blue_color);
        int white = getResources().getColor(R.color.white_color);

        if (isActive) {
            buttonToWatch.setBackgroundColor(blue);
            toWatchIconBlue.setVisibility(View.GONE);
            toWatchIconWhite.setVisibility(View.VISIBLE);
        } else {
            buttonToWatch.setBackgroundColor(white);
            toWatchIconBlue.setVisibility(View.VISIBLE);
            toWatchIconWhite.setVisibility(View.GONE);
        }
    }

    private void setButtonWatched(boolean isActive) {
        int blue = getResources().getColor(R.color.blue_color);
        int white = getResources().getColor(R.color.white_color);

        if (isActive) {
            buttonWatched.setBackgroundColor(blue);
            watchedIconBlue.setVisibility(View.GONE);
            watchedIconWhite.setVisibility(View.VISIBLE);
        } else {
            buttonWatched.setBackgroundColor(white);
            watchedIconBlue.setVisibility(View.VISIBLE);
            watchedIconWhite.setVisibility(View.GONE);
        }
    }

    private void setButtonFavourite(boolean isActive) {
        int blue = getResources().getColor(R.color.blue_color);
        int white = getResources().getColor(R.color.white_color);

        if (isActive) {
            buttonFavourite.setBackgroundColor(blue);
            favouriteIconBlue.setVisibility(View.GONE);
            favouriteIconWhite.setVisibility(View.VISIBLE);
        } else {
            buttonFavourite.setBackgroundColor(white);
            favouriteIconBlue.setVisibility(View.VISIBLE);
            favouriteIconWhite.setVisibility(View.GONE);
        }
    }

    /* Override Loader methods for loading movies from URL */
    @NonNull
    @Override // Instantiate and return a new Loader for the given ID
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, args);
        jsonLoader.setOnStartLoadingListener(new NetworkUtils.JSONLoader.OnStartLoadingListener() {
            @Override
            public void onStartLoading() {
                progressBarLoading.setVisibility(View.VISIBLE);
                isLoading = true;
            }
        });
        return jsonLoader;
    }

    @Override // Called when a previously created loader has finished its load
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        ArrayList<Movie> movies = JSONUtils.getMoviesFromJSON(data); // Creating list of movies from JSON Object, received from "loadInBackground"
        if (movies != null && !movies.isEmpty()) {
            if (page == 1 && activeButton == ACTIVE_BUTTON_POPULAR_OR_TOP_RATED) {
                viewModel.deleteAllMovies();
                movieAdapter.clear();
            }
            for (Movie movie : movies) {
                if (filter(movie, ratingMin, ratingMax, yearMin, yearMax, isHideWatched, watchedMovies) && activeButton == ACTIVE_BUTTON_POPULAR_OR_TOP_RATED) {
                    viewModel.insertMovie(movie);
                    movieAdapter.addMovie(movie);
                }
            }
//            movieAdapter.addMovies(movies); // Add movies list to the Adapter
            page++;
        }


        progressBarLoading.setVisibility(View.INVISIBLE);
        isLoading = false;

        if (movieAdapter.getMovies().isEmpty()) {
            textViewNoMovies.setVisibility(View.VISIBLE);
        } else {
            textViewNoMovies.setVisibility(View.GONE);
        }

        loaderManager.destroyLoader(LOADER_ID); // Stops and removes the loader with the given ID
    }

    @Override
    // Called when a previously created loader is being reset, and thus making its data unavailable
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }
}

