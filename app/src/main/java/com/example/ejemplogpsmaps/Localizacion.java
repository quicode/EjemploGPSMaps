package com.example.ejemplogpsmaps;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import io.socket.client.IO;
import io.socket.client.Socket;

import static android.widget.Toast.LENGTH_LONG;


public class Localizacion implements LocationListener {

    MainActivity mainActivity;
    TextView tvMensaje;
    private Socket socket;
    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity, TextView tvMensaje) {
        this.mainActivity = mainActivity;
        this.tvMensaje = tvMensaje;

    }

    @Override
    public void onLocationChanged(Location location) {
        // Este metodo se ejecuta cuando el GPS recibe nuevas coordenadas
        String texto = "Mi ubicación es: \n"
                + "Latitud = " + location.getLatitude() + "\n"
                + "Longitud = " + location.getLongitude();

        tvMensaje.setText(texto);
        double lat=location.getLatitude();
        double lon=location.getLongitude();
        mapa(location.getLatitude(), location.getLongitude());

        envia(lat,lon);
    }


    private void envia(double lat, double lon){
        try {
            socket = IO.socket("https://iotservice.azurewebsites.net/");
            //socket = IO.socket("http://192.168.10.55:8080");
            socket.connect();
            socket.emit("Connection", "user");
            Toast.makeText(mainActivity.getApplicationContext(),"cone",LENGTH_LONG).show();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            Toast.makeText(mainActivity.getApplicationContext(),e.toString(),LENGTH_LONG).show();
        }

            JSONObject json = new JSONObject();
        //String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Calendar c = Calendar.getInstance();
        String fabricante = Build.MANUFACTURER;
        String modelo = Build.MODEL;
        String user= fabricante+" "+modelo;
            try {

                //json.put("id", "11605328");
                json.put("user", user);
                json.put("latitud", lat);
                json.put("longitud", lon);
                //json.put("date",currentDateTimeString);

                int hour=(c.get(Calendar.HOUR_OF_DAY));
                String sDate = c.get(Calendar.YEAR) + "-"
                        + c.get(Calendar.MONTH)
                        + "-" + c.get(Calendar.DAY_OF_MONTH)
                        + " at " + hour
                        + ":" + c.get(Calendar.MINUTE);
                json.put("date",sDate);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(mainActivity.getApplicationContext(),e.toString(),LENGTH_LONG).show();
            }
        socket.emit("new-message", json);
        Toast.makeText(mainActivity.getApplicationContext(),json.toString(),LENGTH_LONG).show();

        }


    public void mapa(double lat, double lon) {
        // Fragment del Mapa
        FragmentMaps fragment = new FragmentMaps();

        Bundle bundle = new Bundle();
        bundle.putDouble("lat", new Double(lat));
        bundle.putDouble("lon", new Double(lon));
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getMainActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment, fragment, null);
        fragmentTransaction.commit();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("debug", "LocationProvider.AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        tvMensaje.setText("GPS Activado");
    }

    @Override
    public void onProviderDisabled(String provider) {
        tvMensaje.setText("GPS Desactivado");
    }
}
