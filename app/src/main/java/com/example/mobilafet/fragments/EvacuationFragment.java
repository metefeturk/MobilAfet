package com.example.mobilafet.fragments;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mobilafet.R;
import com.example.mobilafet.models.EarthquakeResponse;
import com.example.mobilafet.models.FireHotspot;
import com.example.mobilafet.models.RouteRequest;
import com.example.mobilafet.models.RouteResponse;
import com.example.mobilafet.network.FirmsApiService;
import com.example.mobilafet.network.RetrofitClient;
import com.example.mobilafet.network.UsgsApiService;
import com.example.mobilafet.network.RouteApiService;
import com.example.mobilafet.parsers.CsvParser;
import com.example.mobilafet.utils.ApiConfig;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EvacuationFragment extends Fragment {

    private static final String TAG = "EvacuationFragment";
    private MapView mapView;
    private MaterialCardView infoCard;
    private TextView tvCardTitle, tvCardDesc;
    
    // Osmdroid Overlay Yönetimi (Filtreleme için kullanışlıdır)
    private FolderOverlay eqOverlay;
    private FolderOverlay fireOverlay;
    private FolderOverlay safeOverlay;
    private Polyline currentRoutePolyline; // Çizilen yol
    
    private MyLocationNewOverlay myLocationOverlay;
    private boolean notificationSent = false;

    // Konum izni istemcisi
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted && myLocationOverlay != null) {
                    myLocationOverlay.enableMyLocation();
                    myLocationOverlay.runOnFirstFix(() -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                GeoPoint myLoc = myLocationOverlay.getMyLocation();
                                if (myLoc != null) {
                                    mapView.getController().animateTo(myLoc);
                                    generateDynamicSafeZones(myLoc); // Gerçek konuma göre toplanma alanı yarat
                                }
                            });
                        }
                    });
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Osmdroid Configuration (En önemli kısım, haritanın çalışmasını sağlar)
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        View view = inflater.inflate(R.layout.fragment_evacuation, container, false);
        
        mapView = view.findViewById(R.id.map_view);
        infoCard = view.findViewById(R.id.card_map_info);
        tvCardTitle = view.findViewById(R.id.tv_map_card_title);
        tvCardDesc = view.findViewById(R.id.tv_map_card_desc);
        
        setupMap();
        setupFilters(view);
        checkPermissionsAndEnableLocation();
        
        return view;
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        
        IMapController mapController = mapView.getController();
        mapController.setZoom(12.0);
        // Konum bulunana kadar varsayılan olarak Kastamonu Merkez gösterilir
        GeoPoint defaultLocation = new GeoPoint(41.3888, 33.7827); 
        mapController.setCenter(defaultLocation);

        // Overlay klasörlerini başlat
        eqOverlay = new FolderOverlay();
        fireOverlay = new FolderOverlay();
        safeOverlay = new FolderOverlay();
        
        mapView.getOverlays().add(eqOverlay);
        mapView.getOverlays().add(fireOverlay);
        mapView.getOverlays().add(safeOverlay);

        // Haritaya tıklanınca bilgi kartını gizleme event'i
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                infoCard.setVisibility(View.GONE);
                return false;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) { return false; }
        };
        mapView.getOverlays().add(new MapEventsOverlay(mReceive));

        // Geçici (GPS bağlanana kadar) konum etrafında güvenli bölgeler oluştur
        generateDynamicSafeZones(defaultLocation);

        // API verilerini yükle
        loadEarthquakes();
        loadFires();
    }

    private void setupFilters(View view) {
        Chip chipAll = view.findViewById(R.id.chip_filter_all);
        Chip chipEq = view.findViewById(R.id.chip_filter_earthquake);
        Chip chipFire = view.findViewById(R.id.chip_filter_fire);
        Chip chipSafe = view.findViewById(R.id.chip_filter_safe);

        chipAll.setOnClickListener(v -> filterMarkers(true, true, true));
        chipEq.setOnClickListener(v -> filterMarkers(true, false, false));
        chipFire.setOnClickListener(v -> filterMarkers(false, true, false));
        chipSafe.setOnClickListener(v -> filterMarkers(false, false, true));
    }

    private void checkPermissionsAndEnableLocation() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        mapView.getOverlays().add(myLocationOverlay);

        List<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        perms.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myLocationOverlay.enableMyLocation();
        }
        locationPermissionRequest.launch(perms.toArray(new String[0]));
    }

    private void filterMarkers(boolean showEq, boolean showFire, boolean showSafe) {
        eqOverlay.setEnabled(showEq);
        fireOverlay.setEnabled(showFire);
        safeOverlay.setEnabled(showSafe);
        mapView.invalidate(); // Haritayı yeniden çiz
    }

    private void showMarkerInfo(String title, String snippet) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvCardTitle.setText(title);
                tvCardDesc.setText(snippet);
            infoCard.setVisibility(View.VISIBLE);
            });
        }
    }

    // Bulunulan GPS konumuna göre dinamik / simülasyon tahliye noktaları
    private void generateDynamicSafeZones(GeoPoint center) {
        safeOverlay.getItems().clear();
        addEvacuationMarker(new GeoPoint(center.getLatitude() + 0.005, center.getLongitude() + 0.008), "Kuzey Park Toplanma Alanı", "Kapasite: 500 kişi | Dokun ve yol çiz");
        addEvacuationMarker(new GeoPoint(center.getLatitude() - 0.006, center.getLongitude() + 0.004), "Güney Meydanı Toplanma Alanı", "Kapasite: 350 kişi | Dokun ve yol çiz");
        addEvacuationMarker(new GeoPoint(center.getLatitude() + 0.002, center.getLongitude() - 0.007), "Batı Stadyumu Tahliye Alanı", "Kapasite: 1200 kişi | Dokun ve yol çiz");
        if(getActivity() != null) getActivity().runOnUiThread(() -> mapView.invalidate());
    }

    private void addEvacuationMarker(GeoPoint point, String title, String snippet) {
        Marker m = new Marker(mapView);
        m.setPosition(point);
        m.setTitle(title);
        m.setSnippet(snippet);
        
        Drawable icon = ContextCompat.getDrawable(requireContext(), org.osmdroid.library.R.drawable.marker_default).mutate();
        icon.setTint(Color.parseColor("#4CAF50")); // Yeşil renk
        m.setIcon(icon);
        
        m.setOnMarkerClickListener((marker, mv) -> {
            showMarkerInfo(marker.getTitle(), marker.getSnippet());
            GeoPoint userLoc = myLocationOverlay.getMyLocation();
            if (userLoc == null) userLoc = new GeoPoint(41.3888, 33.7827); // GPS yoksa Kastamonu varsay
            loadRouteToSafeZone(userLoc, point, marker);
            return true;
        });
        safeOverlay.add(m);
    }

    private void loadEarthquakes() {
        UsgsApiService service = RetrofitClient.getClient(ApiConfig.USGS_BASE_URL).create(UsgsApiService.class);
        service.getRecentEarthquakes().enqueue(new Callback<EarthquakeResponse>() {
            @Override
            public void onResponse(Call<EarthquakeResponse> call, Response<EarthquakeResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().features != null) {
                    Drawable iconEq = ContextCompat.getDrawable(requireContext(), org.osmdroid.library.R.drawable.marker_default).mutate();
                    iconEq.setTint(Color.parseColor("#FF5252")); // Hafif Kırmızı (Deprem)

                    long currentTime = System.currentTimeMillis();

                    for (EarthquakeResponse.Feature f : response.body().features) {
                        if (f.geometry.coordinates.size() >= 2) {
                            double lon = f.geometry.coordinates.get(0);
                            double lat = f.geometry.coordinates.get(1);
                            
                            // TEST İÇİN TÜRKİYE FİLTRESİ DEVRE DIŞI BIRAKILDI (Tüm Dünya)
                            // if (lat >= 35.8 && lat <= 42.1 && lon >= 25.5 && lon <= 44.8) {
                                GeoPoint point = new GeoPoint(lat, lon);
                            Marker m = new Marker(mapView);
                            m.setPosition(point);
                            m.setTitle("Deprem: " + f.properties.place);
                                
                                // Saat ve Risk Hesaplama
                                long diff = currentTime - f.properties.time;
                                long hoursAgo = diff / (1000 * 60 * 60);
                                String risk = f.properties.mag >= 5.5 ? "Yüksek Risk" : (f.properties.mag >= 4.0 ? "Orta Risk" : "Düşük Risk");
                                
                                m.setSnippet("Şiddet: " + f.properties.mag + " (" + risk + ")\n" + hoursAgo + " saat önce bildirildi\nDokun ve yol çiz");
                                m.setIcon(iconEq);
                            m.setOnMarkerClickListener((marker, mv) -> {
                                showMarkerInfo(marker.getTitle(), marker.getSnippet());
                                    GeoPoint userLoc = myLocationOverlay.getMyLocation();
                                    if (userLoc == null) userLoc = new GeoPoint(41.3888, 33.7827); // GPS yoksa Kastamonu varsay
                                    loadRouteToSafeZone(userLoc, point, marker);
                                return true;
                            });
                            eqOverlay.add(m);
                                
                                checkAndSendNotification(point, f.properties.mag, f.properties.place, "Deprem");
                            // }
                        }
                    }
                    if(getActivity() != null) getActivity().runOnUiThread(() -> mapView.invalidate());
                }
            }
            @Override public void onFailure(Call<EarthquakeResponse> call, Throwable t) { Log.e(TAG, "Earthquake fetch error", t); }
        });
    }

    private void loadFires() {
        FirmsApiService service = RetrofitClient.getClient(ApiConfig.FIRMS_BASE_URL).create(FirmsApiService.class);
        service.getFireHotspots(ApiConfig.FIRMS_API_KEY).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        List<FireHotspot> hotspots = CsvParser.parseFirmsCsv(response.body());
                        if (getActivity() != null) {
                            Drawable iconFire = ContextCompat.getDrawable(requireContext(), org.osmdroid.library.R.drawable.marker_default).mutate();
                            iconFire.setTint(Color.parseColor("#FF9800")); // Turuncu (Yangın)
                            
                            getActivity().runOnUiThread(() -> {
                                int limit = Math.min(150, hotspots.size());
                                for (int i = 0; i < limit; i++) {
                                    FireHotspot f = hotspots.get(i);
                                    // TEST İÇİN TÜRKİYE FİLTRESİ DEVRE DIŞI BIRAKILDI (Tüm Dünya)
                                    // if (f.latitude >= 35.8 && f.latitude <= 42.1 && f.longitude >= 25.5 && f.longitude <= 44.8) {
                                        GeoPoint point = new GeoPoint(f.latitude, f.longitude);
                                        Marker m = new Marker(mapView);
                                        m.setPosition(point);
                                        m.setTitle("Yangın Riski Noktası");
                                        
                                        String risk = f.brightness >= 330 ? "Yüksek Risk" : (f.brightness >= 310 ? "Orta Risk" : "Düşük Risk");
                                        m.setSnippet("Parlaklık: " + f.brightness + " (" + risk + ")\nSon 24 saat içinde bildirildi\nDokun ve yol çiz");
                                        m.setIcon(iconFire);
                                        m.setOnMarkerClickListener((marker, mv) -> {
                                            showMarkerInfo(marker.getTitle(), marker.getSnippet());
                                            GeoPoint userLoc = myLocationOverlay.getMyLocation();
                                            if (userLoc == null) userLoc = new GeoPoint(41.3888, 33.7827); // GPS yoksa Kastamonu
                                            loadRouteToSafeZone(userLoc, point, marker);
                                            return true;
                                        });
                                        fireOverlay.add(m);
                                        
                                        checkAndSendNotification(point, f.brightness, "Bölgesel Yangın", "Yangın");
                                    // }
                                }
                                mapView.invalidate();
                            });
                        }
                    }).start();
                }
            }
            @Override public void onFailure(Call<String> call, Throwable t) { Log.e(TAG, "Fire fetch error", t); }
        });
    }

    private void loadRouteToSafeZone(GeoPoint origin, GeoPoint destination, Marker targetMarker) {
        RouteApiService service = RetrofitClient.getClient(ApiConfig.OPENROUTESERVICE_BASE_URL).create(RouteApiService.class);
        RouteRequest request = new RouteRequest(Arrays.asList(
                Arrays.asList(origin.getLongitude(), origin.getLatitude()),
                Arrays.asList(destination.getLongitude(), destination.getLatitude())
        ));
        
        service.getDirections(ApiConfig.OPENROUTESERVICE_API_KEY, request).enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().routes.isEmpty()) {
                    RouteResponse.Summary summary = response.body().routes.get(0).summary;
                    double distanceKm = summary.distance / 1000.0;
                    double durationMin = summary.duration / 60.0;
                    
                    String geom = response.body().routes.get(0).geometry;
                    List<GeoPoint> routePoints = decodePolyline(geom);
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (currentRoutePolyline != null) {
                                mapView.getOverlays().remove(currentRoutePolyline);
                            }
                            currentRoutePolyline = new Polyline();
                            currentRoutePolyline.setPoints(routePoints);
                            currentRoutePolyline.setColor(Color.parseColor("#2196F3")); // Mavi yol çizgisi
                            currentRoutePolyline.setWidth(12f);
                            mapView.getOverlays().add(currentRoutePolyline);
                            
                            targetMarker.setSnippet("Uzaklık: " + String.format("%.1f", distanceKm) + " km | Süre: " + String.format("%.0f", durationMin) + " dk\nYol haritada çizildi.");
                            showMarkerInfo(targetMarker.getTitle(), targetMarker.getSnippet());
                            mapView.invalidate();
                        });
                    }
                }
            }
            @Override public void onFailure(Call<RouteResponse> call, Throwable t) { Log.e(TAG, "Route fetch error", t); }
        });
    }

    // API'den gelen kodlanmış rotayı haritada çizilecek koordinatlara çevirir (Polyline Decoder)
    private List<GeoPoint> decodePolyline(String encoded) {
        List<GeoPoint> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20 && index < len);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0; result = 0;
            if (index >= len) break;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20 && index < len);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            poly.add(new GeoPoint((lat / 100000.0), (lng / 100000.0)));
        }
        return poly;
    }

    // Afet yakınlarda ve belli bir riskin üzerindeyse bildirim atar
    private void checkAndSendNotification(GeoPoint eventPoint, double value, String place, String type) {
        if (notificationSent) return; // SPAM önlemek için sadece ilk tehlikede atar

        GeoPoint userLoc = myLocationOverlay.getMyLocation();
        if (userLoc == null) userLoc = new GeoPoint(41.3888, 33.7827); // GPS yoksa Kastamonu varsay
        
        double distanceMeters = eventPoint.distanceToAsDouble(userLoc);
        double distanceKm = distanceMeters / 1000.0;
        
        // 400 km içindeki her türlü 3.0+ deprem veya 310+ yangın için bildirim at (Test edilebilirliği artırır)
        boolean isDangerous = (type.equals("Deprem") && value >= 3.0) || (type.equals("Yangın") && value >= 310);

        if (distanceKm <= 400 && isDangerous) {
            notificationSent = true;
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
            }

            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "afet_alert_channel";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Afet Uyarıları", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
            
            String title = type.equals("Deprem") ? "Yakınlarda Deprem!" : "Yakınlarda Yangın Riski!";
            String text = place + " (" + String.format("%.0f", distanceKm) + " km uzağınızda) " + type + " raporlandı.";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true);
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}