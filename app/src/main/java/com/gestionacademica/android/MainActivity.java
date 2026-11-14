package com.gestionacademica.android;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.presentation.auth.LoginActivity;
import com.gestionacademica.android.presentation.home.HomeActivity;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Optional<Session> session = new SessionManager(getApplicationContext()).getSession();
        Intent destination = session.isPresent()
                ? new Intent(this, HomeActivity.class)
                : new Intent(this, LoginActivity.class);
        startActivity(destination);
        finish();
    }
}