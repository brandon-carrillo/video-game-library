package edu.ranken.brandon_carrillo.game_library.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameListAdapter;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameListFragment;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameListViewModel;

public class UserListFragment extends Fragment {
    // constants
    private static final String LOG_TAG = UserListFragment.class.getSimpleName();

    // views
    private TextView errorText;
    private RecyclerView recyclerView;

    // state
    private UserListViewModel model;
    private UserListAdapter adapter;

    public UserListFragment() {
        super(R.layout.user_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: implement fragment

        // find views
        errorText = view.findViewById(R.id.userListErrorText);
        recyclerView = view.findViewById(R.id.userList);

        // get activity
        FragmentActivity activity = getActivity();
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();

        // setup recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        // setup view model & adapter
        model = new ViewModelProvider(activity).get(UserListViewModel.class);
        adapter = new UserListAdapter(activity, model);
        recyclerView.setAdapter(adapter);

        // observe model
        model.getUsers().observe(lifecycleOwner, (users) -> {
            adapter.setItems(users);
        });

        model.getErrorMessage().observe(lifecycleOwner, (errorMessage) -> {
            errorText.setVisibility(errorMessage != null ? View.VISIBLE : View.GONE);
            if (errorMessage != null) {
                errorText.setText(errorMessage);
            }
        });
        model.getSnackbarMessage().observe(lifecycleOwner, (snackbarMessage) -> {
            if (snackbarMessage != null) {
                Snackbar.make(recyclerView, snackbarMessage, Snackbar.LENGTH_LONG).show();
                model.clearSnackbar();
            }
        });
    }
}