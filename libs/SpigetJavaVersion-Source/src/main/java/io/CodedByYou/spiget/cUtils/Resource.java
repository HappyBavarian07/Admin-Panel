package io.CodedByYou.spiget.cUtils;

import io.CodedByYou.spiget.Author;
import io.CodedByYou.spiget.Rating;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by CodedByYou on 10/8/2017.
 * Day: Sunday
 * Time: 8:17 PM
 */
public class Resource {
    private JSONObject resoure;
    private int resourceid;
    private Author author;
    private String resourcename;
    private boolean permium;
    private int price;
    private int releaseDate;
    private int downloads;
    private int likes;
    private String downloadLink;
    private String resourceLink;
    private String resourceIconLink;
    private Rating rating;
    private List<String> links;
    private List<String> testedVersions;
    private String description,descriptionAsXml;
    public Resource(String resourcename) throws Exception {
        this.resourcename = resourcename;
        resoure = get("");
        resourceid = resoure.getInt("id");
        resoure = U.getResource(null,resourceid);
        this.resourcename = resoure.getString("name");
        permium = resoure.getBoolean("premium");
        price = resoure.getInt("price");
        releaseDate = resoure.getInt("releaseDate");
        downloads = resoure.getInt("downloads");
        likes = resoure.getInt("likes");
        resourceIconLink = "https://www.spigotmc.org/" + resoure.getJSONObject("icon").getString("url");
        descriptionAsXml = resoure.getString("description");
        descriptionAsXml = new String(Base64.getDecoder().decode(descriptionAsXml));
        description = descriptionAsXml;
        description=description.replaceAll("<.*?>", "");
        description = description.replaceAll("(?m)^[ \t]*\r?\n", "");
        links = new ArrayList<>();
        JSONObject object = resoure.getJSONObject("links");
        String o = object.toString();
        String[] x = o.split(",");
        for(String l : x){
            links.add(l);
        }
        object = resoure.getJSONObject("rating");
        rating = new Rating(object.getInt("count"),object.getInt("average"));
        JSONArray array = resoure.getJSONArray("testedVersions");
        testedVersions = new ArrayList<>();
        for(int i = 0; i < array.length();i++){
            testedVersions.add(array.getString(i));
        }
        JSONObject ox = resoure.getJSONObject("file");
        downloadLink = "https://www.spigotmc.org/"+ox.getString("url");
        String z = resourcename;
        z = z.replaceAll(" - ","-");
        z = z.replaceAll(" ","-");
        resourceLink = "https://spigotmc.org/resources/"+z+"."+ resourceid;

        author = Author.getByResource(resourceid);
    }
    public Resource(int resourceid) throws Exception {
        this.resourceid = resourceid;
        resoure = U.getResource(null,resourceid);
        this.resourcename = resoure.getString("name");
        permium = (Boolean) resoure.get("premium");
        price = resoure.getInt("price");
        releaseDate = resoure.getInt("releaseDate");
        downloads = resoure.getInt("downloads");
        descriptionAsXml = resoure.getString("description");
        descriptionAsXml = new String(Base64.getDecoder().decode(descriptionAsXml));
        description = descriptionAsXml;
        description=description.replaceAll("<.*?>", "");
        description = description.replaceAll("(?m)^[ \t]*\r?\n", "");
        likes = resoure.getInt("likes");
        links = new ArrayList<>();
        JSONObject object = resoure.getJSONObject("links");
        String o = object.toString();
        String[] x = o.split(",");
        for(String l : x){
            links.add(l);
        }
        object = resoure.getJSONObject("rating");
        rating = new Rating(object.getInt("count"),object.getInt("average"));
        JSONArray array = resoure.getJSONArray("testedVersions");
        testedVersions = new ArrayList<>();
        for(int i = 0; i < array.length();i++){
            testedVersions.add(array.getString(i));
        }
        JSONObject ox = resoure.getJSONObject("file");
        downloadLink = "https://www.spigotmc.org/"+ox.getString("url");
        String z = resourcename;
        z = z.replaceAll(" - ","-");
        z = z.replaceAll(" ","-");
        resourceLink = "https://spigotmc.org/resources/"+z+"."+ resourceid;

        author = Author.getByResource(resourceid);
    }
    private JSONObject get(String x)throws Exception{
        String url = "https://api.spiget.org/v2/search/resources/"+resourcename+"/"+x;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
        //add request header
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
        //print in String
        //Read JSON response and print
        String res = response.toString();
        res = res.replaceFirst( "\\[","");
        res = res.replaceFirst( "\\]","");
        return new JSONObject(res);
    }
    public String getResourceIconLink() {
        return resourceIconLink;
    }
    public String getTag(){
        String i;
        try {
            i = resoure.getString("tag");
            return  i;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) throws Exception {
        Resource r = new Resource("dlinker");
        System.out.print(r.getDescription());
    }
    public String getDescription() {
        return description;
    }

    public String getResourceName(){
        return resourcename;
    }

    public int getResourceId(){
        return resourceid;
    }

    public Author getAuthor(){
        return author;
    }

    public int getResourceid() {
        return resourceid;
    }

    public boolean isPermium() {
        return permium;
    }

    public int getDownloads() {
        return downloads;
    }

    public int getLikes() {
        return likes;
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public Rating getRating() {
        return rating;
    }

    public String getDownloadLink(){
        return downloadLink;
    }

    public String getResourceLink() {
        return resourceLink;
    }

    public int getPrice() {
        return price;
    }

    public List<String> getLinks() {
        return links;
    }

    public List<String> getTestedVersions() {
        return testedVersions;
    }

}
