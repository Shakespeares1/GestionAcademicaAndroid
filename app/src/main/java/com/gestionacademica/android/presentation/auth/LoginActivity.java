package com.gestionacademica.android.presentation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.gestionacademica.android.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public final class LoginActivity extends AppCompatActivity {
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private ProgressBar progress;
    private TextView status;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bindViews();
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewModel.loginState().observe(this, state -> {
            if (state == null) return;
            boolean loading = state.getStatus() == AuthUiState.Status.LOADING;
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            loginButton.setEnabled(!loading);
            if (state.getStatus() == AuthUiState.Status.ERROR) {
                status.setText(state.getError().getMessage());
                status.setVisibility(View.VISIBLE);
            } else if (state.getStatus() == AuthUiState.Status.SUCCESS) {
                startActivity(new Intent(this, com.gestionacademica.android.presentation.home.HomeActivity.class));
                finish();
            }
        });

        loginButton.setOnClickListener(v -> submitLogin());
        findViewById(R.id.registerLink).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        findViewById(R.id.forgotLink).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void bindViews() {
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progress = findViewById(R.id.loginProgress);
        status = findViewById(R.id.loginStatus);
    }

    private void submitLogin() {
        clearMessages();
        String email = value(emailInput).trim();
        String password = value(passwordInput);
        boolean valid = true;
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
        }
        if (!valid) return;
        viewModel.login(email, password);
    }

    private void clearMessages() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
        status.setText("");
        status.setVisibility(View.GONE);
    }

    private static String value(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }
}