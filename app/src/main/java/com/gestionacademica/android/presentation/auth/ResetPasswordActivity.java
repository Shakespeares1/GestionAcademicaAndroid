package com.gestionacademica.android.presentation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.gestionacademica.android.MainActivity;
import com.gestionacademica.android.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.nio.charset.StandardCharsets;

public final class ResetPasswordActivity extends AppCompatActivity {
    private TextInputLayout tokenLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmationLayout;
    private TextInputEditText tokenInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmationInput;
    private MaterialButton resetButton;
    private ProgressBar progress;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        tokenLayout = findViewById(R.id.resetTokenLayout);
        passwordLayout = findViewById(R.id.resetPasswordLayout);
        confirmationLayout = findViewById(R.id.resetConfirmLayout);
        tokenInput = findViewById(R.id.resetTokenInput);
        passwordInput = findViewById(R.id.resetPasswordInput);
        confirmationInput = findViewById(R.id.resetConfirmInput);
        resetButton = findViewById(R.id.resetButton);
        progress = findViewById(R.id.resetProgress);
        status = findViewById(R.id.resetStatus);

        AuthViewModel viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewModel.resetState().observe(this, state -> {
            if (state == null) return;
            boolean loading = state.getStatus() == AuthUiState.Status.LOADING;
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            resetButton.setEnabled(!loading);
            if (state.getStatus() == AuthUiState.Status.ERROR) {
                showStatus(state.getError().getMessage());
            } else if (state.getStatus() == AuthUiState.Status.SUCCESS) {
                Toast.makeText(this, R.string.reset_success, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        resetButton.setOnClickListener(v -> submit(viewModel));
        findViewById(R.id.resetBackLink).setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void submit(AuthViewModel viewModel) {
        tokenLayout.setError(null);
        passwordLayout.setError(null);
        confirmationLayout.setError(null);
        status.setText("");
        status.setVisibility(View.GONE);
        String token = value(tokenInput).trim();
        String password = value(passwordInput);
        String confirmation = value(confirmationInput);
        boolean valid = true;
        if (TextUtils.isEmpty(token)) {
            tokenLayout.setError(getString(R.string.validation_token_required));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.validation_password_required));
            valid = false;
        } else if (password.trim().isEmpty()) {
            passwordLayout.setError(getString(R.string.validation_password_blank));
            valid = false;
        } else if (password.length() < 12) {
            passwordLayout.setError(getString(R.string.validation_password_minimum));
            valid = false;
        } else if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            passwordLayout.setError(getString(R.string.validation_password_maximum));
            valid = false;
        }
        if (TextUtils.isEmpty(confirmation)) {
            confirmationLayout.setError(getString(R.string.validation_confirmation_required));
            valid = false;
        } else if (!password.equals(confirmation)) {
            confirmationLayout.setError(getString(R.string.validation_password_mismatch));
            valid = false;
        }
        if (valid) viewModel.resetPassword(token, password);
    }

    private void showStatus(String message) {
        status.setText(message);
        status.setVisibility(View.VISIBLE);
    }

    private static String value(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }
}