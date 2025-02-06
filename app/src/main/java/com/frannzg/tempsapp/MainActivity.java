package com.frannzg.tempsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button currentWeatherButton = findViewById(R.id.currentWeatherButton);
        Button searchWeatherButton = findViewById(R.id.searchWeatherButton);

        // Navegar a la actividad de clima actual
        currentWeatherButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CurrentWeatherActivity.class);
            startActivity(intent);
        });

        // Navegar a la actividad de búsqueda por ciudad
        searchWeatherButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchCityWeatherActivity.class);
            startActivity(intent);
        });
    }
}
