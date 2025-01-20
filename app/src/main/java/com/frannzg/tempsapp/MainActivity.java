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
        ProgressBar progressBar = findViewById(R.id.progressBar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener localización al iniciar la app
        getLocationAndFetchWeather(progressBar);

        // Buscar por nombre de ciudad
        searchButton.setOnClickListener(v -> fetchWeatherByCity(cityInput.getText().toString(), progressBar));
    }

    @SuppressLint("MissingPermission")
    private void getLocationAndFetchWeather(ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE); // Mostrar el ProgressBar mientras se carga

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                fetchWeather(location.getLatitude(), location.getLongitude(), progressBar);
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar si no se pudo obtener la ubicación
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al obtener ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar si hay un error
        });
    }

    private void fetchWeather(double latitude, double longitude, ProgressBar progressBar) {
        String url = BASE_URL + "?lat=" + latitude + "&lon=" + longitude +
                "&units=metric&appid=" + API_KEY;

        @SuppressLint("SetTextI18n") JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Obtener datos del JSON
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        double tempMin = main.getDouble("temp_min");
                        double tempMax = main.getDouble("temp_max");
                        int pressure = main.getInt("pressure");
                        int humidity = main.getInt("humidity");

                        String weatherMain = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("main");
                        String weatherDescription = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("description");

                        String cityName = response.getString("name");
                        String country = response.getJSONObject("sys").getString("country");

                        // Obtener el código del icono
                        String iconCode = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("icon");

                        // Construir la URL del icono
                        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                        Log.d("Icon URL", iconUrl); // Para depurar la URL del icono

                        // Mostrar el icono con Glide
                        ImageView weatherIcon = findViewById(R.id.weatherIcon);
                        Glide.with(MainActivity.this)
                                .load(iconUrl)
                                .into(weatherIcon);

                        // Guardar la ciudad para notificaciones
                        saveCityForNotifications(cityName);

                        // Mostrar los datos
                        weatherTextView.setText(
                                "Ciudad: " + cityName + ", " + country + "\n" +
                                        "Clima: " + weatherMain + " (" + weatherDescription + ")\n" +
                                        "Temp: " + temp + "°C\n" +
                                        "Mínima: " + tempMin + "°C | Máxima: " + tempMax + "°C\n" +
                                        "Presión: " + pressure + " hPa\n" +
                                        "Humedad: " + humidity + "%"
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar después de cargar los datos
                    }
                },
                error -> {
                    Toast.makeText(this, "Error al obtener el clima", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar si hay un error
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void fetchWeatherByCity(String city, ProgressBar progressBar) {
        if (city.isEmpty()) {
            Toast.makeText(this, "Introduce una ciudad", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "?q=" + city + "&units=metric&appid=" + API_KEY;

        @SuppressLint("SetTextI18n") JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Obtener datos del JSON
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        double tempMin = main.getDouble("temp_min");
                        double tempMax = main.getDouble("temp_max");
                        int pressure = main.getInt("pressure");
                        int humidity = main.getInt("humidity");

                        String weatherMain = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("main");
                        String weatherDescription = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("description");

                        String cityName = response.getString("name");
                        String country = response.getJSONObject("sys").getString("country");

                        // Obtener el código del icono
                        String iconCode = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("icon");

                        // Construir la URL del icono
                        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

                        // Mostrar el icono con Glide
                        ImageView weatherIcon = findViewById(R.id.weatherIcon);
                        Glide.with(MainActivity.this)
                                .load(iconUrl)
                                .into(weatherIcon);

                        // Mostrar los datos
                        weatherTextView.setText(
                                "Ciudad: " + cityName + ", " + country + "\n" +
                                        "Clima: " + weatherMain + " (" + weatherDescription + ")\n" +
                                        "Tiempo: " + temp + "°C\n" +
                                        "Mínima: " + tempMin + "°C | Máxima: " + tempMax + "°C\n" +
                                        "Presión: " + pressure + " hPa\n" +
                                        "Humedad: " + humidity + "%"
                        );

                    } catch (Exception e) {
                        Log.e("MainActivity", "Error al procesar los datos", e);
                        Toast.makeText(MainActivity.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar después de cargar los datos
                    }
                },
                error -> {
                    Toast.makeText(this, "Error: Ciudad no encontrada", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar si hay un error
                });

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
            getLocationAndFetchWeather(findViewById(R.id.progressBar));
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
