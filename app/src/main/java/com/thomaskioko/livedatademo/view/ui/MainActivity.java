package com.thomaskioko.livedatademo.view.ui;

import android.app.Activity;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.thomaskioko.livedatademo.R;
import com.thomaskioko.livedatademo.di.Injectable;
import com.thomaskioko.livedatademo.repository.api.MovieResult;
import com.thomaskioko.livedatademo.repository.model.ApiResponse;
import com.thomaskioko.livedatademo.repository.model.Movie;
import com.thomaskioko.livedatademo.view.adapter.MovieListAdapter;
import com.thomaskioko.livedatademo.viewmodel.MovieListViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import timber.log.Timber;

public class MainActivity extends LifecycleActivity implements Injectable, HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    RecyclerView mRecyclerView;
    MovieListAdapter mMovieListAdapter;
    private List<Movie> mMovieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AndroidInjection.inject(this);

        mRecyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mRecyclerView.getContext(), 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mMovieListAdapter = new MovieListAdapter(mMovieList);
        mRecyclerView.setAdapter(mMovieListAdapter);

        ProgressBar progressBar = findViewById(R.id.progressbar);

        progressBar.setVisibility(View.VISIBLE);
        ViewModelProviders.of(this, viewModelFactory)
                .get(MovieListViewModel.class)
                .getPopularMovies()
                .observe(this, apiResponse -> {
                    progressBar.setVisibility(View.GONE);
                    handleApiResponse(apiResponse);
                });
    }

    /**
     * Helper method that handles responses from, the API.It's responsible for displaying either
     * an error message of a list of movies based on the reponse from the server.
     *
     * @param apiResponse {@link ApiResponse}
     */
    private void handleApiResponse(ApiResponse apiResponse) {

        if (apiResponse.getStatusCode() != 200) {
            Timber.e("API Error: ");
        } else if (apiResponse.getError() != null) {
            Timber.e("Error: " + apiResponse.getError().getMessage());
        } else {
            MovieResult movieResult = apiResponse.getMovieResult();

            mMovieList.addAll(movieResult.getResults());
            mMovieListAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}