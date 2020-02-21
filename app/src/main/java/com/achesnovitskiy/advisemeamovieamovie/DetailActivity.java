package com.achesnovitskiy.advisemeamovieamovie;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.achesnovitskiy.advisemeamovieamovie.adapters.ReviewAdapter;
import com.achesnovitskiy.advisemeamovieamovie.adapters.TrailerAdapter;
import com.achesnovitskiy.advisemeamovieamovie.data.FavouriteMovie;
import com.achesnovitskiy.advisemeamovieamovie.data.MainViewModel;
import com.achesnovitskiy.advisemeamovieamovie.data.Movie;
import com.achesnovitskiy.advisemeamovieamovie.data.Review;
import com.achesnovitskiy.advisemeamovieamovie.data.ToWatchMovie;
import com.achesnovitskiy.advisemeamovieamovie.data.Trailer;
import com.achesnovitskiy.advisemeamovieamovie.data.WatchedMovie;
import com.achesnovitskiy.advisemeamovieamovie.utils.JSONUtils;
import com.achesnovitskiy.advisemeamovieamovie.utils.NetworkUtils;
import com.android.mymovies.R;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageViewBigPoster;
    private ImageView imageViewAddToFavourite;
    private ImageView imageViewAddToToWatch;
    private ImageView imageViewAddToWatched;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewReleaseDate;
    private TextView textViewOverview;
    private TextView textViewAddToToWatch;
    private TextView textViewAddToWatched;
    private TextView textViewAddToFavourite;
    private ScrollView scrollViewInfo;

    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;

    private int id;
    private int activeButton;
    private final int ACTIVE_BUTTON_POPULAR_OR_TOP_RATED = 1;
    private final int ACTIVE_BUTTON_TO_WATCH = 2;
    private final int ACTIVE_BUTTON_WATCHED = 3;
    private final int ACTIVE_BUTTON_FAVOURITE= 4;
    private String lang;
    private Movie movie;
    private FavouriteMovie favouriteMovie;
    private ToWatchMovie toWatchMovie;
    private WatchedMovie watchedMovie;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        /* Initialization of variables */
        imageViewBigPoster = findViewById(R.id.imageViewBigPoster);
        imageViewAddToFavourite = findViewById(R.id.imageViewAddToFavourite);
        imageViewAddToToWatch = findViewById(R.id.imageViewAddToToWatch);
        imageViewAddToWatched = findViewById(R.id.imageViewAddToWatched);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewOriginalTitle = findViewById(R.id.textViewOriginalTitle);
        textViewRating = findViewById(R.id.textViewRating);
        textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        textViewOverview = findViewById(R.id.textViewOverview);
        scrollViewInfo = findViewById(R.id.scrollViewInfo);

        lang = Locale.getDefault().getLanguage(); // Set default language

        /* Receive intent and get movie id from it */
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id") && intent.hasExtra("active_button")) {
            id = intent.getIntExtra("id", -1);
            activeButton = intent.getIntExtra("active_button", -1);
        } else {
            finish();
        }

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class); // Get ViewModel instance
        switch (activeButton) {
            case ACTIVE_BUTTON_POPULAR_OR_TOP_RATED:
                movie = viewModel.getMovieById(id); // Get movie from ViewModel via id
                break;
            case ACTIVE_BUTTON_TO_WATCH:
                movie = viewModel.getToWatchMovieById(id); // Get movie from ViewModel via id
                break;
            case ACTIVE_BUTTON_WATCHED:
                movie = viewModel.getWatchedMovieById(id); // Get movie from ViewModel via id
                break;
            case ACTIVE_BUTTON_FAVOURITE:
                movie = viewModel.getFavouriteMovieById(id); // Get movie from ViewModel via id
                break;
        }

        /* Set values of views from movie data */
        Picasso.get().load(movie.getBigPosterPath()).placeholder(R.drawable.cadre).into(imageViewBigPoster);
        textViewTitle.setText(movie.getTitle());
        textViewOriginalTitle.setText(movie.getOriginalTitle());
        textViewRating.setText(Double.toString(movie.getVoteAverage()));
        textViewReleaseDate.setText(movie.getReleaseDate());
        textViewOverview.setText(movie.getOverview());

        setFavourite(); // Set "favourite" star
        setToWatch(); // Set "to watch" arrow
        setWatched(); // Set "watched" check

        /* Work with RecyclerView and MovieAdapter */
        trailerAdapter = new TrailerAdapter();
        reviewAdapter = new ReviewAdapter();
        recyclerViewTrailers = findViewById(R.id.recycleViewTrailers);
        recyclerViewReviews = findViewById(R.id.recycleViewReviews);
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setAdapter(trailerAdapter);
        recyclerViewReviews.setAdapter(reviewAdapter);

        /* Getting JSON objects and creating lists of trailers and reviews from it */
        JSONObject jsonObjectTrailer = NetworkUtils.getJSONForVideos(movie.getId(), lang);
        JSONObject jsonObjectReview = NetworkUtils.getJSONForReviews(movie.getId(), lang);
        ArrayList<Trailer> trailers = JSONUtils.getTrailersFromJSON(jsonObjectTrailer);
        ArrayList<Review> reviews = JSONUtils.getReviewsFromJSON(jsonObjectReview);

        /* Set movies and reviews lists in Adapters */
        trailerAdapter.setTrailers(trailers);
        reviewAdapter.setReviews(reviews);

        /* Working out clicking on the trailer */
        trailerAdapter.setOnTrailerClickListener(new TrailerAdapter.OnTrailerClickListener() {
            @Override
            public void onTrailerClick(String url) {
                Intent intentToTrailer = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intentToTrailer);
            }
        });

        scrollViewInfo.smoothScrollTo(0, 0); // Set the scrolled position of the view to the beginning
    }

    /* Work out clicking (from XML) on the "add to favourite" star */
    public void onClickChangeFavourite(View view) {
        if (favouriteMovie == null) {
            viewModel.insertFavouriteMovie(new FavouriteMovie(movie));
            Toast.makeText(this, R.string.add_to_favourite, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.deleteFavouriteMovie(favouriteMovie);
            Toast.makeText(this, R.string.remove_from_favourite, Toast.LENGTH_SHORT).show();
        }
        setFavourite();
    }

    /* Set state of "add to favourite" star */
    private void setFavourite() {
        favouriteMovie = viewModel.getFavouriteMovieById(id);
        if (favouriteMovie == null) {
            imageViewAddToFavourite.setImageResource(R.drawable.ic_favorite_gray_24dp);
        } else {
            imageViewAddToFavourite.setImageResource(R.drawable.ic_favorite_blue_24dp);
        }
    }

    /* Work out clicking (from XML) on the "add to to watch" arrow */
    public void onClickChangeToWatch(View view) {
        if (toWatchMovie == null) {
            viewModel.insertToWatchMovie(new ToWatchMovie(movie));
            Toast.makeText(this, R.string.add_to_to_watch, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.deleteToWatchMovie(toWatchMovie);
            Toast.makeText(this, R.string.remove_from_to_watch, Toast.LENGTH_SHORT).show();
        }
        setToWatch();
    }

    /* Set state of "add to to watch" arrow */
    private void setToWatch() {
        toWatchMovie = viewModel.getToWatchMovieById(id);
        if (toWatchMovie == null) {
            imageViewAddToToWatch.setImageResource(R.drawable.ic_play_arrow_gray_24dp);
        } else {
            imageViewAddToToWatch.setImageResource(R.drawable.ic_play_arrow_blue_24dp);
        }
    }

    /* Work out clicking (from XML) on the "watched" check */
    public void onClickChangeWatched(View view) {
        if (watchedMovie == null) {
            viewModel.insertWatchedMovie(new WatchedMovie(movie));
            Toast.makeText(this, R.string.add_to_watched, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.deleteWatchedMovie(watchedMovie);
            Toast.makeText(this, R.string.remove_from_watched, Toast.LENGTH_SHORT).show();
        }
        setWatched();
    }

    /* Set state of "watched" check */
    private void setWatched() {
        watchedMovie = viewModel.getWatchedMovieById(id);
        if (watchedMovie == null) {
            imageViewAddToWatched.setImageResource(R.drawable.ic_done_gray_24dp);
        } else {
            imageViewAddToWatched.setImageResource(R.drawable.ic_done_blue_24dp);
        }
    }

    /* Click "back" button*/
    public void onClickBack(View view) {
        finish();
    }
}