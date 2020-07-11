package com.github.ros.android.helloros;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.LinkedHashSet;
import java.util.Set;

public class LocationsFragment extends Fragment implements View.OnClickListener, OnDialogEventListener {
    private FloatingActionButton button;
    private LocationsAdapter locationsAdapter;
    private LocationsDialog locationsDialog;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        locationsDialog = new LocationsDialog(getContext());

        View view = inflater.inflate(R.layout.locations_fragment, container, false);
        button = view.findViewById(R.id.add_location);
        button.setOnClickListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        locationsAdapter = new LocationsAdapter(locationsDialog, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(locationsAdapter);
        return view;
    }

    @Override
    public void onPause(){
        super.onPause();
        locationsDialog.unregisterListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        locationsDialog.registerListener(this);
    }

    @Override
    public void onClick(View view) {
        locationsDialog.createEmptyDialog().show();
    }

    @Override
    public void onCreate(String[] params) {
        // Store new location.
        TargetLocation newLocation = new TargetLocation(params);
        String locationKey = getString(R.string.locations_name_suffix_key, params[0]);
        preferences.edit().putString(locationKey, newLocation.toString()).apply();

        // Update and store keyset.
        Set<String> keyset = preferences.getStringSet(getString(R.string.app_key_set),new LinkedHashSet<>());
        keyset.add(locationKey);
        preferences.edit().putStringSet(getString(R.string.app_key_set), keyset).apply();
        locationsAdapter.updateDataSet();
        locationsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDelete(String name) {
        // Remove location.
        String locationKey = getString(R.string.locations_name_suffix_key, name);
        preferences.edit().remove(locationKey).apply();

        // Update and store keyset.
        Set<String> keyset = preferences.getStringSet(getString(R.string.app_key_set),new LinkedHashSet<>());
        if(!keyset.contains(locationKey)) {
            throw new IllegalStateException("Cannot delet non-existing entry.");
        }
        keyset.remove(locationKey);
        preferences.edit().putStringSet(getString(R.string.app_key_set), keyset).apply();
        locationsAdapter.updateDataSet();
        locationsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEdit(String[] params) {
        // Update and store location.
        TargetLocation newLocation = new TargetLocation(params);
        preferences.edit().putString(getString(R.string.locations_name_suffix_key, params[0]),
                newLocation.toString()).apply();
        locationsAdapter.updateDataSet();
        locationsAdapter.notifyDataSetChanged();
    }
}
