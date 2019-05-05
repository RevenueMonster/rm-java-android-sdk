package com.revenuemonster.payment.util;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

public class HttpClient {

    private static InputStream is = null;
    private static JSONObject jObj = null;
    private static String json = "";
    private HttpURLConnection urlConnection = null;

    /**
     * This method helps in retrieving data from HTTP server using HttpURLConnection.
     *
     * @param url    The HTTP URL where JSON data is exposed
     * @param method HTTP method: GET or POST
     * @param data Query parameters for the request
     * @return This method returns the JSON object fetched from the server
     */
    public JSONObject request(String url, String method, String data) {

        try {
            Uri.Builder builder = new Uri.Builder();
            URL urlObj;
            if ("GET".equals(method)) {
                urlObj = new URL(url);
                urlConnection = (HttpURLConnection) urlObj.openConnection();
                urlConnection.setRequestMethod(method);


            } else {
                urlObj = new URL(url);
                urlConnection = (HttpURLConnection) urlObj.openConnection();
                urlConnection.setRequestMethod(method);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                if (data != null && !data.isEmpty()) {
                    urlConnection.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));
                    urlConnection.getOutputStream().write(data.getBytes());
                }
            }
            urlConnection.connect();

            Integer statusCode = urlConnection.getResponseCode();

            if (statusCode < 300) {
                is = urlConnection.getInputStream();
            } else {
                is = urlConnection.getErrorStream();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;

            //Parse the response
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            jObj = new JSONObject(json);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        } catch (Exception e) {
            Log.e("Exception123", "Error parsing data " + e.toString());
        }

        return jObj;
    }
}