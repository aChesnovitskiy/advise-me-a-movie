package com.achesnovitskiy.advisemeamovieamovie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.android.mymovies.R;

import java.util.ArrayList;
import java.util.Calendar;

public class FilterActivity extends AppCompatActivity {

    Button buttonReset;
    Button buttonApply;
    Switch switchHideWatched;
    Spinner spinnerRatingMin;
    Spinner spinnerRatingMax;
    Spinner spinnerYearMin;
    Spinner spinnerYearMax;

    private static final int RATING_MIN_DEFAULT = 0;
    private static final int RATING_MAX_DEFAULT = 10;
    private static final int YEAR_MIN_DEFAULT = 1900;
    private static final int YEAR_MAX_DEFAULT = Calendar.getInstance().get(Calendar.YEAR);

    private static int ratingMin;
    private static int ratingMax;
    private static int yearMin;
    private static int yearMax;
    private static boolean isHideWatched;

    private static ArrayList<Integer> scrollRatingMin = new ArrayList<>();
    private static ArrayList<Integer> scrollRatingMax = new ArrayList<>();
    private static ArrayList<Integer> scrollYearMin = new ArrayList<>();
    private static ArrayList<Integer> scrollYearMax = new ArrayList<>();

    private static ArrayAdapter<Integer> adapterRatingMax;
    private static ArrayAdapter<Integer> adapterRatingMin;
    private static ArrayAdapter<Integer> adapterYearMin;
    private static ArrayAdapter<Integer> adapterYearMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        initializeViews();

        getIntentFromMainActivity();

        setScrollsAndAdaptersToSpinners();

