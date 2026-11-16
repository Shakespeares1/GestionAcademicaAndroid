package com.gestionacademica.android.presentation.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.gestionacademica.android.R;
import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.presentation.asignaturas.AsignaturasActivity;
import com.gestionacademica.android.presentation.auth.LoginActivity;
import com.gestionacademica.android.presentation.reportes.ReportesActivity;
import com.gestionacademica.android.presentation.sensores.SensoresActivity;
import com.gestionacademica.android.presentation.usuarios.UsuariosActivity;
import com.gestionacademica.android.utils.RoleAccessPolicy;
import java.util.Optional;

public final class HomeActivity extends AppCompatActivity {
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getApplicationContext());
        Optional<Session> session = sessionManager.getSession();
        if (!session.isPresent()) {
            openLogin();
            return;
        }
        if (!RoleAccessPolicy.canViewAcademicModules(session.get().getRole())) {
            openLogin();
            return;
        }
        setContentView(R.layout.activity_home);
        String role = session.get().getRole();
        TextView roleLabel = findViewById(R.id.homeRole);
        TextView title = findViewById(R.id.homeTitle);
        TextView description = findViewById(R.id.homeDescription);
        if (RoleAccessPolicy.isAdmin(role)) {
            roleLabel.setText(R.string.home_role_admin);
            title.setText(R.string.home_admin_title);
            description.setText(R.string.home_admin_description);
        } else {
            roleLabel.setText(R.string.home_role_user);
            title.setText(R.string.home_user_title);
            description.setText(R.string.home_user_description);
        }
        findViewById(R.id.assignmentsButton).setOnClickListener(v -> openAssignments());
        android.view.View usersButton = findViewById(R.id.usersManagementButton);
        usersButton.setVisibility(RoleAccessPolicy.isAdmin(role)
                ? android.view.View.VISIBLE : android.view.View.GONE);
        usersButton.setOnClickListener(v -> openUsersManagement());
        findViewById(R.id.reportsButton).setOnClickListener(v -> openReports());
        findViewById(R.id.sensorsButton).setOnClickListener(v -> openSensors());
        findViewById(R.id.logoutButton).setOnClickListener(v -> logout());
    }

    private void openAssignments() {
        Optional<Session> current = sessionManager.getSession();
        if (!current.isPresent()) {
            openLogin();
            return;
        }
        if (!RoleAccessPolicy.canViewAcademicModules(current.get().getRole())) {
            openLogin();
            return;
        }
        startActivity(new Intent(this, AsignaturasActivity.class));
    }

    private void openSensors() {
        Optional<Session> current = sessionManager.getSession();
        if (!current.isPresent()) {
            openLogin();
            return;
        }
        String role = current.get().getRole();
        if (!RoleAccessPolicy.canViewAcademicModules(role)) {
            openLogin();
            return;
        }
        startActivity(new Intent(this, SensoresActivity.class));
    }

    private void openReports() {
        Optional<Session> current = sessionManager.getSession();
        if (!current.isPresent()) {
            openLogin();
            return;
        }
        String role = current.get().getRole();
        if (!RoleAccessPolicy.canViewAcademicModules(role)) {
            openLogin();
            return;
        }
        startActivity(new Intent(this, ReportesActivity.class));
    }

    private void openUsersManagement() {
        Optional<Session> current = sessionManager.getSession();
        if (!current.isPresent()) {
            openLogin();
            return;
        }
        if (!RoleAccessPolicy.isAdmin(current.get().getRole())) {
            android.widget.Toast.makeText(this, R.string.users_admin_only,
                    android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(new Intent(this, UsuariosActivity.class));
    }

    private void logout() {
        sessionManager.clearSession();
        openLogin();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}