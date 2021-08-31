package io.CodedByYou.spiget;

import io.CodedByYou.spiget.cUtils.U;
import org.json.JSONObject;

/**
 * Created by CodedByYou on 10/8/2017.
 * Day: Sunday
 * Time: 9:07 PM
 */
public class Author {
    private int resourceid;
    private JSONObject xAuthor;
    private String Name;
    private String icon;
    private int id;
    private Author(int resourceid,int r) throws Exception{
        this.resourceid = resourceid;
        xAuthor = U.getResource("author",resourceid);
        Name = xAuthor.getString("name");
        id = xAuthor.getInt("id");
        JSONObject form_data = xAuthor.getJSONObject("icon");
        icon = "https://spigotmc.org/" + form_data.getString("url");
    }
    private Author (int id) throws Exception{
        xAuthor = U.getAuthor(id);
        Name = xAuthor.getString("name");
        id = xAuthor.getInt("id");
        JSONObject form_data = xAuthor.getJSONObject("icon");
        icon = "https://spigotmc.org/" + form_data.getString("url");
    }

    public static Author getByName(String name)throws Exception{
        return new Author( U.getAuthor(U.searchAuthor(name).getInt("id")).getInt("id"));
    }

    public static Author getById(int id)throws Exception{
        return new Author( U.getAuthor(id).getInt("id"));
    }

    public static Author getByResource(int id)throws Exception{
        return new Author(id,1);
    }

    public String getName(){
        return Name;
    }

    public int getId(){
        return id;
    }

    public String getIcon(){
        return icon;
    }
}