        setListeners();
    }

    private void setListeners() {
        spinnerRatingMin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ratingMin = (Integer) spinnerRatingMin.getSelectedItem();
                setAdjacentSpinner(spinnerRatingMax, scrollRatingMax, adapterRatingMax, ratingMin, RATING_MAX_DEFAULT, ratingMax);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerRatingMax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ratingMax = (Integer) spinnerRatingMax.getSelectedItem();
                setAdjacentSpinner(spinnerRatingMin, scrollRatingMin, adapterRatingMin, RATING_MIN_DEFAULT, ratingMax, ratingMin);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerYearMin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                yearMin = (Integer) spinnerYearMin.getSelectedItem();
                setAdjacentSpinner(spinnerYearMax, scrollYearMax, adapterYearMax, yearMin, YEAR_MAX_DEFAULT, yearMax);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerYearMax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                yearMax = (Integer) spinnerYearMax.getSelectedItem();
                setAdjacentSpinner(spinnerYearMin, scrollYearMin, adapterYearMin, YEAR_MIN_DEFAULT, yearMax, yearMin);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* Listening of "isHideWatched" switch */
        switchHideWatched.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHideWatched = isChecked;
            }
        });
    }

    private void setAdjacentSpinner(Spinner adjacentSpinner, ArrayList<Integer> adjacentScroll, ArrayAdapter<Integer> adjacentAdapter,
                                    int adjacentScrollMin, int adjacentScrollMax, int adjacentSelectedItem) {
        setScrollItems(adjacentScroll, adjacentScrollMin, adjacentScrollMax);
        adjacentAdapter.notifyDataSetChanged();
        int selectPosition =  adjacentScroll.indexOf(adjacentSelectedItem);
        adjacentSpinner.setSelection(selectPosition);
    }

    private void initializeViews() {
        buttonReset = findViewById(R.id.buttonReset);
        buttonApply = findViewById(R.id.buttonApply);
        switchHideWatched = findViewById(R.id.switchHideWatched);
        spinnerRatingMin = findViewById(R.id.spinnerRatingMin);
        spinnerRatingMax = findViewById(R.id.spinnerRatingMax);
        spinnerYearMin = findViewById(R.id.spinnerYearMin);
        spinnerYearMax = findViewById(R.id.spinnerYearMax);
    }

    private void getIntentFromMainActivity() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ratingMin") && intent.hasExtra("ratingMax") && intent.hasExtra("yearMin")
                && intent.hasExtra("yearMax") && intent.hasExtra("isHideWatched")) {
            ratingMin = intent.getIntExtra("ratingMin", RATING_MIN_DEFAULT);
            ratingMax = intent.getIntExtra("ratingMax", RATING_MAX_DEFAULT);
            yearMin = intent.getIntExtra("yearMin", YEAR_MIN_DEFAULT);
            yearMax = intent.getIntExtra("yearMax", YEAR_MAX_DEFAULT);
            isHideWatched = intent.getBooleanExtra("isHideWatched", false);
            switchHideWatched.setChecked(isHideWatched);
        } else {
            finish();
        }
    }

    private void setScrollsAndAdaptersToSpinners() {
        setScrollItems(scrollRatingMin, RATING_MIN_DEFAULT, ratingMax);
        setScrollItems(scrollRatingMax, ratingMin, RATING_MAX_DEFAULT);
        setScrollItems(scrollYearMin, YEAR_MIN_DEFAULT, yearMax);
        setScrollItems(scrollYearMax, yearMin, YEAR_MAX_DEFAULT);

        int selectPosition =  scrollRatingMin.indexOf(ratingMin);
        adapterRatingMin = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scrollRatingMin);
        adapterRatingMin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRatingMin.setAdapter(adapterRatingMin);
        spinnerRatingMin.setSelection(selectPosition);

        adapterRatingMax = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scrollRatingMax);
        adapterRatingMax.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRatingMax.setAdapter(adapterRatingMax);
        spinnerRatingMax.setSelection(scrollRatingMax.size() - 1);

        selectPosition =  scrollYearMin.indexOf(yearMin);
        adapterYearMin = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scrollYearMin);
        adapterYearMin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYearMin.setAdapter(adapterYearMin);
        spinnerYearMin.setSelection(selectPosition);

        adapterYearMax = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scrollYearMax);
        adapterYearMax.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYearMax.setAdapter(adapterYearMax);
        spinnerYearMax.setSelection(scrollYearMax.size() - 1);
    }


    private void setScrollItems(ArrayList<Integer> scroll, int minItem, int maxItem) {
        scroll.clear();
        for (int i = minItem; i < maxItem + 1; i++) {
            scroll.add(i);
        }
    }

    public void onClickFilterButton(View view) {

        switch (view.getId()) {
            case R.id.buttonReset:
                switchHideWatched.setChecked(false);
                ratingMin = RATING_MIN_DEFAULT;
                ratingMax = RATING_MAX_DEFAULT;
                yearMin = YEAR_MIN_DEFAULT;
                yearMax = YEAR_MAX_DEFAULT;

                setScrollItems(scrollRatingMin, RATING_MIN_DEFAULT, ratingMax);
                setScrollItems(scrollRatingMax, ratingMin, RATING_MAX_DEFAULT);
                setScrollItems(scrollYearMin, YEAR_MIN_DEFAULT, yearMax);
                setScrollItems(scrollYearMax, yearMin, YEAR_MAX_DEFAULT);

                adapterRatingMin.notifyDataSetChanged();
                adapterRatingMax.notifyDataSetChanged();
                adapterYearMin.notifyDataSetChanged();
                adapterYearMax.notifyDataSetChanged();

                spinnerRatingMin.setSelection(0);
                spinnerRatingMax.setSelection(scrollRatingMax.size() - 1);
                spinnerYearMin.setSelection(0);
                spinnerYearMax.setSelection(scrollYearMax.size() - 1);
                break;

            case R.id.buttonApply:
                Intent intent = new Intent();
                intent.putExtra("ratingMin", ratingMin);
                intent.putExtra("ratingMax", ratingMax);
                intent.putExtra("yearMin", yearMin);
                intent.putExtra("yearMax", yearMax);
                intent.putExtra("isHideWatched", isHideWatched);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}
