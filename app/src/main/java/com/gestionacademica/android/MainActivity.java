package com.gestionacademica.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.presentation.auth.LoginActivity;
import com.gestionacademica.android.presentation.home.HomeActivity;
import com.gestionacademica.android.utils.LocalNetworkAccess;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    private static final String PERMISSION_PREFS = "local_network_permission_state";
    private static final String KEY_REQUESTED_BEFORE = "requested_before";
    private static final String STATE_REQUEST_IN_FLIGHT = "permission_request_in_flight";
    private static final String STATE_SETTINGS_LAUNCHED = "settings_launched";
    private static final String STATE_DESTINATION_STARTED = "destination_started";

    private final ActivityResultLauncher<String> localNetworkPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                permissionRequestInFlight = false;
                if (granted && LocalNetworkAccess.isGranted(this, BuildConfig.API_BASE_URL)) {
                    getSharedPreferences(PERMISSION_PREFS, MODE_PRIVATE)
                            .edit().remove(KEY_REQUESTED_BEFORE).apply();
                    openDestination();
                } else {
                    showPermissionDeniedDialog(false);
                }
            });

    private boolean permissionRequestInFlight;
    private boolean settingsLaunched;
    private boolean destinationStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            permissionRequestInFlight = savedInstanceState.getBoolean(STATE_REQUEST_IN_FLIGHT);
            settingsLaunched = savedInstanceState.getBoolean(STATE_SETTINGS_LAUNCHED);
            destinationStarted = savedInstanceState.getBoolean(STATE_DESTINATION_STARTED);
        }

        if (!LocalNetworkAccess.isPermissionRequired(BuildConfig.API_BASE_URL)
                || LocalNetworkAccess.isGranted(this, BuildConfig.API_BASE_URL)) {
            openDestination();
            return;
        }

        if (permissionRequestInFlight || settingsLaunched) return;

        boolean requestedBefore = getSharedPreferences(PERMISSION_PREFS, MODE_PRIVATE)
                .getBoolean(KEY_REQUESTED_BEFORE, false);
        if (!requestedBefore) {
            requestLocalNetworkPermission();
        } else {
            showPermissionDeniedDialog(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (settingsLaunched
                && LocalNetworkAccess.isGranted(this, BuildConfig.API_BASE_URL)) {
            settingsLaunched = false;
            openDestination();
        } else if (settingsLaunched) {
            settingsLaunched = false;
            showPermissionDeniedDialog(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_REQUEST_IN_FLIGHT, permissionRequestInFlight);
        outState.putBoolean(STATE_SETTINGS_LAUNCHED, settingsLaunched);
        outState.putBoolean(STATE_DESTINATION_STARTED, destinationStarted);
        super.onSaveInstanceState(outState);
    }

    private void requestLocalNetworkPermission() {
        if (permissionRequestInFlight) return;
        if (LocalNetworkAccess.isGranted(this, BuildConfig.API_BASE_URL)) {
            openDestination();
            return;
        }
        getSharedPreferences(PERMISSION_PREFS, MODE_PRIVATE)
                .edit().putBoolean(KEY_REQUESTED_BEFORE, true).apply();
        permissionRequestInFlight = true;
        localNetworkPermissionLauncher.launch(LocalNetworkAccess.ACCESS_LOCAL_NETWORK);
    }

    private void showPermissionDeniedDialog(boolean returnedFromSettings) {
        if (isFinishing() || isDestroyed()) return;

        boolean rationale = shouldShowRequestPermissionRationale(
                LocalNetworkAccess.ACCESS_LOCAL_NETWORK);
        int message = returnedFromSettings
                ? R.string.local_network_permission_settings_return
                : rationale
                    ? R.string.local_network_permission_rationale
                    : R.string.local_network_permission_denied;
        int positiveLabel = rationale
                ? R.string.local_network_permission_retry
                : R.string.local_network_permission_settings;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.local_network_permission_title)
                .setMessage(message)
                .setNegativeButton(R.string.local_network_permission_close, (dialog, which) -> finish());

        if (rationale) {
            builder.setPositiveButton(positiveLabel, (dialog, which) ->
                    requestLocalNetworkPermission());
        } else {
            builder.setPositiveButton(positiveLabel, (dialog, which) -> openApplicationSettings());
        }
        builder.show();
    }

    private void openApplicationSettings() {
        settingsLaunched = true;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    private void openDestination() {
        if (destinationStarted || isFinishing()) return;
        destinationStarted = true;
        Optional<Session> session = new SessionManager(getApplicationContext()).getSession();
        Intent destination = session.isPresent()
                ? new Intent(this, HomeActivity.class)
                : new Intent(this, LoginActivity.class);
        startActivity(destination);
        finish();
    }
}
