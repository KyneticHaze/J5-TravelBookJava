package com.furkanharmanci.travelbookjava.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.furkanharmanci.travelbookjava.R;
import com.furkanharmanci.travelbookjava.model.Place;
import com.furkanharmanci.travelbookjava.roomdb.PlaceDao;
import com.furkanharmanci.travelbookjava.roomdb.PlaceDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.furkanharmanci.travelbookjava.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;
    ActivityResultLauncher<String> permissionLauncher;
    SharedPreferences sharedPreferences;
    boolean info;
    //Room
    PlaceDatabase db;
    PlaceDao placeDao;
    Double markedLatitude;
    Double markedLongitude;
    // Kullan-at (disposable) thread
    CompositeDisposable disposable = new CompositeDisposable();
    Place selectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        resultLauncher();
        sharedPreferences = MapsActivity.this.getSharedPreferences("com.furkanharmanci.travelbookjava", MODE_PRIVATE);
        info = false;

        //database atıf yapıldı
        db = Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"places").build();
        placeDao = db.placeDao();

        // Varsayılan enlem boylam, proje açılınca sıfır olsun
        markedLatitude = 0.0;
        markedLongitude = 0.0;
        binding.save.setEnabled(false);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        //uzun tıklama dinlemesini burada set ettik. Alttaki methodda da işlevlendireceğiz
        mMap.setOnMapLongClickListener(this);
        //buton pasif

        Intent intent  = getIntent();
        String intentInfo = intent.getStringExtra("info"); // new / old

        if(intentInfo.equals("new")) {
            binding.save.setVisibility(View.VISIBLE);
            binding.delete.setVisibility(View.GONE);
            //Konum yönetimi ve dinlemesi
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    info = sharedPreferences.getBoolean("info",false);
                    if (!info) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("info", true).apply();
                    }
                }
            };

            // İzin kontrolü
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // izin verilmemişse
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // request permission
                    Snackbar.make(binding.getRoot(), "Permission needed for maps", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();
                } else {
                    // request permission again
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            } else {
                // izin daha önce verilmişse
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                // son bilinen konum
                Location lastKnownUserLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownUserLocation != null) {
                    // bir konum geliyorsa
                    LatLng userLocation = new LatLng(lastKnownUserLocation.getLatitude(), lastKnownUserLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }

                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.clear();
            selectedPlace = (Place) intent.getSerializableExtra("place");
            LatLng latLng = new LatLng(selectedPlace.latitude, selectedPlace.longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

            binding.placeText.setText(selectedPlace.placeName);
            binding.save.setVisibility(View.GONE);
            binding.delete.setVisibility(View.VISIBLE);
        }
    }

    public void resultLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    if (ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //izin verilmişse

                        //Konum güncellemesi
                        //minTimeMs => kaç ms'de bir konum güncellemesi yapacağını gösterir. minDistanceM => Metre cinsinden konuma mesafesini gösterir.
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                        // son bilinen konum
                        Location lastKnownUserLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastKnownUserLocation != null) {
                            LatLng userLocation = new LatLng(lastKnownUserLocation.getLatitude(),lastKnownUserLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Permission Needed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Your Sign"));

        markedLatitude = latLng.latitude;
        markedLongitude = latLng.longitude;

        // buton aktif
        binding.save.setEnabled(true);
    }

    public void saveUserLocation(View v) {
        Place userLocationPlace = new Place(binding.placeText.getText().toString(),markedLatitude,markedLongitude);

        // kullan-at(disposable) threading işlemi
        disposable.add(placeDao.insert(userLocationPlace)
                .subscribeOn(Schedulers.io()) // işlemi io threadde yap
                .observeOn(AndroidSchedulers.mainThread()) // yapılan işlemi main(UI) threadde gözlemle
                .subscribe(MapsActivity.this::handleResponse) // handleResponse'u buraya referans verdik
        );
    }

    public void handleResponse() {
        // gelen cevabı ele alma methodu
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Tüm aktiviteleri kapat
        startActivity(intent);
    }

    public void deleteUserLocation(View v) {

        disposable.add(placeDao.delete(selectedPlace)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}