package com.frannzg.tempsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "d8456d71b46efebe42d31d99f977cadc";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private TextView weatherTextView;
    private EditText cityInput;
    private Button searchButton;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherTextView = findViewById(R.id.weatherTextView);
        cityInput = findViewById(R.id.cityInput);
        searchButton = findViewById(R.id.searchButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener localización al iniciar la app
        getLocationAndFetchWeather();

        // Buscar por nombre de ciudad
        searchButton.setOnClickListener(v -> fetchWeatherByCity(cityInput.getText().toString()));
    }

    @SuppressLint("MissingPermission")
    private void getLocationAndFetchWeather() {
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
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
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

                        // Guardar la ciudad para notificaciones
                        saveCityForNotifications(cityName);

                        // Mostrar los datos
                        weatherTextView.setText("Ciudad: " + cityName + "\nTemp: " + temp + "°C");

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al obtener el clima", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }

    private void fetchWeatherByCity(String city) {
        if (city.isEmpty()) {
            Toast.makeText(this, "Introduce una ciudad", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "?q=" + city + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");

                        weatherTextView.setText("Ciudad: " + city + "\nTemp: " + temp + "°C");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error: Ciudad no encontrada", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }

    private void saveCityForNotifications(String city) {
        SharedPreferences sharedPreferences = getSharedPreferences("cities", MODE_PRIVATE);
        sharedPreferences.edit().putString("last_city", city).apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocationAndFetchWeather();
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
