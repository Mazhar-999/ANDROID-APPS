package com.example.gpstrack;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location startLocation, endLocation;
    private TextView locationText, distanceText;
    private Button startButton, stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Map-related initialization
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Location tracking initialization
        locationText = findViewById(R.id.locationText);
        distanceText = findViewById(R.id.distanceText);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        startButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        });

        stopButton.setOnClickListener(v -> {
            locationManager.removeUpdates(this);
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
            if (endLocation != null && startLocation != null) {
                float distance = startLocation.distanceTo(endLocation) / 1000; // Convert meters to kilometers
                distanceText.setText("Distance: " + distance + " km");
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check location permission and enable MyLocation layer
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Request location permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }

        // Move the camera to a default location
        LatLng defaultLocation = new LatLng(-34, 151); // Example coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10)); // Zoom level 10
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (startLocation == null) {
            startLocation = location;
            locationText.setText("Starting Location: " + startLocation.getLatitude() + ", " + startLocation.getLongitude());
        } else {
            endLocation = location;
            locationText.setText("Current Location: " + endLocation.getLatitude() + ", " + endLocation.getLongitude());

            // Update map marker and camera position
            LatLng newLatLng = new LatLng(endLocation.getLatitude(), endLocation.getLongitude());
            mMap.clear(); // Clear previous markers
            mMap.addMarker(new MarkerOptions().position(newLatLng).title("Current Location")); // Add a new marker
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15)); // Zoom level can be adjusted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable MyLocation
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                startButton.performClick(); // Automatically start location updates
            } else {
                Toast.makeText(this, "Location permission is required to track your location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
