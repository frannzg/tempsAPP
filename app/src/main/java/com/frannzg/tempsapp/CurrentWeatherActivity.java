package com.frannzg.tempsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

public class CurrentWeatherActivity extends AppCompatActivity {

    private static final String API_KEY = "47d956df6f27f12f28d3c273e7a6028b";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String ICON_URL = "https://openweathermap.org/img/wn/";

    private TextView currentWeatherTextView;
    private ProgressBar progressBar;
    private ImageView weatherIcon;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);

        currentWeatherTextView = findViewById(R.id.currentWeatherTextView);
        progressBar = findViewById(R.id.progressBar);
        weatherIcon = findViewById(R.id.weatherIcon);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocationAndFetchWeather();
    }

    @SuppressLint("MissingPermission")
    private void getLocationAndFetchWeather() {
        progressBar.setVisibility(View.VISIBLE);

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
                Log.e("Location", "Última ubicación conocida no disponible. Solicitando una nueva...");
                requestNewLocation();
            }
        }).addOnFailureListener(e -> {
            Log.e("Location", "Error al obtener la ubicación: " + e.getMessage());
            currentWeatherTextView.setText("Error al obtener la ubicación.");
            progressBar.setVisibility(View.GONE);
        });
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                fusedLocationClient.removeLocationUpdates(this);

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    fetchWeather(location.getLatitude(), location.getLongitude());
                } else {
                    Log.e("Location", "No se pudo obtener una nueva ubicación.");
                    currentWeatherTextView.setText("No se pudo obtener la ubicación. Intenta nuevamente.");
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, getMainLooper());
    }

    private void fetchWeather(double latitude, double longitude) {
        String url = BASE_URL + "?lat=" + latitude + "&lon=" + longitude +
                "&units=metric&appid=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        double feelsLike = main.getDouble("feels_like");
                        double tempMin = main.getDouble("temp_min");
                        double tempMax = main.getDouble("temp_max");
                        int pressure = main.getInt("pressure");
                        int humidity = main.getInt("humidity");
                        String cityName = response.getString("name");
                        String country = response.getJSONObject("sys").getString("country");

                        String icon = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("icon");

                        currentWeatherTextView.setText(
                                "Ciudad: " + cityName + ", " + country + "\n" +
                                        "Temperatura: " + temp + "°C\n" +
                                        "Sensación térmica: " + feelsLike + "°C\n" +
                                        "Temp. mínima: " + tempMin + "°C\n" +
                                        "Temp. máxima: " + tempMax + "°C\n" +
                                        "Presión: " + pressure + " hPa\n" +
                                        "Humedad: " + humidity + "%"
                        );

                        Glide.with(this)
                                .load(ICON_URL + icon + "@2x.png")
                                .into(weatherIcon);

                    } catch (Exception e) {
                        Toast.makeText(this, "Error al procesar los datos del clima.", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    Log.e("WeatherAPI", "Error al obtener el clima: " + error.getMessage());
                    currentWeatherTextView.setText("Error al obtener el clima. Verifica tu conexión a internet.");
                    progressBar.setVisibility(View.GONE);
                });

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchWeather();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
