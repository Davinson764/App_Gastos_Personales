package com.example.app_gastospersonales;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.app_gastospersonales.databinding.ActivityConvertirDineroBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.*;
import retrofit2.*;

public class convertir_dinero extends BaseActivity {

    // Views
    Spinner spinner1, spinner2;
    EditText etValor;
    TextView tvTotal;
    Button btnConvertir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar layout propio
        ActivityConvertirDineroBinding binding = ActivityConvertirDineroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Toolbar y menú lateral (heredado)
        Toolbar toolbar = binding.appBarConvertirDinero.toolbar;
        setupDrawer(toolbar); // Método de BaseActivity

        // Enlazar views
        spinner1 = findViewById(R.id.spinner_1);
        spinner2 = findViewById(R.id.spinner_2);
        etValor  = findViewById(R.id.et_valor);
        tvTotal  = findViewById(R.id.tv_total);
        btnConvertir = findViewById(R.id.btn_convertir);

        // Llenar spinners
        List<String> monedas = Arrays.asList("USD", "EUR", "ARS", "MXN", "COP", "PEN", "BRL");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monedas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);

        // Botón convertir
        btnConvertir.setOnClickListener(v -> convertirMoneda());
    }

    private void convertirMoneda() {
        String base = spinner1.getSelectedItem().toString();
        String destino = spinner2.getSelectedItem().toString();
        String input = etValor.getText().toString();
        double monto;
        try {
            monto = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            tvTotal.setText("Ingresa un monto válido");
            return;
        }

        CurrencyApiService api = RetrofitClient.getService();
        Call<CurrencyResponse> call = api.getLatestRates(
                "cur_live_wlZJ6UGYe70Z5X6u28PVQVL4mNWMJdtD0AsOb5Gq",
                base
        );

        call.enqueue(new Callback<CurrencyResponse>() {
            @Override
            public void onResponse(Call<CurrencyResponse> call, Response<CurrencyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Divisa currency = response.body().data.get(destino);
                    if (currency != null) {
                        double total = monto * currency.value;
                        tvTotal.setText(String.format(Locale.getDefault(), "Total: %.2f %s", total, destino));
                    } else {
                        tvTotal.setText("Moneda destino no encontrada");
                    }
                } else {
                    tvTotal.setText("Error en la respuesta");
                }
            }

            @Override
            public void onFailure(Call<CurrencyResponse> call, Throwable t) {
                tvTotal.setText("Fallo: " + t.getMessage());
            }
        });
    }
}