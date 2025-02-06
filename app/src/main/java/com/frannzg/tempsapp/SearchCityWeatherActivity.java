package com.frannzg.tempsapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

public class SearchCityWeatherActivity extends AppCompatActivity {

    private static final String API_KEY = "47d956df6f27f12f28d3c273e7a6028b";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String ICON_URL = "https://openweathermap.org/img/wn/";

    private EditText cityInput;
    private Button searchButton;
    private TextView searchWeatherTextView;
    private ProgressBar searchProgressBar;
    private ImageView weatherIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city_weather);

        // Enlazar vistas
        cityInput = findViewById(R.id.cityInput);
        searchButton = findViewById(R.id.searchButton);
        searchWeatherTextView = findViewById(R.id.searchWeatherTextView);
        searchProgressBar = findViewById(R.id.searchProgressBar);
        weatherIcon = findViewById(R.id.weatherIcon);

        // Configurar botón de búsqueda
        searchButton.setOnClickListener(v -> {
            String city = cityInput.getText().toString().trim();
            if (city.isEmpty()) {
                searchWeatherTextView.setText("Por favor, introduce una ciudad.");
            } else {
                fetchWeatherByCity(city);
            }
        });
    }

    private void fetchWeatherByCity(String city) {
        searchProgressBar.setVisibility(View.VISIBLE);
        searchWeatherTextView.setText("");

        String url = BASE_URL + "?q=" + city + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Obtener datos del clima
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        double feelsLike = main.getDouble("feels_like");
                        double tempMin = main.getDouble("temp_min");
                        double tempMax = main.getDouble("temp_max");
                        int pressure = main.getInt("pressure");
                        int humidity = main.getInt("humidity");

                        String cityName = response.getString("name");
                        String country = response.getJSONObject("sys").getString("country");

                        // Obtener el ícono del clima
                        String icon = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("icon");

                        String weatherDescription = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("description");

                        // Actualizar vista con los datos
                        searchWeatherTextView.setText(
                                "Ciudad: " + cityName + ", " + country + "\n" +
                                        "Descripción: " + weatherDescription + "\n" +
                                        "Temperatura: " + temp + "°C\n" +
                                        "Sensación térmica: " + feelsLike + "°C\n" +
                                        "Temp. mínima: " + tempMin + "°C\n" +
                                        "Temp. máxima: " + tempMax + "°C\n" +
                                        "Presión: " + pressure + " hPa\n" +
                                        "Humedad: " + humidity + "%"
                        );

                        // Cargar el ícono del clima
                        Glide.with(this)
                                .load(ICON_URL + icon + "@2x.png")
                                .into(weatherIcon);

                    } catch (Exception e) {
                        Toast.makeText(this, "Error al procesar los datos.", Toast.LENGTH_SHORT).show();
                    } finally {
                        searchProgressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    searchWeatherTextView.setText("Ciudad no encontrada.");
                    searchProgressBar.setVisibility(View.GONE);
                });

        Volley.newRequestQueue(this).add(request);
    }
}
