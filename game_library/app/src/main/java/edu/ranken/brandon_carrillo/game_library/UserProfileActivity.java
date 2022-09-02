package edu.ranken.brandon_carrillo.game_library;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.ui.user.UserProfileFragment;
import edu.ranken.brandon_carrillo.game_library.ui.user.UserProfileViewModel;

public class UserProfileActivity extends AppCompatActivity {
    // constants
    private static final String LOG_TAG = UserProfileActivity.class.getName();
    public static final String EXTRA_USER_ID = "userId";

    // views
    private FragmentContainerView fragmentContainer;
    private UserProfileFragment fragment;

    private String userId;

    // state
    private UserProfileViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        fragmentContainer = findViewById(R.id.fragmentContainer);
        fragment = new UserProfileFragment();
        model = new ViewModelProvider(this).get(UserProfileViewModel.class);

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();



        // get intent

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String intentAction = intent.getAction();
            Uri intentData = intent.getData();
            if (intentAction == null) {
                userId = intent.getStringExtra(EXTRA_USER_ID);
                model.fetchUserProfile(userId);
            } else if (Objects.equals(intentAction, Intent.ACTION_VIEW) && intentData != null) {
                handleWebLink(intent);
            }
        } else {
            Log.i(LOG_TAG, "userId: " + userId);
            userId = savedInstanceState.getString("userId");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("userId", userId);
    }

    private void handleWebLink(Intent intent) {
        Uri uri = intent.getData();
        String path = uri.getPath();
        String prefix = "/user/";

        // parse uri path
        if (path.startsWith(prefix)) {
            int userIdEnd = path.indexOf("/", prefix.length());

            if (userIdEnd < 0) {
                userId = path.substring(prefix.length());
            } else {
                userId = path.substring(prefix.length(), userIdEnd);
            }

        } else {
            userId = null;
        }

        // load user data
        model.fetchUserProfile(userId);
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getAction() != null) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(new Intent(this, LoginActivity.class));
            stackBuilder.addNextIntent(new Intent(this, HomeActivity.class));
            stackBuilder.startActivities();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // force up navigation to have the same behavior as back navigation
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}