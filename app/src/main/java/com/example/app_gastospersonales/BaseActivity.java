package com.example.app_gastospersonales;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Llámalo DESPUÉS de setContentView en cada hijo
     * @param toolbar la toolbar del hijo
     */
    protected void setupDrawer(Toolbar toolbar) {

        this.toolbar = toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Ícono de hamburguesa que abre/cierra el drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState(); // ← IMPORTANTE

        // Forzar icono visible
        toggle.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Clicks del menú lateral
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Class<?> destino = null;

            if (id == R.id.nav_home) {
               // destino = MainActivity.class;
            } else if (id == R.id.nav_Reportes) {
                // destino = ReportesActivity.class;
            } else if (id == R.id.nav_Perfil) {
                // destino = PerfilActivity.class;
            } else if (id == R.id.nav_convertir_dinero) {
                if (this instanceof convertir_dinero) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                destino = convertir_dinero.class;
            } else if (id == R.id.nav_Bancos) {
                 destino = CajerosActivity.class;
            }

            if (destino != null) {
                startActivity(new Intent(this, destino));
                finish(); // evita pila infinita
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return toggle.onOptionsItemSelected(null) || super.onSupportNavigateUp();
    }
}