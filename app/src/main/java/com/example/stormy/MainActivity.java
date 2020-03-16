package com.example.stormy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stormy.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static final String TAG=MainActivity.class.getSimpleName();

   final double latitude = 37.8267;
   final double longitude = -122.4233;

    private ImageView iconImageView;
private CurrentWeather currentWeather;
    private String Tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getForeCast(latitude,longitude);

    }

    private void getForeCast(double latitude, double longitude) {
        final ActivityMainBinding binding= DataBindingUtil.setContentView(MainActivity.this,R.layout.activity_main);

        iconImageView=findViewById(R.id.iconImageView);
        TextView darkSky=(TextView)findViewById(R.id.PoweredByValue);

        darkSky.setMovementMethod(LinkMovementMethod.getInstance() );

        String apiKey = "84d2d3d1fbae1af3830d2d546321d4bf";

        String forecastURL = "https://api.darksky.net/forecast/" + apiKey + "/" + latitude + "," + longitude;

        if (isNetworkAvailable())
        {
            OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(forecastURL)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {


                try {
                    String jsonData= response.body().string();
                    Log.v(TAG,jsonData);
                    if (response.isSuccessful()) {

                        currentWeather=getCurrentDetails(jsonData);

                       final CurrentWeather displayWeather=new CurrentWeather(
                                currentWeather.getLocationLabel(),
                                currentWeather.getIcon(),
                                currentWeather.getSummary(),
                                currentWeather.getTime(),
                                currentWeather.getTemperature(),
                                currentWeather.getHumidity(),
                                currentWeather.getPrecipChance(),
                                currentWeather.getTimeZone()
                        );

                        binding.setWeather(displayWeather);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Drawable drawable=getResources().
                                        getDrawable(displayWeather.getIconid());
                                iconImageView.setImageDrawable(drawable );
                            }
                        });

                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IO Exception caught : ", e);
                } catch(JSONException e) {
                    Log.e(TAG,"JSON Exception caught: ", e);

                }

            }
        });
    }
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {

        JSONObject forecast= new JSONObject(jsonData);
        String timezone=forecast.getString("timezone");

        JSONObject currently=forecast.getJSONObject("currently");

        CurrentWeather currentWeather= new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setLocationLabel("Alcatraz Island, CA");
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);



        return currentWeather;

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager= (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo=manager.getActiveNetworkInfo();

                boolean isAvailable=false;

                if(networkInfo!=null&& networkInfo.isConnected())
                {
                    isAvailable=true;

                }
                else {
                    Toast.makeText(this, R.string.network_unavailable_resources,
                            Toast.LENGTH_LONG).show();
                }

                return isAvailable;
    }

    private void alertUserAboutError() {

        AlertDialogFragment dialog=new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    public void OnclickRefresher(View view)
    {
        Toast.makeText(this,"Refreshing data", Toast.LENGTH_LONG).show();
       getForeCast(latitude,longitude);
    }


}
