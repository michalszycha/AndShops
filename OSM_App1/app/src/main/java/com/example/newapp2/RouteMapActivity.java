package com.example.newapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

import static com.example.newapp2.MainActivity.JSONShops;
import static com.example.newapp2.MainActivity.end_id;
import static com.example.newapp2.MainActivity.kategoria_map;
import static com.example.newapp2.MainActivity.position_latitude;
import static com.example.newapp2.MainActivity.position_longitude;

public class RouteMapActivity extends AppCompatActivity {

    public static MapView routemap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        routemap = (MapView) findViewById(R.id.routemap);
        routemap.setTileSource(TileSourceFactory.MAPNIK);

        //map.setBuiltInZoomControls(true);
        routemap.setMultiTouchControls(true);

        IMapController mapController = routemap.getController();
        mapController.setZoom(16.0);
        GeoPoint startPoint = new GeoPoint(position_latitude, position_longitude);
        mapController.setCenter(startPoint);

        Marker startMarker = new Marker(routemap);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        routemap.getOverlays().add(startMarker);
        startMarker.setTitle("Twoja pozycja!");

        drawRoute(kategoria_map);
    }


    public void drawRoute(int kategoria) {
        RoadManager roadManager = new OSRMRoadManager(this);
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        GeoPoint startPoint = new GeoPoint(position_latitude, position_longitude);
        waypoints.add(startPoint);

        Gson gson2 = new Gson();

        //GeoPoint endPoint = new GeoPoint(54.3934, 18.5817);
        for (int i = 0; i < JSONShops.size(); i++) {
            String jsonString = JSONShops.get(i).toString();
            MainActivity.Shops shop = gson2.fromJson(jsonString, MainActivity.Shops.class);
            if(i == end_id) {
                GeoPoint endPoint = new GeoPoint(shop.latitude,shop.longitude);
                waypoints.add(endPoint);

                Marker shopMarker = new Marker(routemap);
                shopMarker.setPosition(endPoint);
                if(kategoria == 0) {
                    shopMarker.setIcon(getResources().getDrawable(R.drawable.apteka));
                }
                if(kategoria == 1) {
                    shopMarker.setIcon(getResources().getDrawable(R.drawable.centrum_handlowe));
                }
                if(kategoria == 2) {
                    shopMarker.setIcon(getResources().getDrawable(R.drawable.piekarnia));
                }
                if(kategoria == 3) {
                    shopMarker.setIcon(getResources().getDrawable(R.drawable.spozywcze));
                }
                if(kategoria == 4) {
                    shopMarker.setIcon(getResources().getDrawable(R.drawable.monopolowy));
                }
                if(kategoria == 5) {
                    shopMarker.setIcon(getResources().getDrawable(R.drawable.supermarket));
                }
                if(kategoria == 6) {
                    shopMarker.setIcon(getResources().getDrawable(R.drawable.stacja_benzynowa));
                }
                //icons from https://www.flaticon.com
                shopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                routemap.getOverlays().add(shopMarker);
                if(shop.opening_hours.equals("brak danych")) {
                    shopMarker.setTitle(shop.name);
                }
                else {
                    shopMarker.setTitle(shop.name+"\n"+shop.opening_hours);
                }
            }
        }

        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        routemap.getOverlays().add(roadOverlay);
        routemap.invalidate();

    }
}
