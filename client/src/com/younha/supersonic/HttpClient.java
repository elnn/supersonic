package com.younha.supersonic;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;


public class HttpClient {
    private static final String SERVER_URL = "http://coreahs.iptime.org:9240/";

    public static abstract class HttpListener {
        public abstract void onSendComplete(JSONObject result);
        public abstract void onSendError(int error);
    }

    public HttpClient() {
    }

    public SendHttp Send() {
        return new SendHttp();
    }

    public class SendHttp extends AsyncTask<Object, Void, JSONObject> {

        HttpListener listener;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result == null) {
                listener.onSendError(0);
            } else {
                listener.onSendComplete(result);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected JSONObject doInBackground(Object... arg0) {

            String url = (String) arg0[0];
            String value = (String) arg0[1];
            listener = (HttpListener) arg0[2];

            if (value == null)
                value = "";

            url = SERVER_URL + url;
            Log.e("url", url + "/" + value);
            DefaultHttpClient client = new DefaultHttpClient();

            try {
                HttpGet post = new HttpGet(url + "/" + value);

                HttpParams params = client.getParams();
                HttpConnectionParams.setConnectionTimeout(params, 3000);
                HttpConnectionParams.setSoTimeout(params, 3000);

                HttpResponse response = client.execute(post);
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));

                String line = null;
                String result = "";

                while ((line = br.readLine()) != null) {
                    result += line;
                }

                return new JSONObject(result);

            } catch (Exception e) {
                e.printStackTrace();
                client.getConnectionManager().shutdown();
                return null;
            }
        }
    }
}