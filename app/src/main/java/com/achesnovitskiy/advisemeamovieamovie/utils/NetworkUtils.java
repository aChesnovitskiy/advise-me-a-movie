package com.achesnovitskiy.advisemeamovieamovie.utils;

/* Utils for getting JSON objects from network (themoviedb.org) */

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class NetworkUtils {

    /* Base URLs */
    private static final String BASE_URL = "https://api.themoviedb.org/3/discover/movie"; // For the movie-JSON request
    private static final String BASE_URL_VIDEOS = "https://api.themoviedb.org/3/movie/%s/videos"; // For the videos-JSON request
    private static final String BASE_URL_REVIEWS = "https://api.themoviedb.org/3/movie/%s/reviews"; // For the reviews-JSON request

    /* JSON parameters */
    private static final String PARAMS_API_KEY = "api_key";
    private static final String PARAMS_LANGUAGE = "language";
    private static final String PARAMS_SORT_BY = "sort_by";
    private static final String PARAMS_PAGE = "page";
    private static final String PARAMS_VOTE_COUNT_MIN = "vote_count.gte";
    private static final String PARAMS_VOTE_AVERAGE_MIN = "vote_average.gte";
    private static final String PARAMS_VOTE_AVERAGE_MAX = "vote_average.lte";

    /* Parameters values */
    private static final String API_KEY = "0531babc65d8df55ede145d8029ed0ac";
    private static final String SORT_BY_POPULARITY = "popularity.desc";
    private static final String SORT_BY_TOP_RATED = "vote_average.desc";
    private static final Integer MIN_VOTE_COUNT = 200;

    /* Sorting method selection */
    public static final int POPULARITY = 0;
    public static final int TOP_RATED = 1;

    /* Build URL address for the movies-JSON */
    public static URL buildURL(int sortBy, int page, String lang, int ratingMin, int ratingMax) {
        URL result = null;
        String sortingMethod;
        if (sortBy == POPULARITY) {
            sortingMethod = SORT_BY_POPULARITY;
        } else {
            sortingMethod = SORT_BY_TOP_RATED;
        }
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAMS_API_KEY, API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE, lang)
                .appendQueryParameter(PARAMS_SORT_BY, sortingMethod)
                .appendQueryParameter(PARAMS_PAGE, Integer.toString(page))
                .appendQueryParameter(PARAMS_VOTE_COUNT_MIN, Integer.toString(MIN_VOTE_COUNT))
                .appendQueryParameter(PARAMS_VOTE_AVERAGE_MIN, Integer.toString(ratingMin))
                .appendQueryParameter(PARAMS_VOTE_AVERAGE_MAX, Integer.toString(ratingMax))
                .build();
        try {
            result = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /* Build URL address for the videos-JSON */
    private static URL buildURLToVideos(int id, String lang) {
        Uri uri = Uri.parse(String.format(BASE_URL_VIDEOS, id)).buildUpon()
                .appendQueryParameter(PARAMS_API_KEY, API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE, lang)
                .build();
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Build URL address for the reviews-JSON */
    private static URL buildURLToReviews(int id, String lang) {
        Uri uri = Uri.parse(String.format(BASE_URL_REVIEWS, id)).buildUpon()
                .appendQueryParameter(PARAMS_API_KEY, API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE, lang)
                .build();
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Get movies-JSON object from URL */
    public static JSONObject getJSONFromNetwork (int sortBy, int page, String lang, int ratingMin, int ratingMax) {
        JSONObject result = null;
        URL url = buildURL(sortBy, page, lang, ratingMin, ratingMax);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /* Get videos-JSON object from URL */
    public static JSONObject getJSONForVideos (int id, String lang) {
        JSONObject result = null;
        URL url = buildURLToVideos(id, lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /* Get reviews-JSON object from URL */
    public static JSONObject getJSONForReviews(int id, String lang) {
        JSONObject result = null;
        URL url = buildURLToReviews(id, lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /* AsyncTaskLoader for getting JSON object (better than AsyncTask) */
    public static class JSONLoader extends AsyncTaskLoader<JSONObject> {
        Bundle bundle; // Bundle with String URL

        public JSONLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            this.bundle = bundle;
        }

        /* Set listener for start loading */
        private OnStartLoadingListener onStartLoadingListener;
        public interface OnStartLoadingListener {
            void onStartLoading();
        }
        public void setOnStartLoadingListener(OnStartLoadingListener onStartLoadingListener) {
            this.onStartLoadingListener = onStartLoadingListener;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if (onStartLoadingListener != null) {
                onStartLoadingListener.onStartLoading();
            }
            forceLoad(); // Force an asynchronous load
        }

        @Nullable
        @Override
        public JSONObject loadInBackground() {
            if (bundle == null) {
                return null;
            }
            String urlAsString = bundle.getString("url");
            URL url = null;
            try {
                url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            JSONObject result = null;
            if (url == null) {
                return null;
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                StringBuilder builder = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = reader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }

    // AsyncTask for getting JSON object
    private static class JSONLoadTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... urls) {
            JSONObject result = null;
            if (urls == null || urls.length == 0) {
                return result;
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) urls[0].openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                StringBuilder builder = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = reader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }

}
