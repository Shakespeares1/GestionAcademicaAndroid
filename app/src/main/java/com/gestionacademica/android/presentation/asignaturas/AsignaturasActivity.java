package com.gestionacademica.android.presentation.asignaturas;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.gestionacademica.android.R;
import com.gestionacademica.android.data.dto.AsignaturaDto;
import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.presentation.auth.LoginActivity;
import com.gestionacademica.android.utils.RoleAccessPolicy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class AsignaturasActivity extends AppCompatActivity {
    public static final String EXTRA_ASIGNATURA_ID =
            "com.gestionacademica.android.extra.ASIGNATURA_ID";

    private AsignaturasViewModel viewModel;
    private SessionManager sessionManager;
    private boolean isAdmin;
    private List<AsignaturaDto> assignments = new ArrayList<>();
    private TextInputEditText searchInput;
    private LinearLayout assignmentList;
    private ProgressBar progress;
    private TextView status;
    private TextView empty;
    private MaterialButton retry;
    private long requestedDetailId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getApplicationContext());
        Optional<Session> session = sessionManager.getSession();
        if (!session.isPresent()
                || !RoleAccessPolicy.canViewAcademicModules(session.get().getRole())) {
            openLogin();
            return;
        }
        isAdmin = RoleAccessPolicy.isAdmin(session.get().getRole());
        setContentView(R.layout.activity_asignaturas);

        searchInput = findViewById(R.id.assignmentsSearch);
        assignmentList = findViewById(R.id.assignmentList);
        progress = findViewById(R.id.assignmentsProgress);
        status = findViewById(R.id.assignmentsStatus);
        empty = findViewById(R.id.assignmentsEmpty);
        retry = findViewById(R.id.assignmentsRetry);
        MaterialButton add = findViewById(R.id.addAssignmentButton);
        add.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        add.setOnClickListener(v ->
                startActivity(new Intent(this, AsignaturaFormActivity.class)));
        retry.setOnClickListener(v -> viewModel.loadAll());

        viewModel = new ViewModelProvider(this).get(AsignaturasViewModel.class);
        viewModel.listState().observe(this, this::renderListState);
        viewModel.detailState().observe(this, this::renderDetailState);
        viewModel.deleteState().observe(this, this::renderDeleteState);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderAssignments();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.loadAll();
    }

    private void renderListState(AsignaturaUiState<List<AsignaturaDto>> state) {
        if (state == null) return;
        boolean loading = state.getStatus() == AsignaturaUiState.Status.LOADING;
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        retry.setVisibility(View.GONE);
        if (state.getStatus() == AsignaturaUiState.Status.SUCCESS) {
            assignments = state.getValue() == null ? new ArrayList<>() : state.getValue();
            status.setText("");
            status.setVisibility(View.GONE);
            renderAssignments();
        } else if (state.getStatus() == AsignaturaUiState.Status.ERROR) {
            status.setText(state.getError().getMessage());
            status.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        }
    }

    private void renderAssignments() {
        assignmentList.removeAllViews();
        String query = searchInput.getText() == null
                ? "" : searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        int shown = 0;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (AsignaturaDto item : assignments) {
            if (item == null || !matches(item, query)) continue;
            View card = inflater.inflate(R.layout.item_asignatura, assignmentList, false);
            TextView title = card.findViewById(R.id.itemTitle);
            TextView metadata = card.findViewById(R.id.itemMetadata);
            TextView credits = card.findViewById(R.id.itemCredits);
            MaterialButton details = card.findViewById(R.id.itemDetailsButton);
            LinearLayout adminActions = card.findViewById(R.id.itemAdminActions);
            MaterialButton edit = card.findViewById(R.id.itemEditButton);
            MaterialButton delete = card.findViewById(R.id.itemDeleteButton);

            title.setText(nonEmpty(item.nombreCompleto) ? item.nombreCompleto : safe(item.nombre));
            metadata.setText(getString(R.string.assignment_card_metadata,
                    safe(item.carrera), safe(item.profesor)));
            credits.setText(getString(R.string.assignment_credits_semester,
                    item.numeroCreditos, item.semestre));
            details.setOnClickListener(v -> loadDetails(item.id));
            adminActions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            edit.setOnClickListener(v -> edit(item.id));
            delete.setOnClickListener(v -> confirmDelete(item));

            assignmentList.addView(card);
            shown++;
        }

        if (shown == 0) {
            boolean noData = assignments.isEmpty();
            empty.setText(noData
                    ? R.string.assignments_empty : R.string.assignments_no_matches);
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    private boolean matches(AsignaturaDto item, String query) {
        if (query.isEmpty()) return true;
        return searchable(item.nombre).contains(query)
                || searchable(item.nombreCompleto).contains(query)
                || searchable(item.carrera).contains(query)
                || searchable(item.profesor).contains(query);
    }

    private static String searchable(String value) {
        return safe(value).toLowerCase(Locale.ROOT);
    }

    private void loadDetails(Long id) {
        if (id == null || id <= 0) {
            Toast.makeText(this, R.string.assignment_invalid_id, Toast.LENGTH_SHORT).show();
            return;
        }
        requestedDetailId = id;
        viewModel.loadById(id);
    }

    private void renderDetailState(AsignaturaUiState<AsignaturaDto> state) {
        if (state == null || state.getStatus() == AsignaturaUiState.Status.IDLE) return;
        if (state.getStatus() == AsignaturaUiState.Status.LOADING) {
            progress.setVisibility(View.VISIBLE);
            return;
        }
        progress.setVisibility(View.GONE);
        if (state.getStatus() == AsignaturaUiState.Status.ERROR) {
            Toast.makeText(this, state.getError().getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        AsignaturaDto item = state.getValue();
        if (item == null || item.id == null || item.id != requestedDetailId) return;
        String details = getString(R.string.assignment_detail_text,
                safe(item.nombre), safe(item.nombreCompleto), safe(item.descripcion),
                safe(item.areaConocimiento), safe(item.carrera), item.numeroCreditos,
                safe(item.contenidoTematico), item.semestre, safe(item.profesor));
        new AlertDialog.Builder(this)
                .setTitle(R.string.assignment_details_title)
                .setMessage(details)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void edit(Long id) {
        if (!isAdmin) return;
        if (id == null || id <= 0) {
            Toast.makeText(this, R.string.assignment_invalid_id, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, AsignaturaFormActivity.class);
        intent.putExtra(EXTRA_ASIGNATURA_ID, id);
        startActivity(intent);
    }

    private void confirmDelete(AsignaturaDto item) {
        if (!isAdmin || item.id == null || item.id <= 0) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.assignment_delete_confirm_title)
                .setMessage(getString(R.string.assignment_delete_confirm_message,
                        nonEmpty(item.nombreCompleto) ? item.nombreCompleto : safe(item.nombre)))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.assignments_delete,
                        (dialog, which) -> viewModel.delete(item.id))
                .show();
    }

    private void renderDeleteState(AsignaturaUiState<Void> state) {
        if (state == null) return;
        if (state.getStatus() == AsignaturaUiState.Status.LOADING) {
            progress.setVisibility(View.VISIBLE);
        } else if (state.getStatus() == AsignaturaUiState.Status.SUCCESS) {
            progress.setVisibility(View.GONE);
            Toast.makeText(this, R.string.assignment_deleted, Toast.LENGTH_SHORT).show();
            viewModel.loadAll();
        } else if (state.getStatus() == AsignaturaUiState.Status.ERROR) {
            progress.setVisibility(View.GONE);
            Toast.makeText(this, state.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private static boolean nonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String safe(String value) {
        return nonEmpty(value) ? value.trim() : "—";
    }
}