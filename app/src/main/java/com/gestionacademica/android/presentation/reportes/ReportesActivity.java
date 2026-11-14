package com.gestionacademica.android.presentation.reportes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.gestionacademica.android.R;
import com.gestionacademica.android.data.dto.AsignaturaDto;
import com.gestionacademica.android.data.dto.ReportePageDto;
import com.gestionacademica.android.data.dto.UsuarioDto;
import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.presentation.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class ReportesActivity extends AppCompatActivity {
    private final List<ReportType> availableTypes = new ArrayList<>();
    private final List<String> availableLabels = new ArrayList<>();
    private ReportType selectedType;
    private ReportesViewModel viewModel;
    private boolean isAdmin;

    private MaterialAutoCompleteTextView reportSelector;
    private TextView description;
    private View careerFilters;
    private View creditsFilters;
    private View userRoleFilters;
    private View dateFilters;
    private TextInputEditText careerInput;
    private TextInputEditText semesterInput;
    private TextInputEditText areaInput;
    private TextInputEditText creditsMinInput;
    private TextInputEditText creditsMaxInput;
    private TextInputEditText professorInput;
    private TextInputEditText assignmentNameInput;
    private MaterialAutoCompleteTextView roleDropdown;
    private MaterialAutoCompleteTextView statusDropdown;
    private TextInputEditText dateFromInput;
    private TextInputEditText dateToInput;
    private TextInputLayout semesterLayout;
    private TextInputLayout creditsMinLayout;
    private TextInputLayout creditsMaxLayout;
    private TextInputLayout dateFromLayout;
    private TextInputLayout dateToLayout;
    private TextInputLayout pageLayout;
    private TextInputLayout sizeLayout;
    private TextInputEditText pageInput;
    private TextInputEditText sizeInput;
    private ProgressBar progress;
    private TextView status;
    private TextView pageInfo;
    private TextView empty;
    private LinearLayout results;
    private View pagination;
    private MaterialButton previousPage;
    private MaterialButton nextPage;
    private ReportePageDto<?> currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Optional<Session> session = new SessionManager(getApplicationContext()).getSession();
        if (!session.isPresent()) {
            openLogin();
            return;
        }
        String role = session.get().getRole();
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            openLogin();
            return;
        }
        isAdmin = "ADMIN".equals(role);
        setContentView(R.layout.activity_reportes);
        bindViews();
        setupReportOptions();
        setupFilterDropdowns();
        configureListeners();

        viewModel = new ViewModelProvider(this).get(ReportesViewModel.class);
        viewModel.state().observe(this, this::renderState);
        selectReport(0);
    }

    private void bindViews() {
        reportSelector = findViewById(R.id.reportSelector);
        description = findViewById(R.id.reportDescription);
        careerFilters = findViewById(R.id.reportCareerFilters);
        creditsFilters = findViewById(R.id.reportCreditsFilters);
        userRoleFilters = findViewById(R.id.reportUserRoleFilters);
        dateFilters = findViewById(R.id.reportDateFilters);
        careerInput = findViewById(R.id.reportCareerInput);
        semesterInput = findViewById(R.id.reportSemesterInput);
        areaInput = findViewById(R.id.reportAreaInput);
        creditsMinInput = findViewById(R.id.reportCreditsMinInput);
        creditsMaxInput = findViewById(R.id.reportCreditsMaxInput);
        professorInput = findViewById(R.id.reportProfessorInput);
        assignmentNameInput = findViewById(R.id.reportAssignmentNameInput);
        roleDropdown = findViewById(R.id.reportRoleDropdown);
        statusDropdown = findViewById(R.id.reportAccountStatusDropdown);
        dateFromInput = findViewById(R.id.reportDateFromInput);
        dateToInput = findViewById(R.id.reportDateToInput);
        semesterLayout = findViewById(R.id.reportSemesterLayout);
        creditsMinLayout = findViewById(R.id.reportCreditsMinLayout);
        creditsMaxLayout = findViewById(R.id.reportCreditsMaxLayout);
        dateFromLayout = findViewById(R.id.reportDateFromLayout);
        dateToLayout = findViewById(R.id.reportDateToLayout);
        pageLayout = findViewById(R.id.reportPageLayout);
        sizeLayout = findViewById(R.id.reportSizeLayout);
        pageInput = findViewById(R.id.reportPageInput);
        sizeInput = findViewById(R.id.reportSizeInput);
        progress = findViewById(R.id.reportProgress);
        status = findViewById(R.id.reportStatus);
        pageInfo = findViewById(R.id.reportPageInfo);
        empty = findViewById(R.id.reportEmpty);
        results = findViewById(R.id.reportResults);
        pagination = findViewById(R.id.reportPagination);
        previousPage = findViewById(R.id.reportPreviousPage);
        nextPage = findViewById(R.id.reportNextPage);
    }

    private void setupReportOptions() {
        addReport(ReportType.ASSIGNMENTS_CAREER_SEMESTER_AREA,
                R.string.report_title_career);
        addReport(ReportType.ASSIGNMENTS_CREDITS_PROFESSOR_NAME,
                R.string.report_title_credits);
        if (isAdmin) {
            addReport(ReportType.USERS_ROLE_STATUS, R.string.report_title_user_role);
            addReport(ReportType.USERS_REGISTRATION_DATE, R.string.report_title_user_date);
        }
        reportSelector.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, availableLabels));
    }

    private void addReport(ReportType type, int labelResource) {
        availableTypes.add(type);
        availableLabels.add(getString(labelResource));
    }

    private void setupFilterDropdowns() {
        String[] roleValues = getResources().getStringArray(R.array.report_role_filters);
        String[] statusValues = getResources().getStringArray(R.array.report_status_filters);
        roleDropdown.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, roleValues));
        statusDropdown.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, statusValues));
        roleDropdown.setText(roleValues[0], false);
        statusDropdown.setText(statusValues[0], false);
    }

    private void configureListeners() {
        reportSelector.setOnItemClickListener((parent, view, position, id) -> selectReport(position));
        findViewById(R.id.runReportButton).setOnClickListener(v -> runReport());
        findViewById(R.id.clearReportFiltersButton).setOnClickListener(v -> clearFilters());
        previousPage.setOnClickListener(v -> changePage(-1));
        nextPage.setOnClickListener(v -> changePage(1));
    }

    private void selectReport(int index) {
        if (index < 0 || index >= availableTypes.size()) return;
        selectedType = availableTypes.get(index);
        reportSelector.setText(availableLabels.get(index), false);
        careerFilters.setVisibility(selectedType == ReportType.ASSIGNMENTS_CAREER_SEMESTER_AREA
                ? View.VISIBLE : View.GONE);
        creditsFilters.setVisibility(selectedType == ReportType.ASSIGNMENTS_CREDITS_PROFESSOR_NAME
                ? View.VISIBLE : View.GONE);
        userRoleFilters.setVisibility(selectedType == ReportType.USERS_ROLE_STATUS
                ? View.VISIBLE : View.GONE);
        dateFilters.setVisibility(selectedType == ReportType.USERS_REGISTRATION_DATE
                ? View.VISIBLE : View.GONE);
        description.setText(descriptionFor(selectedType));
        clearResults();
    }

    private int descriptionFor(ReportType type) {
        switch (type) {
            case ASSIGNMENTS_CAREER_SEMESTER_AREA:
                return R.string.report_description_career;
            case ASSIGNMENTS_CREDITS_PROFESSOR_NAME:
                return R.string.report_description_credits;
            case USERS_ROLE_STATUS:
                return R.string.report_description_user_role;
            case USERS_REGISTRATION_DATE:
                return R.string.report_description_user_date;
            default:
                return R.string.reports_title;
        }
    }

    private void runReport() {
        clearValidationErrors();
        Integer page = parseNonNegative(pageInput, pageLayout, R.string.report_page_invalid);
        Integer size = parsePositive(sizeInput, sizeLayout, R.string.report_size_invalid);
        if (page == null || size == null || size > 100) {
            if (size != null && size > 100) {
                sizeLayout.setError(getString(R.string.report_size_invalid));
            }
            return;
        }

        switch (selectedType) {
            case ASSIGNMENTS_CAREER_SEMESTER_AREA:
                Integer semester = parseOptionalPositive(semesterInput, semesterLayout);
                if (!TextUtils.isEmpty(value(semesterInput).trim()) && semester == null) return;
                viewModel.asignaturasPorCarreraSemestreArea(
                        optionalText(careerInput), semester, optionalText(areaInput), page, size);
                break;
            case ASSIGNMENTS_CREDITS_PROFESSOR_NAME:
                Integer min = parseOptionalPositive(creditsMinInput, creditsMinLayout);
                Integer max = parseOptionalPositive(creditsMaxInput, creditsMaxLayout);
                if ((!TextUtils.isEmpty(value(creditsMinInput).trim()) && min == null)
                        || (!TextUtils.isEmpty(value(creditsMaxInput).trim()) && max == null)) return;
                if (min != null && max != null && min > max) {
                    creditsMinLayout.setError(getString(R.string.report_credit_range_invalid));
                    return;
                }
                viewModel.asignaturasPorCreditosProfesorNombre(min, max,
                        optionalText(professorInput), optionalText(assignmentNameInput), page, size);
                break;
            case USERS_ROLE_STATUS:
                if (!isAdmin) {
                    showLocalError(R.string.report_users_admin_only);
                    return;
                }
                viewModel.usuariosPorRolEstado(selectedValue(roleDropdown, R.array.report_role_filters),
                        selectedValue(statusDropdown, R.array.report_status_filters), page, size);
                break;
            case USERS_REGISTRATION_DATE:
                if (!isAdmin) {
                    showLocalError(R.string.report_users_admin_only);
                    return;
                }
                String from = optionalText(dateFromInput);
                String to = optionalText(dateToInput);
                Date fromDate = parseIsoDate(from, dateFromLayout);
                Date toDate = parseIsoDate(to, dateToLayout);
                if ((from != null && fromDate == null) || (to != null && toDate == null)) return;
                if (fromDate != null && toDate != null && fromDate.after(toDate)) {
                    dateFromLayout.setError(getString(R.string.report_date_range_invalid));
                    return;
                }
                viewModel.usuariosPorFechaRegistro(from, to, page, size);
                break;
        }
    }

    private void clearFilters() {
        careerInput.setText("");
        semesterInput.setText("");
        areaInput.setText("");
        creditsMinInput.setText("");
        creditsMaxInput.setText("");
        professorInput.setText("");
        assignmentNameInput.setText("");
        String any = getString(R.string.report_any);
        roleDropdown.setText(any, false);
        statusDropdown.setText(any, false);
        dateFromInput.setText("");
        dateToInput.setText("");
        pageInput.setText(getString(R.string.report_page_default));
        sizeInput.setText(getString(R.string.report_size_default));
        clearValidationErrors();
        clearResults();
        runReport();
    }

    private void changePage(int delta) {
        if (currentPage == null) return;
        int page = Math.max(0, currentPage.page + delta);
        pageInput.setText(String.valueOf(page));
        runReport();
    }

    private void renderState(ReportesUiState state) {
        if (state == null || state.getStatus() == ReportesUiState.Status.IDLE
                || state.getReportType() != selectedType) return;
        boolean loading = state.getStatus() == ReportesUiState.Status.LOADING;
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            status.setVisibility(View.GONE);
            return;
        }
        if (state.getStatus() == ReportesUiState.Status.ERROR) {
            currentPage = null;
            results.removeAllViews();
            pageInfo.setVisibility(View.GONE);
            pagination.setVisibility(View.GONE);
            empty.setVisibility(View.GONE);
            status.setText(state.getError().getMessage());
            status.setVisibility(View.VISIBLE);
            return;
        }
        ReportePageDto<?> page = state.getPage();
        if (page == null) {
            showLocalError(R.string.report_empty);
            return;
        }
        currentPage = page;
        status.setVisibility(View.GONE);
        pageInfo.setVisibility(View.VISIBLE);
        pageInfo.setText(getString(R.string.report_page_info,
                page.page + 1, Math.max(1, page.totalPages), page.totalElements));
        pageInput.setText(String.valueOf(page.page));
        renderResults(page);
        boolean hasPages = page.totalPages > 1;
        pagination.setVisibility(hasPages ? View.VISIBLE : View.GONE);
        previousPage.setEnabled(page.page > 0);
        nextPage.setEnabled(page.page + 1 < page.totalPages);
        empty.setVisibility(page.items == null || page.items.isEmpty() ? View.VISIBLE : View.GONE);
        if (empty.getVisibility() == View.VISIBLE) empty.setText(R.string.report_empty);
    }

    private void renderResults(ReportePageDto<?> page) {
        results.removeAllViews();
        if (page.items == null) return;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Object item : page.items) {
            View card = inflater.inflate(R.layout.item_report_result, results, false);
            TextView title = card.findViewById(R.id.reportResultTitle);
            TextView summary = card.findViewById(R.id.reportResultSummary);
            if (item instanceof AsignaturaDto && isAssignmentReport()) {
                AsignaturaDto assignment = (AsignaturaDto) item;
                title.setText(nonEmpty(assignment.nombreCompleto)
                        ? assignment.nombreCompleto : safe(assignment.nombre));
                summary.setText(getString(R.string.report_assignment_result,
                        safe(assignment.nombreCompleto), safe(assignment.carrera), assignment.semestre,
                        safe(assignment.areaConocimiento), assignment.numeroCreditos, safe(assignment.profesor)));
            } else if (item instanceof UsuarioDto && isUserReport()) {
                UsuarioDto user = (UsuarioDto) item;
                title.setText(safe(user.nombreCompleto));
                summary.setText(getString(R.string.report_user_result, safe(user.nombreCompleto),
                        safe(user.email), safe(user.role), safe(user.accountStatus), safe(user.fechaCreacion)));
            } else {
                continue;
            }
            results.addView(card);
        }
    }

    private boolean isAssignmentReport() {
        return selectedType == ReportType.ASSIGNMENTS_CAREER_SEMESTER_AREA
                || selectedType == ReportType.ASSIGNMENTS_CREDITS_PROFESSOR_NAME;
    }

    private boolean isUserReport() {
        return selectedType == ReportType.USERS_ROLE_STATUS
                || selectedType == ReportType.USERS_REGISTRATION_DATE;
    }

    private void clearResults() {
        currentPage = null;
        results.removeAllViews();
        pageInfo.setVisibility(View.GONE);
        pagination.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);
        status.setVisibility(View.GONE);
    }

    private void clearValidationErrors() {
        semesterLayout.setError(null);
        creditsMinLayout.setError(null);
        creditsMaxLayout.setError(null);
        dateFromLayout.setError(null);
        dateToLayout.setError(null);
        pageLayout.setError(null);
        sizeLayout.setError(null);
    }

    private void showLocalError(int messageResource) {
        progress.setVisibility(View.GONE);
        status.setText(messageResource);
        status.setVisibility(View.VISIBLE);
    }

    private Integer parseNonNegative(TextInputEditText input, TextInputLayout layout, int error) {
        String raw = value(input).trim();
        try {
            int parsed = Integer.parseInt(raw);
            if (parsed < 0) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException exception) {
            layout.setError(getString(error));
            return null;
        }
    }

    private Integer parsePositive(TextInputEditText input, TextInputLayout layout, int error) {
        String raw = value(input).trim();
        try {
            int parsed = Integer.parseInt(raw);
            if (parsed < 1) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException exception) {
            layout.setError(getString(error));
            return null;
        }
    }

    private Integer parseOptionalPositive(TextInputEditText input, TextInputLayout layout) {
        String raw = value(input).trim();
        if (raw.isEmpty()) return null;
        try {
            int parsed = Integer.parseInt(raw);
            if (parsed < 1) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException exception) {
            layout.setError(getString(R.string.report_integer_invalid));
            return null;
        }
    }

    private Date parseIsoDate(String raw, TextInputLayout layout) {
        if (raw == null) return null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        Date parsed = format.parse(raw, position);
        if (parsed == null || position.getIndex() != raw.length()) {
            layout.setError(getString(R.string.report_date_invalid));
            return null;
        }
        return parsed;
    }

    private String selectedValue(AutoCompleteTextView dropdown, int optionsResource) {
        String selected = dropdown.getText() == null ? "" : dropdown.getText().toString().trim();
        String any = getString(R.string.report_any);
        if (selected.isEmpty() || any.equals(selected)) return null;
        for (String option : getResources().getStringArray(optionsResource)) {
            if (option.equals(selected)) return selected;
        }
        return null;
    }

    private static String optionalText(TextInputEditText input) {
        String value = input.getText() == null ? "" : input.getText().toString().trim();
        return value.isEmpty() ? null : value;
    }

    private static String value(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private static boolean nonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String safe(String value) {
        return nonEmpty(value) ? value.trim() : "—";
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}