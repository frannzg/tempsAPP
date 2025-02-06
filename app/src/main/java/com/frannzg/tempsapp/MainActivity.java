package com.frannzg.tempsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "47d956df6f27f12f28d3c273e7a6028b";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private TextView currentWeatherTextView, searchWeatherTextView;
    private EditText cityInput;
    private Button searchButton;
    private FusedLocationProviderClient fusedLocationClient;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentWeatherTextView = findViewById(R.id.currentWeatherTextView);
        searchWeatherTextView = findViewById(R.id.searchWeatherTextView);
        cityInput = findViewById(R.id.cityInput);
        searchButton = findViewById(R.id.searchButton);
        progressBar = findViewById(R.id.progressBar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener localización al iniciar la app
        getLocationAndFetchWeather();

        // Buscar por nombre de ciudad
        searchButton.setOnClickListener(v -> {
            String city = cityInput.getText().toString();
            if (city.isEmpty()) {
                searchWeatherTextView.setText("Por favor, introduce una ciudad.");
            } else {
                fetchWeatherByCity(city);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocationAndFetchWeather() {
        progressBar.setVisibility(View.VISIBLE); // Mostrar el ProgressBar mientras se carga

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                fetchWeather(location.getLatitude(), location.getLongitude());
            } else {
                currentWeatherTextView.setText("No se pudo obtener la ubicación.");
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            currentWeatherTextView.setText("Error al obtener ubicación.");
            progressBar.setVisibility(View.GONE);
        });
    }

    private void fetchWeather(double latitude, double longitude) {
        String url = BASE_URL + "?lat=" + latitude + "&lon=" + longitude +
                "&units=metric&appid=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        String cityName = response.getString("name");
                        String country = response.getJSONObject("sys").getString("country");

                        // Mostrar datos de la ubicación actual
                        currentWeatherTextView.setText(
                                "Ciudad: " + cityName + ", " + country + "\n" +
                                        "Temp: " + temp + "°C"
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar después de cargar los datos
                    }
                },
                error -> {
                    currentWeatherTextView.setText("Error al obtener el clima.");
                    progressBar.setVisibility(View.GONE);
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void fetchWeatherByCity(String city) {
        progressBar.setVisibility(View.VISIBLE);

        String url = BASE_URL + "?q=" + city + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        String cityName = response.getString("name");
                        String country = response.getJSONObject("sys").getString("country");

                        // Mostrar datos de la ciudad buscada
                        searchWeatherTextView.setText(
                                "Ciudad: " + cityName + ", " + country + "\n" +
                                        "Temp: " + temp + "°C"
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    searchWeatherTextView.setText("Ciudad no encontrada.");
                    progressBar.setVisibility(View.GONE);
                });

        Volley.newRequestQueue(this).add(request);
    }
}

