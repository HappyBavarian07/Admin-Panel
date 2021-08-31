package io.CodedByYou.spiget.cUtils.cUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CodedByYou on 10/9/2017.
 * Day: Monday
 * Time: 5:12 PM
 */
public class U {
    public static JSONObject getResource(String x,int id)throws Exception{
        String url;
        if(x == null){
            url = "https://api.spiget.org/v2/resources/"+id;
        }else{
            url = "https://api.spiget.org/v2/resources/"+id+"/"+x;
        }
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //print in String
        //Read JSON response and print
        String res = response.toString();
        if(x != null) {
            res = res.replaceFirst("\\[", "");
            res = res.replaceFirst("\\]", "");
        }
        return new JSONObject(res);
    }

    public static JSONObject searchAuthor(String x)throws Exception{
        String url = "https://api.spiget.org/v2/search/authors/"+x;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String res = response.toString();
        res = res.replaceFirst( "\\[","");
        res = res.replaceFirst( "\\]","");
        return new JSONObject(res);
    } public static JSONObject getAuthor(int x)throws Exception{
        String url = "https://api.spiget.org/v2/authors/"+x;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String res = response.toString();
        res = res.replaceFirst( "\\[","");
        res = res.replaceFirst( "\\]","");
        return new JSONObject(res);
    }
}
