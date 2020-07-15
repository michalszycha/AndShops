package com.example.newapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

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

import static com.example.newapp2.MainActivity.*;

public class MapActivity extends AppCompatActivity {

    public static MapView map = null;
    public Gson gson;

    public static ArrayList PolyPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        setContentView(R.layout.activity_map);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        //map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(16.0);
        GeoPoint startPoint = new GeoPoint(position_latitude, position_longitude);
        mapController.setCenter(startPoint);

        PolyPoints = new <GeoPoint> ArrayList();

        //add markers
        createMarkers(kategoria_map);

        //draw range of searching
        createPoints();
        drawRange();

        //drawRoute();

        //gson
        gson = new Gson();
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public void createMarkers(int kategoria) {

        //ArrayList shopsPoints = new ArrayList<GeoPoint>();
        //ArrayList shopMarkers = new ArrayList<Marker>();

        Marker startMarker = new Marker(map);
        GeoPoint startPoint = new GeoPoint(position_latitude, position_longitude);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        startMarker.setTitle("Twoja pozycja!");

        Gson gson2 = new Gson();

        Log.d("json_size_mapa",String.valueOf(JSONShops.size()));

        for (int i = 0; i < JSONShops.size(); i++) {
            String jsonString = JSONShops.get(i).toString();
            Shops shop = gson2.fromJson(jsonString, Shops.class);
            GeoPoint shopPoint = new GeoPoint(shop.latitude,shop.longitude);
            Marker shopMarker = new Marker(map);
            shopMarker.setPosition(shopPoint);
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
            map.getOverlays().add(shopMarker);
            if(shop.opening_hours.equals("brak danych")) {
                shopMarker.setTitle(shop.name);
            }
            else {
                shopMarker.setTitle(shop.name+"\n"+shop.opening_hours);
            }
        }
    }

    public void createPoints() {
        GeoPoint Point1 = new GeoPoint(position_latitude+range_lat,position_longitude+range_lon);
        PolyPoints.add(Point1);
        GeoPoint Point2 = new GeoPoint(position_latitude+range_lat,position_longitude-range_lon);
        PolyPoints.add(Point2);
        GeoPoint Point3 = new GeoPoint(position_latitude-range_lat,position_longitude-range_lon);
        PolyPoints.add(Point3);
        GeoPoint Point4 = new GeoPoint(position_latitude-range_lat,position_longitude+range_lon);
        PolyPoints.add(Point4);
        GeoPoint Point5 = Point1;
        PolyPoints.add(Point5);
    }

    public void drawRange() {
        Polyline line = new Polyline();   //see note below!
        line.setColor(Color.MAGENTA);
        line.setWidth(1.5f);
        line.setPoints(PolyPoints);
        map.getOverlayManager().add(line);
    }

    public void drawRoute() {
        RoadManager roadManager = new OSRMRoadManager(this);
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        GeoPoint startPoint = new GeoPoint(position_latitude, position_longitude);
        waypoints.add(startPoint);

        GeoPoint endPoint = new GeoPoint(54.3934, 18.5817);
        waypoints.add(endPoint);

        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        map.getOverlays().add(roadOverlay);
        //map.invalidate();

    }
}
