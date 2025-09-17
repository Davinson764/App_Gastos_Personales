package com.example.app_gastospersonales;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CajerosActivity extends BaseActivity {

    private static final int PERMISO_UBICACION = 100;

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private Spinner spinner;
    private TextView tvNombre, tvDireccion;
    private GeoPoint miUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de OSMdroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_cajeros);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setupDrawer(toolbar);

        // Vistas
        mapView   = findViewById(R.id.mapView);
        spinner   = findViewById(R.id.spinner);
        tvNombre  = findViewById(R.id.tvNombre);
        tvDireccion = findViewById(R.id.tvDireccion);

        // Mapa SATELITAL
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);

        // Spinner
        List<String> bancos = Arrays.asList("Bancolombia", "Davivienda", "Banco de Bogotá", "Banco Agrario");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bancos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        obtenerUbicacionActual();

        // Cambio de banco
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (miUbicacion != null) {
                    String bancoSeleccionado = bancos.get(position);
                    buscarBancos(bancoSeleccionado);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISO_UBICACION);
        } else {
            leerUbicacion();
        }
    }

    private void leerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                miUbicacion = new GeoPoint(location.getLatitude(), location.getLongitude());
                mostrarMiUbicacion(miUbicacion);
                buscarBancos(spinner.getSelectedItem().toString());
            } else {
                Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarMiUbicacion(GeoPoint punto) {
        mapView.getController().setZoom(18.0);
        mapView.getController().setCenter(punto);

        mapView.getOverlays().clear();
        Marker marker = new Marker(mapView);
        marker.setPosition(punto);
        marker.setTitle("Aquí estás");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation));
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    private void buscarBancos(String nombreBanco) {
        if (miUbicacion == null) return;

        double lat = miUbicacion.getLatitude();
        double lon = miUbicacion.getLongitude();

        String overpassQuery = String.format(Locale.US,
                "[out:json];(node[\"amenity\"=\"bank\"](around:5000,%f,%f);node[\"amenity\"=\"atm\"](around:5000,%f,%f););out;",
                lat, lon, lat, lon);

        String url = "https://overpass-api.de/api/interpreter?data=" + overpassQuery;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(CajerosActivity.this, "Error consultando Overpass", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONArray elements = obj.getJSONArray("elements");

                    runOnUiThread(() -> {
                        // Limpia mapa y muestra tu ubicación
                        mostrarMiUbicacion(miUbicacion);

                        // Variables para el PRIMER resultado
                        String primerNombre = null;
                        String primerDireccion = null;

                        for (int i = 0; i < elements.length(); i++) {
                            JSONObject banco = elements.optJSONObject(i);
                            if (banco == null) continue;

                            double blat = banco.optDouble("lat");
                            double blon = banco.optDouble("lon");
                            String bname = banco.optJSONObject("tags").optString("name", "Banco");

                            // Filtra por nombre
                            if (bname.toLowerCase().contains(nombreBanco.toLowerCase())) {
                                GeoPoint bancoPunto = new GeoPoint(blat, blon);

                                Marker marker = new Marker(mapView);
                                marker.setPosition(bancoPunto);
                                marker.setTitle(bname);
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                marker.setIcon(ContextCompat.getDrawable(CajerosActivity.this, android.R.drawable.ic_menu_compass));
                                mapView.getOverlays().add(marker);

                                // Guardar PRIMER resultado
                                if (primerNombre == null) {
                                    primerNombre = bname;
                                    primerDireccion = obtenerDireccion(blat, blon);
                                }
                            }
                        }

                        // Actualiza TextViews con el PRIMER resultado
                        if (primerNombre != null) {
                            tvNombre.setText(primerNombre);
                            tvDireccion.setText(primerDireccion);
                        } else {
                            tvNombre.setText("Sin resultados");
                            tvDireccion.setText("--");
                        }

                        mapView.invalidate();
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(CajerosActivity.this, "Error procesando datos", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String obtenerDireccion(double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(lat, lon, 1);
            if (list != null && !list.isEmpty()) {
                return list.get(0).getAddressLine(0);
            }
        } catch (Exception e) {
            // nada
        }
        return "Dirección no disponible";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_UBICACION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            leerUbicacion();
        } else {
            Toast.makeText(this, "Permiso ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }
}