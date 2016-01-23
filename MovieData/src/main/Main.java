package main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dataTypes.Actor;
import dataTypes.Movie;
import database.Helper;

public class Main {
	
	private static final String API_KEY = "qtqep7qydngcc7grk4r4hyd9";
	private static String URI = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?page_limit=16&page=1&country=gr&apikey="
								+ API_KEY;
	private final String USER_AGENT = "Mozilla/5.0";
	private Connection c;
	private Helper databaseUtils = new Helper();
	
	public static void main (String[] args) throws Exception
	{
		Main roToApi = new Main();
		roToApi.RoToSendGet();
	}
	
	private void RoToSendGet() throws Exception {
		
		c = databaseUtils.PostgresConnector();
		databaseUtils.deleteAll(c);
		while(URI.length() > 10){
			URL obj = new URL(URI);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);
			
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			JSONObject jsonObj = new JSONObject(response.toString());
			escapeJSON(jsonObj);
			
			if(jsonObj.getJSONObject("links").has("next")){
				URI = jsonObj.getJSONObject("links").get("next") + "&apikey=" + API_KEY;
			}
			else{
				break;
			}
		}
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
	}
	
	private void escapeJSON(JSONObject jsonObj) throws JSONException{
		JSONArray jsonArray = jsonObj.getJSONArray("movies"); 
		
		for (int i = 0; i < jsonArray.length(); i++) {
			Movie movie = new Movie();
	        JSONObject explrObject = jsonArray.getJSONObject(i);
	        
	        movie.setName(explrObject.get("title").toString());
	        movie.setDescription(explrObject.get("synopsis").toString());
	        movie.setYear(Integer.parseInt(explrObject.get("year").toString()));
	        
	        JSONArray array = explrObject.getJSONArray("abridged_cast");
	        List<Actor> starring = new ArrayList<Actor>();
	        
	        for( int j = 0; j < array.length(); j++){
	        	String name = array.getJSONObject(j).get("name").toString();
	        	Actor actor = new Actor();
	        	actor.setName(name);
	        	
	        	String[] split = name.split(" ");	        	
	        	name = "";
	        	
	        	for(int e = 0; e < split.length; e++){
	        		name += split[e];
	        		if(e < split.length-1){
	        			name += "_";
	        		}
	        	}
	        	
	        	actor.setWiki(connectToWiki(name));
	        	starring.add(actor);
	        }
	        movie.setStarring(starring);
	        databaseUtils.insertData(movie, c);
		}
	}
	
	private String connectToWiki(String name){
		String result = "";
		String[] nameSurname = name.split("_");
		try{
    		Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/" + name)
        			.data("query", "Java")
        			.userAgent(USER_AGENT)
        			.cookie("auth", "token")
        			.timeout(5000)
        			.get();
        	Elements links = doc.select("p");
        	for (Element link : links) {
                result = link.text();
                if(result.contains(nameSurname[0]) | result.contains("born")){
                	if(result.endsWith("to:") | result.endsWith("of:") | result.endsWith("to :")){
                		result = connectToWiki(name + "_(actor)");
                		break;
                	}
                	else{
                		break;
                	}                    
                }
            }        	
    	}
    	catch (NullPointerException e) {
            e.printStackTrace();
        } catch (HttpStatusException e) {
        	result = "There is no information in Wikipedia";
            //e.printStackTrace();
        } catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
}
