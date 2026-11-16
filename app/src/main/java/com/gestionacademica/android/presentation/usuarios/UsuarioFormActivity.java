package com.gestionacademica.android.presentation.usuarios;

import android.content.Intent;
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
import com.gestionacademica.android.data.dto.ActualizarUsuarioRequest;
import com.gestionacademica.android.data.dto.RegistroUsuarioRequest;
import com.gestionacademica.android.data.dto.UsuarioDto;
import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.utils.ApiError;
import com.gestionacademica.android.utils.UsernameValidator;
import com.gestionacademica.android.presentation.auth.LoginActivity;
import com.gestionacademica.android.presentation.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class UsuarioFormActivity extends AppCompatActivity {
    private TextInputLayout usernameLayout;
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmationLayout;
    private TextInputEditText usernameInput;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmationInput;
    private TextView title;
    private TextView status;
    private ProgressBar progress;
    private MaterialButton saveButton;
    private MaterialButton retryButton;
    private UsuarioViewModel viewModel;
    private long userId = -1L;
    private boolean editing;
    private boolean detailLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireAdmin()) return;
        setContentView(R.layout.activity_usuario_form);
        bindViews();
        userId = getIntent().getLongExtra(UsuariosActivity.EXTRA_USER_ID, -1L);
        editing = userId > 0;
        title.setText(editing ? R.string.users_edit_title : R.string.users_create_title);

        viewModel = new ViewModelProvider(this).get(UsuarioViewModel.class);
        viewModel.detailState().observe(this, this::renderDetail);
        viewModel.saveState().observe(this, this::renderSave);
        retryButton.setOnClickListener(v -> {
            if (editing) viewModel.loadById(userId);
        });
        saveButton.setOnClickListener(v -> submit());

        if (editing) {
            passwordLayout.setVisibility(View.GONE);
            confirmationLayout.setVisibility(View.GONE);
            saveButton.setEnabled(false);
            viewModel.loadById(userId);
        } else {
            detailLoaded = true;
            saveButton.setEnabled(true);
            retryButton.setVisibility(View.GONE);
        }
    }

    private boolean requireAdmin() {
        Optional<Session> session = new SessionManager(getApplicationContext()).getSession();
        if (session.isPresent() && "ADMIN".equals(session.get().getRole())) return true;
        Toast.makeText(this, R.string.users_admin_only, Toast.LENGTH_LONG).show();
        Intent destination = new Intent(this,
                session.isPresent() ? HomeActivity.class : LoginActivity.class);
        destination.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(destination);
        finish();
        return false;
    }

    private void bindViews() {
        usernameLayout = findViewById(R.id.userUsernameLayout);
        nameLayout = findViewById(R.id.userNameLayout);
        emailLayout = findViewById(R.id.userEmailLayout);
        passwordLayout = findViewById(R.id.userPasswordLayout);
        confirmationLayout = findViewById(R.id.userConfirmPasswordLayout);
        usernameInput = findViewById(R.id.userUsernameInput);
        nameInput = findViewById(R.id.userNameInput);
        emailInput = findViewById(R.id.userEmailInput);
        passwordInput = findViewById(R.id.userPasswordInput);
        confirmationInput = findViewById(R.id.userConfirmPasswordInput);
        title = findViewById(R.id.userFormTitle);
        status = findViewById(R.id.userFormStatus);
        progress = findViewById(R.id.userFormProgress);
        saveButton = findViewById(R.id.userFormSaveButton);
        retryButton = findViewById(R.id.userFormRetry);
    }

    private void renderDetail(UsuarioUiState<UsuarioDto> state) {
        if (!editing || state == null) return;
        if (state.getStatus() == UsuarioUiState.Status.LOADING) {
            progress.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.GONE);
            saveButton.setEnabled(false);
        } else if (state.getStatus() == UsuarioUiState.Status.ERROR) {
            progress.setVisibility(View.GONE);
            status.setText(state.getError().getMessage());
            status.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
        } else if (state.getStatus() == UsuarioUiState.Status.SUCCESS) {
            progress.setVisibility(View.GONE);
            UsuarioDto user = state.getValue();
            if (user == null) {
                status.setText(R.string.users_detail_empty);
                status.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                return;
            }
            usernameInput.setText(user.username == null ? "" : user.username);
            nameInput.setText(user.nombreCompleto == null ? "" : user.nombreCompleto);
            emailInput.setText(user.email == null ? "" : user.email);
            detailLoaded = true;
            status.setVisibility(View.GONE);
            retryButton.setVisibility(View.GONE);
            saveButton.setEnabled(true);
        }
    }

    private void submit() {
        clearErrors();
        if (!detailLoaded) return;
        String username = UsernameValidator.normalize(value(usernameInput));
        String name = value(nameInput).trim();
        String email = value(emailInput).trim();
        boolean valid = true;
        if (username.isEmpty()) {
            usernameLayout.setError(getString(R.string.username_required));
            valid = false;
        } else if (!UsernameValidator.isValid(username)) {
            usernameLayout.setError(getString(R.string.username_invalid));
            valid = false;
        }
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.users_name_required));
            valid = false;
        }
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.validation_email_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.validation_email_invalid));
            valid = false;
        } else if (email.length() > 254) {
            emailLayout.setError(getString(R.string.users_email_too_long));
            valid = false;
        }

        String password = value(passwordInput);
        if (!editing) {
            String confirmation = value(confirmationInput);
            if (TextUtils.isEmpty(password)) {
                passwordLayout.setError(getString(R.string.users_password_required));
                valid = false;
            } else if (password.trim().isEmpty()) {
                passwordLayout.setError(getString(R.string.users_password_blank));
                valid = false;
            } else if (password.codePointCount(0, password.length()) < 12
                    || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                passwordLayout.setError(getString(R.string.users_password_policy));
                valid = false;
            }
            if (TextUtils.isEmpty(confirmation)) {
                confirmationLayout.setError(getString(R.string.users_password_confirmation_required));
                valid = false;
            } else if (!password.equals(confirmation)) {
                confirmationLayout.setError(getString(R.string.users_password_mismatch));
                valid = false;
            }
        }
        if (!valid) return;

        saveButton.setEnabled(false);
        if (editing) {
            viewModel.update(userId, new ActualizarUsuarioRequest(username, name, email));
        } else {
            viewModel.register(new RegistroUsuarioRequest(username, name, email, password));
        }
    }

    private void renderSave(UsuarioUiState<UsuarioDto> state) {
        if (state == null) return;
        boolean loading = state.getStatus() == UsuarioUiState.Status.LOADING;
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading && detailLoaded);
        if (state.getStatus() == UsuarioUiState.Status.ERROR) {
            status.setText(userWriteErrorMessage(state.getError()));
            status.setVisibility(View.VISIBLE);
        } else if (state.getStatus() == UsuarioUiState.Status.SUCCESS) {
            Toast.makeText(this, editing
                    ? R.string.users_updated : R.string.users_created, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    private void clearErrors() {
        usernameLayout.setError(null);
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmationLayout.setError(null);
        status.setText("");
        status.setVisibility(View.GONE);
    }

    private String userWriteErrorMessage(ApiError error) {
        if (error == null) return getString(R.string.username_server_rejected);
        if (error.getStatusCode() == 409) return getString(R.string.username_duplicate);
        if (error.getStatusCode() == 400) return getString(R.string.username_server_rejected);
        return error.getMessage();
    }

    private static String value(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}