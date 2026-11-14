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

public final class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private MaterialButton sendButton;
    private ProgressBar progress;
    private TextView status;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        emailLayout = findViewById(R.id.recoveryEmailLayout);
        emailInput = findViewById(R.id.recoveryEmailInput);
        sendButton = findViewById(R.id.recoveryButton);
        progress = findViewById(R.id.recoveryProgress);
        status = findViewById(R.id.recoveryStatus);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewModel.recoveryState().observe(this, state -> {
            if (state == null) return;
            boolean loading = state.getStatus() == AuthUiState.Status.LOADING;
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            sendButton.setEnabled(!loading);
            if (state.getStatus() == AuthUiState.Status.ERROR) {
                showStatus(state.getError().getMessage());
            } else if (state.getStatus() == AuthUiState.Status.SUCCESS) {
                String serverMessage = state.getValue() == null ? null : state.getValue().message;
                showStatus(TextUtils.isEmpty(serverMessage)
                        ? getString(R.string.recovery_generic_response) : serverMessage);
            }
        });
        sendButton.setOnClickListener(v -> submit());
        findViewById(R.id.resetWithTokenLink).setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class)));
        findViewById(R.id.backToLoginLink).setOnClickListener(v -> finish());
    }

    private void submit() {
        emailLayout.setError(null);
        status.setText("");
        status.setVisibility(View.GONE);
        String email = emailInput.getText() == null ? "" : emailInput.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.validation_email_required));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.validation_email_invalid));
        } else {
            viewModel.requestPasswordReset(email);
        }
    }

    private void showStatus(String message) {
        status.setText(message);
        status.setVisibility(View.VISIBLE);
    }
}