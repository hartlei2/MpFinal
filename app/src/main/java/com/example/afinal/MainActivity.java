package com.example.afinal;

import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String API_URL = "https://api.funtranslations.com/translate/morse.json";
    private String translatedText;
    private MediaPlayer blank = null;
    private MediaPlayer longBeep;
    private MediaPlayer shortBeep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        blank = MediaPlayer.create(MainActivity.this,R.raw.blank);
        longBeep = MediaPlayer.create(MainActivity.this,R.raw.dash);
        shortBeep = MediaPlayer.create(MainActivity.this,R.raw.dot);
    }

    public void translateText(View view) throws JSONException {
        EditText editText = (EditText) findViewById(R.id.textToTranslate);
        String text = editText.getText().toString();

        JSONObject request = new JSONObject();
        request.put("text", text);

        // from documentation at https://developer.android.com/training/volley/request.html#request-json
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, API_URL, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            translatedText = response.getJSONObject("contents").getString("translated");
                            ((TextView) findViewById(R.id.translatedText)).setText(translatedText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error", error.toString());
                        Toast.makeText(getApplicationContext(),"API request failed!",Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
    public void playBeeps (View view) {
        playSound(longBeep, shortBeep, 0);
    }

    private void playSound(final MediaPlayer dash, final MediaPlayer dot, final int offset) {
        if (translatedText == null) {
            Toast.makeText(getApplicationContext(),"Enter text to translate!",Toast.LENGTH_SHORT).show();
            return;
        }
        if (offset >= translatedText.length()) {
            return;
        }

        char character = translatedText.charAt(offset);
            if (character == '.') {
                dot.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        SystemClock.sleep(blank.getDuration());
                        playSound(dash, dot, offset + 1);
                    }
                });
                dot.start();
            } else if (character == '-') {
                dash.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        SystemClock.sleep(blank.getDuration());
                        playSound(dash, dot, offset + 1);
                    }
                });
                dash.start();
            } else {
                blank.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        SystemClock.sleep(blank.getDuration());
                        playSound(dash, dot, offset + 1);
                    }
                });
                blank.start();
            }
    }
}
