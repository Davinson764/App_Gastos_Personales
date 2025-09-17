package com.example.app_gastospersonales;

import android.os.Bundle;
import android.view.Menu;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.app_gastospersonales.databinding.ActivityConvertirDineroBinding;
import com.google.android.material.navigation.NavigationView; // ✅ ESTA ESTÁ BIEN

public class convertirDinero extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityConvertirDineroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityConvertirDineroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBarConvertirDinero.toolbar;
        setSupportActionBar(toolbar);

        // ❌ FAB ELIMINADO – ya no hace falta
        // binding.appBarConvertirDinero.fab.setOnClickListener(...);

        // ✅ Toolbar sin título
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;   // ✔️ tipo correcto

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_Inicio, R.id.nav_Reportes, R.id.nav_Perfil)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_convertir_dinero);

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController); // ✔️ compila
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.convertir_dinero, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_convertir_dinero);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}