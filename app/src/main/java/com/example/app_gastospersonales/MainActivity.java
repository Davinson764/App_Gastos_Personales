package com.example.app_gastospersonales;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setupDrawer(toolbar); // ahora sí existe
    }
}