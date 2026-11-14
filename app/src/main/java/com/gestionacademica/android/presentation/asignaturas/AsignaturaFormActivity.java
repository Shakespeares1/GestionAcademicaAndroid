package com.gestionacademica.android.presentation.asignaturas;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.gestionacademica.android.R;
import com.gestionacademica.android.data.dto.AsignaturaDto;
import com.gestionacademica.android.data.dto.AsignaturaRequest;
import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.presentation.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Optional;

public final class AsignaturaFormActivity extends AppCompatActivity {
    private TextInputLayout[] requiredLayouts;
    private TextInputEditText nameInput;
    private TextInputEditText fullNameInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText areaInput;
    private TextInputEditText careerInput;
    private TextInputEditText creditsInput;
    private TextInputEditText contentInput;
    private TextInputEditText semesterInput;
    private TextInputEditText professorInput;
    private TextView title;
    private TextView status;
    private ProgressBar progress;
    private MaterialButton saveButton;
    private MaterialButton retryLoadButton;
    private AsignaturasViewModel viewModel;
    private long assignmentId = -1L;
    private boolean editing;
    private boolean detailLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Optional<Session> session = new SessionManager(getApplicationContext()).getSession();
        if (!session.isPresent()) {
            openLogin();
            return;
        }
        if (!"ADMIN".equals(session.get().getRole())) {
            Toast.makeText(this, R.string.assignment_not_admin, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_asignatura_form);
        bindViews();
        assignmentId = getIntent().getLongExtra(AsignaturasActivity.EXTRA_ASIGNATURA_ID, -1L);
        editing = assignmentId > 0;
        title.setText(editing
                ? R.string.assignment_form_edit_title : R.string.assignment_form_create_title);

        viewModel = new ViewModelProvider(this).get(AsignaturasViewModel.class);
        viewModel.detailState().observe(this, this::renderDetail);
        viewModel.saveState().observe(this, this::renderSave);
        retryLoadButton.setOnClickListener(v -> {
            if (editing) viewModel.loadById(assignmentId);
        });
        saveButton.setOnClickListener(v -> submit());

        if (editing) {
            saveButton.setEnabled(false);
            viewModel.loadById(assignmentId);
        } else {
            detailLoaded = true;
            retryLoadButton.setVisibility(View.GONE);
        }
    }

    private void bindViews() {
        title = findViewById(R.id.assignmentFormTitle);
        status = findViewById(R.id.assignmentFormStatus);
        progress = findViewById(R.id.assignmentFormProgress);
        saveButton = findViewById(R.id.assignmentSaveButton);
        retryLoadButton = findViewById(R.id.assignmentFormRetry);
        TextInputLayout nameLayout = findViewById(R.id.assignmentNameLayout);
        TextInputLayout fullNameLayout = findViewById(R.id.assignmentFullNameLayout);
        TextInputLayout descriptionLayout = findViewById(R.id.assignmentDescriptionLayout);
        TextInputLayout areaLayout = findViewById(R.id.assignmentAreaLayout);
        TextInputLayout careerLayout = findViewById(R.id.assignmentCareerLayout);
        TextInputLayout creditsLayout = findViewById(R.id.assignmentCreditsLayout);
        TextInputLayout contentLayout = findViewById(R.id.assignmentContentLayout);
        TextInputLayout semesterLayout = findViewById(R.id.assignmentSemesterLayout);
        TextInputLayout professorLayout = findViewById(R.id.assignmentProfessorLayout);
        requiredLayouts = new TextInputLayout[] { nameLayout, fullNameLayout, descriptionLayout,
                areaLayout, careerLayout, creditsLayout, contentLayout, semesterLayout, professorLayout };
        nameInput = findViewById(R.id.assignmentNameInput);
        fullNameInput = findViewById(R.id.assignmentFullNameInput);
        descriptionInput = findViewById(R.id.assignmentDescriptionInput);
        areaInput = findViewById(R.id.assignmentAreaInput);
        careerInput = findViewById(R.id.assignmentCareerInput);
        creditsInput = findViewById(R.id.assignmentCreditsInput);
        contentInput = findViewById(R.id.assignmentContentInput);
        semesterInput = findViewById(R.id.assignmentSemesterInput);
        professorInput = findViewById(R.id.assignmentProfessorInput);
    }

