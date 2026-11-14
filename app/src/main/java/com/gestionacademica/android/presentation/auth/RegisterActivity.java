package com.gestionacademica.android.presentation.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.gestionacademica.android.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.nio.charset.StandardCharsets;

public final class RegisterActivity extends AppCompatActivity {
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmationLayout;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmationInput;
    private MaterialButton registerButton;
    private ProgressBar progress;
    private TextView status;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        bindViews();
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewModel.registrationState().observe(this, state -> {
            if (state == null) return;
            boolean loading = state.getStatus() == AuthUiState.Status.LOADING;
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            registerButton.setEnabled(!loading);
            if (state.getStatus() == AuthUiState.Status.ERROR) {
                showStatus(state.getError().getMessage());
            } else if (state.getStatus() == AuthUiState.Status.SUCCESS) {
                Toast.makeText(this, R.string.registration_success, Toast.LENGTH_LONG).show();
                finish();
            }
        });
        registerButton.setOnClickListener(v -> submit());
        findViewById(R.id.loginLink).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.registerEmailLayout);
        passwordLayout = findViewById(R.id.registerPasswordLayout);
        confirmationLayout = findViewById(R.id.confirmPasswordLayout);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmationInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        progress = findViewById(R.id.registerProgress);
        status = findViewById(R.id.registerStatus);
    }

    private void submit() {
        clearMessages();
        String name = value(nameInput).trim();
        String email = value(emailInput).trim();
        String password = value(passwordInput);
        String confirmation = value(confirmationInput);
        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.validation_name_required));
            valid = false;
        }
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.validation_email_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.validation_email_invalid));
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
        if (valid) viewModel.register(name, email, password);
    }

    private void clearMessages() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmationLayout.setError(null);
        status.setText("");
        status.setVisibility(View.GONE);
    }

    private void showStatus(String message) {
        status.setText(message);
        status.setVisibility(View.VISIBLE);
    }

    private static String value(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }
}