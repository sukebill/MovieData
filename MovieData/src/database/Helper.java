package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.List;

import dataTypes.Actor;
import dataTypes.Movie;

public class Helper {
	
	public Connection PostgresConnector(){
		Connection c = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/task3",
	            "postgres", "qAVAcgQ4");
	         c.setAutoCommit(false);
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	      System.out.println("Opened database successfully");
		return c;
	}
	
	public void deleteAll(Connection c){
		Statement stmt;
		try {
			stmt = c.createStatement();
			String sql = "DELETE from movie;";
	        stmt.executeUpdate(sql);
	        c.commit();
	        
	        sql = "DELETE from actor";
	        stmt.executeUpdate(sql);
	        c.commit();
	        
	        sql = "DELETE from actorinmovie";
	        stmt.executeUpdate(sql);
	        c.commit();
	        
	        stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
	}
	
	public void insertData(Movie movie, Connection c){	      
	      System.out.println("insering " + movie.getName() + "...");
	         try {
	        	 int idMovie = insertMovie(movie, c);
	        	 insertActor(movie.getStarring(), idMovie, c);	        	 
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	private int insertMovie(Movie movie, Connection c) throws SQLException{
		int last_inserted_id = 0;
		PreparedStatement stmt = null;
		
		String sql = "INSERT INTO movie (name,description,year) "
	               + "VALUES (?,?,?);";
		stmt = c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
   	 	stmt.setString(1,movie.getName());
   	 	stmt.setString(2, movie.getDescription());
   	 	stmt.setInt(3, movie.getYear());
   	 	stmt.execute();
   	 	
	   	ResultSet rs = stmt.getGeneratedKeys();
	    if(rs.next())
	    {
	        last_inserted_id = rs.getInt(1);
	    }
	    
	    rs.close();
   	 	stmt.close();
        c.commit();
        return last_inserted_id;
	}
	
	private void insertActor(List<Actor> actors, int idMovie, Connection c) throws SQLException {
		PreparedStatement stmt = null;
		Savepoint savepoint = null;
		String sql = "INSERT INTO actor (name,wiki) "
	               + "VALUES (?,?);";
		
		for(int i = 0; i < actors.size(); i++ ){
			try {
				stmt = c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1,actors.get(i).getName());
		   	 	stmt.setString(2, actors.get(i).getWiki());
		   	    savepoint = c.setSavepoint();
		   	 	stmt.execute();
		   	 	
		   	 	ResultSet rs = stmt.getGeneratedKeys();
		   	 	int last_inserted_id = 0;
			    if(rs.next())
			    {
			        last_inserted_id = rs.getInt(1);			        
			    }
		   	 	
			    rs.close();
		   	 	stmt.close();
		   	 	c.commit();
		   	 	
		   	 	relateActorWithMovie(idMovie, last_inserted_id, c);
			} catch (SQLException e) {
				if(c != null && savepoint != null) {
			        c.rollback(savepoint);
			        //insert the actor in table actorXmovie
			        int idActor = getActorId(actors.get(i).getName(), c);
			        relateActorWithMovie(idMovie, idActor, c);
			    }
				//e.printStackTrace();
			}
	   	 	
		}	
	}
	
	private void relateActorWithMovie(int idMovie, int idActor, Connection c){
		PreparedStatement stmt = null;
		String sql = "INSERT INTO actorinmovie (idmovie,idactor) "
	               + "VALUES (?,?);";
		try {
			stmt = c.prepareStatement(sql);
			stmt.setInt(1,idMovie);
	   	 	stmt.setInt(2, idActor);
	   	    
	   	 	stmt.execute();  	 	
	   	 	
	   	 	stmt.close();
	   	 	c.commit();	   	 	
	   	 	
		} catch (SQLException e) {			
			e.printStackTrace();
		}
	}
	
	private int getActorId(String name, Connection c){
		int id = 0;
		String sql = "select id_actor from actor where name='" + name + "';";
		Statement stmt = null;
		try {	   	    
			stmt = c.createStatement();
	   	 	ResultSet rs = stmt.executeQuery(sql);	   	 	
	   	 	if(rs.next())
		    {
		        id = rs.getInt(1);			        
		    }	   	 	
	   	 	stmt.close();
	   	 	c.commit();	 	 	
		} catch (SQLException e) {			
			e.printStackTrace();
		}
		return id;
	}

}
