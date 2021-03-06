package Connectors;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Connectors.Classes.Artist;
import Connectors.Classes.VolleyCallBack;
import Connectors.Classes.images;

public class SearchService {

    private static String ENDPOINT = "https://api.spotify.com/v1/search?";
    private String url;
    private String type;
    private SharedPreferences sharedPreferences;
    Context mContext;
    private RequestQueue queue; // add or cancel network requests through request queue.
    private ArrayList<Artist> artists = new ArrayList<>();

    public SearchService(Context context) {
        mContext=context;
    }

    public ArrayList<Artist> getArtists(){
        return artists;
    }


    public ArrayList<Artist> Search(String q,VolleyCallBack callBack) {
        artists.clear();
        type = "artist";
        q = q.replaceAll(" ","%20");
        url = String.format(ENDPOINT+"q=%s&type=%s&limit=5",q, type);
        String endpoint = url;

        sharedPreferences = mContext.getSharedPreferences("SPOTIFY",0);
        queue = Volley.newRequestQueue(mContext);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    System.out.println("T");
                    Gson gson = new Gson();
                    JSONObject artistsObj = response.optJSONObject("artists");
                    JSONArray itemsArray = artistsObj.optJSONArray("items");
                    for(int n=0;n<itemsArray.length();n++){

                        try {
                            JSONObject object = itemsArray.getJSONObject(n);
                            Artist artist = gson.fromJson(object.toString(), Artist.class);
                            JSONArray imagesArray = object.optJSONArray("images");
                            ArrayList<images> list = new ArrayList<>();
                            if(imagesArray != null){
                                for(int i = 0;i<imagesArray.length();i++){
                                    images img = gson.fromJson(imagesArray.getString(i),images.class);
                                    list.add(img);
                                }
                            }

                            artist.setImages(list);
                            artists.add(artist);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callBack.onSuccess();

                }, error -> {
                    // TODO: Handle error
                    Log.e("ERROR",error.getMessage());

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }

        };
        queue.add(jsonObjectRequest);
        return artists;

    }


}
