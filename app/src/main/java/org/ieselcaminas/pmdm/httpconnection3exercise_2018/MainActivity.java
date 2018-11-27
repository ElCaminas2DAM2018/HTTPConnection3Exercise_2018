package org.ieselcaminas.pmdm.httpconnection3exercise_2018;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {

        private int length = 0;
        private ProgressDialog progressDialog;

        protected Bitmap doInBackground(String... urls) {
            return downloadImage(urls[0]);
        }
        protected void onPostExecute(Bitmap result) {
            ImageView img = (ImageView) findViewById(R.id.imageView);
            img.setImageBitmap(result);
            progressDialog.dismiss();
        }
        protected  void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Downloading image file...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        private Bitmap downloadImage(String URL) {
            Bitmap bitmap = null;
            InputStream in = null;
            int increment;
            byte[] data;
            try {
                in = openHttpConnection(URL);
                data = new byte[length];
                increment = length / 100;
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                int count = -1;
                int progress=0;
                while((count = in.read(data,0,increment)) != -1) {
                    outStream.write(data, 0, count);
                    progress++;
                    publishProgress(progress);
                }
                bitmap = BitmapFactory.decodeByteArray(outStream.toByteArray() , 0, data.length);
                in.close();
                outStream.close();
            } catch (IOException e1) {
                Log.d("NetworkingActivity", e1.getLocalizedMessage());
            }
            return bitmap;
        }

        private InputStream openHttpConnection(String urlString) throws IOException {
            InputStream in = null;
            int response;
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            if (!(conn instanceof HttpURLConnection))
                throw new IOException("Not an HTTP connection");
            try{
                HttpURLConnection httpConn = (HttpURLConnection) conn;
                httpConn.setInstanceFollowRedirects(true);
                httpConn.setRequestMethod("GET");
                httpConn.connect();
                response = httpConn.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    in = httpConn.getInputStream();
                }
                length = httpConn.getContentLength();
            }
            catch (Exception ex) {
                Log.d("Networking", ex.getLocalizedMessage());
                throw new IOException("Error connecting");
            }
            return in;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // fetch data
                    DownloadImageTask downloadImageTask = new DownloadImageTask();
                    downloadImageTask.execute("https://images.pexels.com/photos/7919/pexels-photo.jpg?cs=srgb&dl=cc0-desktop-backgrounds-fog-7919.jpg&fm=jpg");
                } else {
                    // display error
                    Toast.makeText(getApplicationContext(), "No internet connection available.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}