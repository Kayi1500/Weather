package com.example.lenovo.weatherapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;


public class MainActivity extends AppCompatActivity {

    //Deklarerar medlemsvariabler
    private Button btn;
    private LocationManager locationM;
    private LocationListener locationL;
    private String symbol;
    public static TextView viewerTemp;
    public static TextView viewerCloud;
    public static TextView viewerWind;
    public static TextView viewerRain;
    public static TextView stad;
    public static ImageView bild;
    public static double longi;
    public static double lati;

    public void getWeatherImg(){
        bild = (ImageView) findViewById(R.id.imageView);
        String url = "https://api.met.no/weatherapi/weathericon/1.1?content_type=image%2Fpng&symbol="+getSymbol();
        Picasso.with(this).load(url).into(bild);
    }
    public String getSymbol(){
        return symbol;
    }

    public void setSymbol(String symbol){
        this.symbol = symbol;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.updateWeather);
        viewerTemp = (TextView) findViewById(R.id.tempText);
        viewerCloud = (TextView) findViewById(R.id.cloudText);
        viewerWind = (TextView) findViewById(R.id.windText);
        viewerRain = (TextView) findViewById(R.id.rainText);
        stad = (TextView) findViewById(R.id.stadText);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);

        locationM = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location initial = locationM.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longi = initial.getLongitude();
        lati = initial.getLatitude();
        locationL = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longi = location.getLongitude();
                lati = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationL);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lati, longi, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String cityName = addresses.get(0).getAddressLine(0);
        stad.setText(cityName);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(API.base_url)
                        .addConverterFactory(SimpleXmlConverterFactory.create())
                        .build();

                API api = retrofit.create(API.class);

                Call<ResponseBody> call = api.getData("https://api.met.no/weatherapi/locationforecast/1.9/?lat="+lati+"&lon="+longi+"");
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String XMLString=  response.body().string();
                            Document doc = DocumentBuilderFactory.newInstance()
                                    .newDocumentBuilder()
                                    .parse(new InputSource(new StringReader(XMLString)));

                            NodeList nodListaTemp = doc.getElementsByTagName("temperature");
                            NodeList nodListaCloud = doc.getElementsByTagName("cloudiness");
                            NodeList nodListaWind = doc.getElementsByTagName("windSpeed");
                            NodeList nodListaRain = doc.getElementsByTagName("humidity");
                            NodeList nodListaIMG = doc.getElementsByTagName("symbol");

                            Node nodTemp = nodListaTemp.item(0);
                            Element e = (Element)nodTemp;
                            String temp = e.getAttribute("value").toString();

                            Node nodCloud = nodListaCloud.item(0);
                            Element e2 = (Element)nodCloud;
                            String cloud = e2.getAttribute("percent").toString();

                            Node nodWind = nodListaWind.item(0);
                            Element e3 = (Element)nodWind;
                            String wind = e3.getAttribute("mps").toString();

                            Node nodRain = nodListaRain.item(0);
                            Element e4 = (Element)nodRain;
                            String rain = e4.getAttribute("value").toString();

                            Node nodIMG =  nodListaIMG.item(0);
                            Element img = (Element)nodIMG;
                            symbol = img.getAttribute("number").toString();
                            setSymbol(symbol);
                            getWeatherImg();


                            viewerTemp.setText("Det är: "+temp+" Grader Celcius");
                            viewerCloud.setText("Det är: "+cloud+ " % molnigt");
                            viewerWind.setText("Det blåser: "+wind+" (Miles per second)");
                            viewerRain.setText("Det är: "+rain+ " Fuktigt (i procent)");

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });

            }
        });
    }

}


