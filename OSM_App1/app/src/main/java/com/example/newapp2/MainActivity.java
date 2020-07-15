//template app which provides gps coordinates


package com.example.newapp2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Double.parseDouble;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private LocationManager lm;
    private LocationListener listener;
    private TextView coor_txt;
    private TextView textView;
    private Button button;
    private Button button2;

    public static double position_latitude;
    public static double position_longitude;

    public static double point1_lat;
    public static double point1_lon;
    public static double point2_lat;
    public static double point2_lon;

    public static double range_lat = 0.0045;
    public static double range_lon = range_lat*2;

    public static double end_id;

    private String kategoria;
    public static int kategoria_map;

    //spinner
    private Spinner spinner;
    private static final String[] paths = {"Apteka", "Centrum handlowe", "Piekarnia", "Spożywczy", "Sklep monopolowy", "Supermarket", "Stacja benzynowa"};

    public Gson gson;

    public static ArrayList JSONShops;

    //table
    private TableLayout table;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        coor_txt = findViewById(R.id.coor);
        textView = findViewById(R.id.textView);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new MyListener();

        registerListener();

        //button for searching
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getObjects(position_latitude,position_longitude);
                //fill_table(JSONShops);
            }
        });

        //button for map
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        //spinner
        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String>SpinnerAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item,paths);

        SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(SpinnerAdapter);
        //spinner.setPrompt("Select club");
        spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);

        //create gson to parse class to JSON
        gson = new Gson();
        //create list of JSON objects
        JSONShops = new ArrayList();

        //initialize table
        table = findViewById(R.id.tableLayout);
        initTable();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){
        registerListener();
    }

    @SuppressLint("MissingPermission")
    void registerListener() {
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    }

    //listener for changing location
    public class MyListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            przetwarzajLokalizacje(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider){

        }

        @Override
        public void onProviderDisabled(String provider){

        }

        public void przetwarzajLokalizacje(Location location) {
            String info;
            position_latitude = location.getLatitude();
            position_longitude = location.getLongitude();
            info = "\nLatitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude();
            coor_txt.setText(info);
        }
    }

    //class with shops datas
    public static class Shops {
        String name;
        String road;
        String road_nr;
        double latitude;
        double longitude;
        String opening_hours;

        public Shops( double latitude, double longitude, String name, String road, String road_nr, String opening_hours) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.road = road;
            this.road_nr = road_nr;
            this.opening_hours = opening_hours;
        }
    }

    //function with retrofit
    public void getObjects(double current_latitude, double current_longitude) {
        JSONShops.clear();
        table.removeAllViews();
        initTable();
        try {
            MapActivity.map.getOverlays().clear();
            Log.d("map","clear");
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d("map","clear_nope");
        }

        point1_lat = current_latitude - range_lat;
        point1_lon = current_longitude - range_lon;
        point2_lat = current_latitude + range_lat;
        point2_lon = current_longitude + range_lon;

        String viewbox = point1_lon+","+point1_lat+","+point2_lon+","+point2_lat;

        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        Map<String, String> data = new HashMap<>();

        //new path: https://nominatim.openstreetmap.org/?addressdetails=1&namedetails=1&extratags=1&q=[Supermarket]&format=json&limit=50&viewbox=18.6054728,54.3831255,18.6107781,54.3816183&bounded=1
        data.put("addressdetails", String.valueOf(1));
        data.put("namedetails", String.valueOf(1));
        data.put("extratags", String.valueOf(1));
        data.put("q", kategoria);
        data.put("format", "json");
        data.put("limit", "100");
        data.put("viewbox", viewbox);
        data.put("bounded", String.valueOf(1));

        Call<List<Data>> call = service.getData(data);
        call.enqueue(new Callback<List<Data>>() {
            @Override
            public void onResponse(Call<List<Data>> call, Response<List<Data>> response) {
                String info = "";
                Log.d("tak", "dziala");
                try {
                    Log.d("ile", String.valueOf(response.body().size()));
                    for (int i = 0; i < response.body().size(); i++) {
                        try {
                            String name = response.body().get(i).namedetails.name;
                            String road = response.body().get(i).address.road;
                            String house_number = response.body().get(i).address.house_number;
                            String lat = response.body().get(i).lat;
                            String lon = response.body().get(i).lon;
                            String opening_hours = response.body().get(i).extratags.opening_hours;

                            if (name == null) {
                                name = "brak danych";
                            }
                            if (road == null) {
                                road = "brak danych";
                            }
                            if (house_number == null) {
                                house_number = "brak danych";
                            }
                            if (opening_hours == null) {
                                opening_hours = "brak danych";
                            }
                            //info += i + ") " + name + " " + road + " " + house_number + " " + " " + lat + " " + lon + "\n";
                            //textView.setText(info);
                            double distance = getDistance(position_latitude, position_longitude, parseDouble(lat), parseDouble(lon));

                            Shops shop = new Shops(parseDouble(lat),parseDouble(lon),name,road,house_number,opening_hours);
                            String jsonshop = gson.toJson(shop);
                            JSONShops.add(jsonshop);
                            //Log.d("json",jsonshop);
                            fillTable(i, name, road, house_number, opening_hours, distance);
                            //Log.d("fill",String.valueOf(i));
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                    if (response.body().size() == 0) {
                        textView.setText("brak takich sklepów");
                    }
                } catch (Exception e) {
                    //
                }
            }
            @Override
            public void onFailure(Call<List<Data>> call, Throwable t) {
                Log.d("nie", String.valueOf(t));
            }
        });
    }

    //spinner list
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        switch (position) {
            //apteka
            case 0:
                //spożywcze
                kategoria = "["+paths[0]+"]";
                kategoria_map = 0;
                break;
            case 1:
                //centrum handlowe
                kategoria = "["+paths[1]+"]";
                kategoria_map = 1;
                break;
            case 2:
                //piekarnia
                kategoria = "["+paths[2]+"]";
                kategoria_map = 2;
                break;
            case 3:
                //spożywcze
                kategoria = "["+paths[3]+"]";
                kategoria_map = 3;
                break;
            case 4:
                //monopolowy
                kategoria = "["+paths[4]+"]";
                kategoria_map = 4;
                break;
            case 5:
                //supermarket
                kategoria = "["+paths[5]+"]";
                kategoria_map = 5;
                break;
            case 6:
                //stacja benzynowa
                kategoria = "["+paths[6]+"]";
                kategoria_map = 6;
                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

    //opening MapActivity
    public void openMap() {
        Intent intentMap = new Intent(this, MapActivity.class);
        startActivity(intentMap);
    }

    //opening MapActivity
    public void openRouteMap() {
        Intent intentMap = new Intent(this, RouteMapActivity.class);
        startActivity(intentMap);
    }

    //init table's first row
    public void initTable() {
        TableRow tbrow0 = new TableRow(this);
        TextView tv0 = new TextView(this);
        tv0.setText("Nazwa");
        tv0.setTextColor(Color.BLACK);
        tv0.setWidth(300);
        tbrow0.addView(tv0);
        TextView tv1 = new TextView(this);
        tv1.setText("Ulica");
        tv1.setTextColor(Color.BLACK);
        tv1.setWidth(300);
        tbrow0.addView(tv1);
        TextView tv2 = new TextView(this);
        tv2.setText("Nr");
        tv2.setTextColor(Color.BLACK);
        tv2.setWidth(150);
        tbrow0.addView(tv2);
        TextView tv3 = new TextView(this);
        tv3.setText("Godziny otwarcia");
        tv3.setTextColor(Color.BLACK);
        tv3.setWidth(300);
        tbrow0.addView(tv3);
        TextView tv4 = new TextView(this);
        tv4.setText("Odleglosc");
        tv4.setTextColor(Color.BLACK);
        tv4.setWidth(300);
        tbrow0.addView(tv4);

        table.addView(tbrow0);
    }
    //add to table datas about shops
    public void fillTable(int id, String name, String address, String address_nr, String opening_hours, double distance) {
        final TableRow shop_row = new TableRow(this);
        shop_row.setId(id);
        //name
        /*
        TextView tv0 = new TextView(this);
        tv0.setText(name);
        tv0.setTextColor(Color.BLACK);
        if(name == "brak danych") {
            tv0.setTextColor(Color.RED);
        }
        tv0.setWidth(300);
        shop_row.addView(tv0);
        */
        Button bt0 = new Button(this);
        bt0.setText(name);
        bt0.setWidth(300);
        bt0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end_id = shop_row.getId();
                Log.d("shop",String.valueOf(end_id));
                openRouteMap();
            }
        });
        shop_row.addView(bt0);
        //address
        TextView tv1 = new TextView(this);
        tv1.setText(address);
        tv1.setTextColor(Color.BLACK);
        if(address == "brak danych") {
            tv1.setTextColor(Color.RED);
        }
        tv1.setWidth(300);
        shop_row.addView(tv1);
        //address_nr
        TextView tv2 = new TextView(this);
        tv2.setText(address_nr);
        tv2.setTextColor(Color.BLACK);
        if(address_nr == "brak danych") {
            tv2.setTextColor(Color.RED);
        }
        tv2.setWidth(150);
        shop_row.addView(tv2);
        //opening hours
        TextView tv3 = new TextView(this);
        tv3.setText(opening_hours);
        tv3.setTextColor(Color.BLACK);
        if(opening_hours == "brak danych") {
            tv3.setTextColor(Color.RED);
        }
        tv3.setWidth(300);
        shop_row.addView(tv3);
        //distance
        distance = distance*1000.0;
        TextView tv4 = new TextView(this);
        tv4.setText(String.format("%.3f", distance)+"m");
        tv4.setTextColor(Color.BLACK);
        tv4.setWidth(300);
        shop_row.addView(tv4);
        table.addView(shop_row);
    }

    //calc distance between user and shop
    double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }
    double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371;

        double dLat = degreesToRadians(lat2-lat1);
        double dLon = degreesToRadians(lon2-lon1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadiusKm * c;
    }

}