    private void renderDetail(AsignaturaUiState<AsignaturaDto> state) {
        if (!editing || state == null) return;
        if (state.getStatus() == AsignaturaUiState.Status.LOADING) {
            progress.setVisibility(View.VISIBLE);
            retryLoadButton.setVisibility(View.GONE);
            saveButton.setEnabled(false);
        } else if (state.getStatus() == AsignaturaUiState.Status.ERROR) {
            progress.setVisibility(View.GONE);
            status.setText(state.getError().getMessage());
            status.setVisibility(View.VISIBLE);
            retryLoadButton.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
        } else if (state.getStatus() == AsignaturaUiState.Status.SUCCESS) {
            progress.setVisibility(View.GONE);
            AsignaturaDto item = state.getValue();
            if (item == null) {
                status.setText(R.string.assignment_detail_empty);
                status.setVisibility(View.VISIBLE);
                retryLoadButton.setVisibility(View.VISIBLE);
                return;
            }
            fill(item);
            detailLoaded = true;
            status.setVisibility(View.GONE);
            retryLoadButton.setVisibility(View.GONE);
            saveButton.setEnabled(true);
        }
    }

    private void fill(AsignaturaDto item) {
        set(nameInput, item.nombre);
        set(fullNameInput, item.nombreCompleto);
        set(descriptionInput, item.descripcion);
        set(areaInput, item.areaConocimiento);
        set(careerInput, item.carrera);
        set(creditsInput, String.valueOf(item.numeroCreditos));
        set(contentInput, item.contenidoTematico);
        set(semesterInput, String.valueOf(item.semestre));
        set(professorInput, item.profesor);
    }

    private void submit() {
        clearErrors();
        if (!detailLoaded) return;
        boolean valid = true;
        TextInputEditText[] textFields = { nameInput, fullNameInput, descriptionInput, areaInput,
                careerInput, creditsInput, contentInput, semesterInput, professorInput };
        for (int i = 0; i < textFields.length; i++) {
            String value = value(textFields[i]).trim();
            if (TextUtils.isEmpty(value)) {
                requiredLayouts[i].setError(getString(R.string.assignment_field_required));
                valid = false;
            }
        }

        Integer credits = parsePositiveInteger(creditsInput, 5);
        Integer semester = parsePositiveInteger(semesterInput, 7);
        if (credits == null || semester == null) valid = false;
        if (!valid) return;

        AsignaturaRequest request = new AsignaturaRequest();
        request.nombre = value(nameInput).trim();
        request.nombreCompleto = value(fullNameInput).trim();
        request.descripcion = value(descriptionInput).trim();
        request.areaConocimiento = value(areaInput).trim();
        request.carrera = value(careerInput).trim();
        request.numeroCreditos = credits;
        request.contenidoTematico = value(contentInput).trim();
        request.semestre = semester;
        request.profesor = value(professorInput).trim();
        saveButton.setEnabled(false);
        if (editing) viewModel.update(assignmentId, request);
        else viewModel.create(request);
    }

    private Integer parsePositiveInteger(TextInputEditText input, int layoutIndex) {
        String text = value(input).trim();
        if (TextUtils.isEmpty(text)) return null;
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                requiredLayouts[layoutIndex].setError(getString(R.string.assignment_integer_positive));
                return null;
            }
            return value;
        } catch (NumberFormatException exception) {
            requiredLayouts[layoutIndex].setError(getString(R.string.assignment_integer_required));
            return null;
        }
    }

    private void renderSave(AsignaturaUiState<AsignaturaDto> state) {
        if (state == null) return;
        boolean loading = state.getStatus() == AsignaturaUiState.Status.LOADING;
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading && detailLoaded);
        if (state.getStatus() == AsignaturaUiState.Status.ERROR) {
            status.setText(state.getError().getMessage());
            status.setVisibility(View.VISIBLE);
        } else if (state.getStatus() == AsignaturaUiState.Status.SUCCESS) {
            Toast.makeText(this, editing
                    ? R.string.assignment_updated : R.string.assignment_created, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    private void clearErrors() {
        for (TextInputLayout layout : requiredLayouts) layout.setError(null);
        status.setText("");
        status.setVisibility(View.GONE);
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private static void set(TextInputEditText input, String value) {
        input.setText(value == null ? "" : value);
    }

    private static String value(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}