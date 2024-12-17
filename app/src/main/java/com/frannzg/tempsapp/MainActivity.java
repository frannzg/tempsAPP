package com.frannzg.tempsapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "d8456d71b46efebe42d31d99f977cadc";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private TextView weatherTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enlazar el TextView del layout
        weatherTextView = findViewById(R.id.weatherTextView);

        // Llamar a la función para obtener el clima
        getWeatherData();
    }

    private void getWeatherData() {
        double latitude = 41.1469; // Latitud de Reus
        double longitude = 1.2444; // Longitud de Reus

        // Construir la URL con los parámetros
        String urlString = BASE_URL + "?lat=" + latitude + "&lon=" + longitude
                + "&units=metric&appid=" + API_KEY;

        // Log de la URL generada para depuración
        Log.d("API_URL", "URL: " + urlString);

        // Crear la solicitud usando Volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, urlString, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Extraer datos del JSON
                            JSONObject main = response.getJSONObject("main");
                            double temperature = main.getDouble("temp");

                            String weatherDescription = response.getJSONArray("weather")
                                    .getJSONObject(0)
                                    .getString("description");

                            // Actualizar el TextView con los datos del clima
                            weatherTextView.setText(
                                    "Temp: " + temperature + "°C\n"
                                            + "Description: " + weatherDescription
                            );

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Error: " + error.getMessage());
                        Toast.makeText(MainActivity.this, "Error al obtener el clima", Toast.LENGTH_SHORT).show();
                    }
                });

        // Añadir la solicitud a la cola de Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
