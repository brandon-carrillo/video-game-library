package edu.ranken.brandon_carrillo.game_library;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.ranken.brandon_carrillo.game_library.ui.game.GameDetailsFragment;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameDetailsViewModel;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameListViewModel;
import edu.ranken.brandon_carrillo.game_library.ui.home.HomePageAdapter;
import edu.ranken.brandon_carrillo.game_library.ui.user.UserListViewModel;
import edu.ranken.brandon_carrillo.game_library.ui.user.UserProfileFragment;
import edu.ranken.brandon_carrillo.game_library.ui.user.UserProfileViewModel;

public class HomeActivity extends AppCompatActivity {
    // constants
    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    // views
    private ViewPager2 pager;
    private BottomNavigationView bottomNav;
    private FragmentContainerView detailsContainer;

    // state
    private HomePageAdapter adapter;
    private GameListViewModel gameListViewModel;
    private GameDetailsViewModel gameDetailsViewModel;
    private UserListViewModel userListViewModel;
    private UserProfileViewModel userProfileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // find views
        pager = findViewById(R.id.homePager);
        bottomNav = findViewById(R.id.homeBottomNav);
        detailsContainer = findViewById(R.id.homeDetailsContainer);

        // create adapter
        adapter = new HomePageAdapter(this);
        pager.setAdapter(adapter);

        // register listener
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNav.getMenu().getItem(position).setChecked(true);
            }
        });
        bottomNav.setOnItemSelectedListener((MenuItem item) -> {
            int itemId = item.getItemId();
            if (itemId == R.id.actionGameList) {
                pager.setCurrentItem(0);
                if (detailsContainer != null) {
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.homeDetailsContainer, GameDetailsFragment.class, null)
                        .commit();
                }
                return true;
            } else if (itemId == R.id.actionUserList) {
                pager.setCurrentItem(1);
                if (detailsContainer != null) {
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.homeDetailsContainer, UserProfileFragment.class, null)
                        .commit();
                }
                return true;
            } else {
                return false;
            }
        });

        // get models
        gameListViewModel = new ViewModelProvider(this).get(GameListViewModel.class);
        gameDetailsViewModel = new ViewModelProvider(this).get(GameDetailsViewModel.class);
        userListViewModel = new ViewModelProvider(this).get(UserListViewModel.class);
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        // observe models
        gameListViewModel.getSelectedGame().observe(this, (game) -> {
            if (detailsContainer == null) {
                if (game != null) {
                    gameListViewModel.setSelectedGame(null);
                    Intent intent = new Intent(this, GameDetailsActivity.class);
                    intent.putExtra(GameDetailsActivity.EXTRA_GAME_ID, game.id);
                    this.startActivity(intent);
                }
            } else {
                if (game != null) {
                    Log.i(LOG_TAG, "show game details " + game.id);
                    gameDetailsViewModel.fetchGame(game.id);
                } else {
                    gameDetailsViewModel.fetchGame(null);
                }
            }
        });
        userListViewModel.getSelectedUser().observe(this, (user) -> {
            if (detailsContainer == null) {
                if (user != null) {
                    userListViewModel.setSelectedUser(null);
                    Intent intent = new Intent(this, UserProfileActivity.class);
                    intent.putExtra(UserProfileActivity.EXTRA_USER_ID, user.userId);
                    this.startActivity(intent);
                }
            } else {
                if (user != null) {
                    Log.i(LOG_TAG, "showing user profile " + user.userId);
                    userProfileViewModel.fetchUserProfile(user.userId);

                } else {
                    userProfileViewModel.fetchUserProfile(null);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // force up navigation to have the same behavior as back navigation
            onBackPressed();
            return true;
        } else if (itemId == R.id.actionUserProfile) {
            onUserProfile();
            return true;
        } else if (itemId == R.id.actionSignOut) {
            confirmSignOut();
//            showConfirmSignOutDialog();
//            onSignOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Log.i(LOG_TAG, "Back Pressed");
    }

    public void onUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // FIXME: check if user is null (not fixed)
        String userId = user.getUid();
        if (user != null) {
            Intent intent = new Intent(this, MyProfileActivity.class);
            intent.putExtra(MyProfileActivity.EXTRA_USER_ID, userId);
            this.startActivity(intent);
        }
    }

    private void confirmSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.signOut);
        builder.setMessage(R.string.sureSignOut);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> { onSignOut();});
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> { /* ... */ });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


//    private void showConfirmSignOutDialog() {
//        Context context = this;
//        ConfirmDialog dialog = new ConfirmDialog(
//            context,
//            "Are you sure that you want to sign out?",
//            (which) -> { onSignOut(); },
//            (which) -> {  }
//        );
//        dialog.show();
//    }


    public void onSignOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener((result) -> {
                Log.i(LOG_TAG, "Signed out.");
                finish();
            });
    }
}
