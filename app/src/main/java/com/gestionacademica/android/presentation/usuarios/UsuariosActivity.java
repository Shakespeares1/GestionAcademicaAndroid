package com.gestionacademica.android.presentation.usuarios;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.gestionacademica.android.R;
import com.gestionacademica.android.data.dto.ActualizarAccesoUsuarioRequest;
import com.gestionacademica.android.data.dto.UsuarioDto;
import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.presentation.auth.LoginActivity;
import com.gestionacademica.android.presentation.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class UsuariosActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID =
            "com.gestionacademica.android.extra.USER_ID";

    private SessionManager sessionManager;
    private UsuarioViewModel viewModel;
    private List<UsuarioDto> users = new ArrayList<>();
    private TextInputEditText searchInput;
    private LinearLayout userList;
    private ProgressBar progress;
    private TextView status;
    private TextView empty;
    private MaterialButton retry;
    private long requestedDetailId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getApplicationContext());
        if (!requireAdmin()) return;
        setContentView(R.layout.activity_usuarios);

        searchInput = findViewById(R.id.usersSearch);
        userList = findViewById(R.id.usersList);
        progress = findViewById(R.id.usersProgress);
        status = findViewById(R.id.usersStatus);
        empty = findViewById(R.id.usersEmpty);
        retry = findViewById(R.id.usersRetry);
        findViewById(R.id.addUserButton).setOnClickListener(v ->
                startActivity(new Intent(this, UsuarioFormActivity.class)));
        retry.setOnClickListener(v -> viewModel.loadAll());

        viewModel = new ViewModelProvider(this).get(UsuarioViewModel.class);
        viewModel.listState().observe(this, this::renderListState);
        viewModel.detailState().observe(this, this::renderDetailState);
        viewModel.accessState().observe(this, this::renderAccessState);
        viewModel.deleteState().observe(this, this::renderDeleteState);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderUsers();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel == null) return;
        if (!requireAdmin()) return;
        viewModel.loadAll();
    }

    private boolean requireAdmin() {
        Optional<Session> session = sessionManager.getSession();
        if (!session.isPresent()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return false;
        }
        if (!"ADMIN".equals(session.get().getRole())) {
            Toast.makeText(this, R.string.users_admin_only, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return false;
        }
        return true;
    }

    private void renderListState(UsuarioUiState<List<UsuarioDto>> state) {
        if (state == null) return;
        boolean loading = state.getStatus() == UsuarioUiState.Status.LOADING;
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        retry.setVisibility(View.GONE);
        if (state.getStatus() == UsuarioUiState.Status.SUCCESS) {
            users = state.getValue() == null ? new ArrayList<>() : state.getValue();
            status.setVisibility(View.GONE);
            renderUsers();
        } else if (state.getStatus() == UsuarioUiState.Status.ERROR) {
            status.setText(state.getError().getMessage());
            status.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        }
    }

    private void renderUsers() {
        userList.removeAllViews();
        String query = searchInput.getText() == null
                ? "" : searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        LayoutInflater inflater = LayoutInflater.from(this);
        int shown = 0;
        for (UsuarioDto user : users) {
            if (user == null || !matches(user, query)) continue;
            View card = inflater.inflate(R.layout.item_usuario, userList, false);
            TextView name = card.findViewById(R.id.userItemName);
            TextView username = card.findViewById(R.id.userItemUsername);
            TextView email = card.findViewById(R.id.userItemEmail);
            TextView access = card.findViewById(R.id.userItemAccess);
            name.setText(safe(user.nombreCompleto));
            username.setText(getString(R.string.users_username_display, safe(user.username)));
            email.setText(safe(user.email));
            access.setText(getString(R.string.users_access_summary,
                    safe(user.role), safe(user.accountStatus)));
            card.findViewById(R.id.userDetailsButton).setOnClickListener(v -> loadDetails(user.id));
            card.findViewById(R.id.userEditButton).setOnClickListener(v -> edit(user.id));
            card.findViewById(R.id.userAccessButton).setOnClickListener(v -> showAccessDialog(user));
            card.findViewById(R.id.userDeleteButton).setOnClickListener(v -> confirmDelete(user));
            userList.addView(card);
            shown++;
        }
        if (shown == 0) {
            empty.setText(users.isEmpty() ? R.string.users_empty : R.string.users_no_matches);
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    private boolean matches(UsuarioDto user, String query) {
        if (query.isEmpty()) return true;
        return safe(user.nombreCompleto).toLowerCase(Locale.ROOT).contains(query)
                || safe(user.username).toLowerCase(Locale.ROOT).contains(query)
                || safe(user.email).toLowerCase(Locale.ROOT).contains(query);
    }

    private void loadDetails(Long id) {
        if (!validId(id)) {
            Toast.makeText(this, R.string.users_invalid_id, Toast.LENGTH_SHORT).show();
            return;
        }
        requestedDetailId = id;
        viewModel.loadById(id);
    }

    private void renderDetailState(UsuarioUiState<UsuarioDto> state) {
        if (state == null || state.getStatus() == UsuarioUiState.Status.IDLE) return;
        if (state.getStatus() == UsuarioUiState.Status.LOADING) {
            progress.setVisibility(View.VISIBLE);
            return;
        }
        progress.setVisibility(View.GONE);
        if (state.getStatus() == UsuarioUiState.Status.ERROR) {
            Toast.makeText(this, state.getError().getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        UsuarioDto user = state.getValue();
        if (user == null || user.id == null || user.id != requestedDetailId) return;
        String detail = getString(R.string.users_detail_text,
                safe(user.id.toString()), safe(user.username), safe(user.nombreCompleto), safe(user.email),
                safe(user.role), safe(user.accountStatus), safe(user.fechaCreacion));
        new AlertDialog.Builder(this)
                .setTitle(R.string.users_details_title)
                .setMessage(detail)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void edit(Long id) {
        if (!validId(id)) {
            Toast.makeText(this, R.string.users_invalid_id, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, UsuarioFormActivity.class);
        intent.putExtra(EXTRA_USER_ID, id);
        startActivity(intent);
    }

    private void showAccessDialog(UsuarioDto user) {
        if (!validId(user.id)) return;
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_usuario_access, null);
        AutoCompleteTextView role = content.findViewById(R.id.userRoleDropdown);
        AutoCompleteTextView accountStatus = content.findViewById(R.id.userStatusDropdown);
        String[] roles = getResources().getStringArray(R.array.user_roles);
        String[] statuses = getResources().getStringArray(R.array.account_statuses);
        role.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roles));
        accountStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, statuses));
        role.setText(safe(user.role), false);
        accountStatus.setText(safe(user.accountStatus), false);

        new AlertDialog.Builder(this)
                .setTitle(R.string.users_access_dialog_title)
                .setView(content)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.users_access_save, (dialog, which) -> {
                    String selectedRole = role.getText().toString().trim();
                    String selectedStatus = accountStatus.getText().toString().trim();
                    if (!contains(roles, selectedRole) || !contains(statuses, selectedStatus)) {
                        Toast.makeText(this, R.string.users_access_values_required,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    viewModel.updateAccess(user.id,
                            new ActualizarAccesoUsuarioRequest(selectedRole, selectedStatus));
                })
                .show();
    }

    private void confirmDelete(UsuarioDto user) {
        if (!validId(user.id)) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.users_delete_title)
                .setMessage(getString(R.string.users_delete_message, safe(user.nombreCompleto)))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.users_delete, (dialog, which) -> viewModel.delete(user.id))
                .show();
    }

    private void renderAccessState(UsuarioUiState<UsuarioDto> state) {
        if (state == null) return;
        if (state.getStatus() == UsuarioUiState.Status.LOADING) {
            progress.setVisibility(View.VISIBLE);
        } else if (state.getStatus() == UsuarioUiState.Status.SUCCESS) {
            progress.setVisibility(View.GONE);
            Toast.makeText(this, R.string.users_access_updated, Toast.LENGTH_SHORT).show();
            viewModel.loadAll();
        } else if (state.getStatus() == UsuarioUiState.Status.ERROR) {
            progress.setVisibility(View.GONE);
            Toast.makeText(this, state.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void renderDeleteState(UsuarioUiState<Void> state) {
        if (state == null) return;
        if (state.getStatus() == UsuarioUiState.Status.LOADING) {
            progress.setVisibility(View.VISIBLE);
        } else if (state.getStatus() == UsuarioUiState.Status.SUCCESS) {
            progress.setVisibility(View.GONE);
            Toast.makeText(this, R.string.users_deleted, Toast.LENGTH_SHORT).show();
            viewModel.loadAll();
        } else if (state.getStatus() == UsuarioUiState.Status.ERROR) {
            progress.setVisibility(View.GONE);
            Toast.makeText(this, state.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static boolean validId(Long id) {
        return id != null && id > 0;
    }

    private static boolean contains(String[] values, String target) {
        for (String value : values) if (value.equals(target)) return true;
        return false;
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value.trim();
    }
}