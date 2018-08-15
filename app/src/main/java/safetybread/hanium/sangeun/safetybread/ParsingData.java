package safetybread.hanium.sangeun.safetybread;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;

/**
 * Created by sangeun on 2018-08-12.
 */

public class ParsingData {
    private static final AsyncHttpClient httpClient = new AsyncHttpClient();
    //private static final String API_URL = "http://data.ex.co.kr/openapi/locationinfo/locationinfoRest?key=9629341692&type=json&numOfRows=20&pageNo=";
    private static final String API_URL = "http://api.data.go.kr/openapi/restarea-std?s_page=1&s_list=300&type=json";
    private static String ServiceKey = "";

    static public JSONArray getJson(InputStream is) {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "EUC-KR"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonString = writer.toString();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    static public void get(AsyncHttpResponseHandler handler) {
        if (ServiceKey.equals("")) {
            try {
                ServiceKey = URLEncoder.encode("De8CvmhhCiN4hcz/qlJZ5wHLT09jYgYcPPl2Gg531adYXQ7vF+gFmUb1127SPZ0E0XskFTEDAjcnednuJJ3URw==", "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        Log.i("URL: ", API_URL + ServiceKey);
        RequestParams params = new RequestParams();
        params.setContentEncoding("utf8");
        params.put("serviceKey", ServiceKey);
        httpClient.get(API_URL, params, handler);
    }
}
