package crawler;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
 
public class DatabaseEngine {
 
    public static final String DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL = "jdbc:sqlite:crawler.db";
 
    public static final String CREATE_ENTRY_POINTS_TABLE = "CREATE TABLE IF NOT EXISTS entry_points (entry_id INTEGER PRIMARY KEY AUTOINCREMENT, name varchar(255), entry_url varchar(2000) NOT NULL UNIQUE, search_depth INTEGER, date_visited INTEGER, visits INTEGER )";
    public static final String CREATE_LINK_POINTS_TABLE = "CREATE TABLE IF NOT EXISTS link_points (link_id INTEGER PRIMARY KEY AUTOINCREMENT, entry_id INTEGER, link_url varchar(2000) NOT NULL UNIQUE, link_depth INTEGER, date_visited INTEGER, visits INTEGER, hit_count INTEGER, link_count INTEGER , flags VARCHAR(50)) ";
    
    private Connection conn;
    private Statement stat;
 
    public DatabaseEngine() {
        try {
            Class.forName(DatabaseEngine.DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Brak sterownika JDBC");
            e.printStackTrace();
        }
 
        this.init();
    }
 
    private void init()
    {
        try {
            conn = DriverManager.getConnection(DB_URL);
            stat = conn.createStatement();
        	stat.execute(CREATE_ENTRY_POINTS_TABLE);
            stat.execute(CREATE_LINK_POINTS_TABLE);
        } catch (SQLException e) {
            System.err.println("Blad przy inicjalizacji bazy:");
            e.printStackTrace();
        }
    }
    
    public boolean insertEntryPoint(String name, String entry_url, int search_depth) {
        try {
            PreparedStatement prepStmt = conn.prepareStatement("insert or replace into entry_points values (NULL, ?, ?, ?, ?, ?);");
            prepStmt.setString(1, name);
            prepStmt.setString(2, entry_url);
            prepStmt.setInt(3, search_depth);
            prepStmt.setLong(4, System.currentTimeMillis() );
            prepStmt.setInt(5, 0);
            prepStmt.execute();
        } catch (SQLException e) {
            System.err.println("Error on entry point insert");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean touchEntryPoint(String name)
    {
    	try {
            PreparedStatement prepStmt = conn.prepareStatement("update entry_points set ( date = ? , visits = ? ) where name = ?;");
            prepStmt.setLong(1, System.currentTimeMillis() );
            prepStmt.setInt(2, this.getEntryPointVisits(name)+1);
            prepStmt.setString(3, name);
            prepStmt.execute();
        } catch (SQLException e) {
            System.err.println("Error while touching entry point");
            e.printStackTrace();
            return false;
        }
    	return true;
    }

    private boolean insertLinkPoint(int entry_id, String link_url, int link_depth, int visits, int hit_count, int link_count, String flags) {
        try {
            PreparedStatement prepStmt = conn.prepareStatement("insert or replace into link_points values (NULL, ?, ?, ?, ?, ?, ?, ?, ?);");
            prepStmt.setInt(1, entry_id);
            prepStmt.setString(2, link_url);
            prepStmt.setInt(3, link_depth);
            prepStmt.setLong(4, System.currentTimeMillis() );
            prepStmt.setInt(5, visits);
            prepStmt.setInt(6, hit_count);
            prepStmt.setInt(7, link_count);
            prepStmt.setString(8, flags);
            prepStmt.execute();
        } catch (SQLException e) {
            System.err.println("Error on link point insert");
            e.printStackTrace();
            return false;
        }
        System.out.println("Inserted linkPoint values. eID: "+entry_id+" link_url: "+link_url+" link_depth: "+link_depth+" visits: "+visits+" hits: "+hit_count+" links: "+link_count+" flags: "+flags);
        return true;
    }
    
    public boolean insertLinkPoint(int entry_id, String link_url, int link_depth, int hit_count, int link_count, String flags) {
    	 try {
             ResultSet result = stat.executeQuery("SELECT * FROM link_points WHERE link_url = '"+link_url+"' and entry_id = "+entry_id );
             if(result.next())
             {
            	 int visits = result.getInt("visits");
            	 System.out.println("UPDATING linkPoint eID: "+entry_id+" url: "+link_url);
            	 return insertLinkPoint(entry_id, link_url, link_depth, visits+1, hit_count, link_count, flags);
             }
             else return insertLinkPoint(entry_id, link_url, link_depth, 1, hit_count, link_count, flags);
             
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
    }
    
    private int getEntryPointVisits(String name)
    {
        try {
            ResultSet result = stat.executeQuery("SELECT visits FROM entry_points WHERE name = '"+name+"'");
            int visits = -1;
            if(result.next()) visits = result.getInt("visits");
            return visits;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public List<EntryPoint> getEntryPoints() {
        List<EntryPoint> entryPoints = new LinkedList<EntryPoint>();
        try {
            ResultSet result = stat.executeQuery("SELECT * FROM entry_points");
            
            long timestamp;
            int entry_id, visits, search_depth;
            String name, entry_url;
            
            while(result.next()) {
                entry_id = result.getInt("entry_id");
                name = result.getString("name");
                entry_url = result.getString("entry_url");
                timestamp = result.getLong("date_visited");
                search_depth = result.getInt("search_depth");
                visits = result.getInt("visits");
                entryPoints.add(new EntryPoint(entry_id, name, entry_url, timestamp, search_depth, visits));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return entryPoints;
    }

    public EntryPoint getEntryPoint(String name) {
        try {
            ResultSet result = stat.executeQuery("SELECT * FROM entry_points WHERE name = '"+name+"'");
            
            long timestamp;
            int entry_id, visits, search_depth;
            String  entry_url;
            
            if(result.next()) {
                entry_id = result.getInt("entry_id");
                name = result.getString("name");
                entry_url = result.getString("entry_url");
                timestamp = result.getLong("date_visited");
                search_depth = result.getInt("search_depth");
                visits = result.getInt("visits");
                return new EntryPoint(entry_id, name, entry_url, timestamp, search_depth, visits);
            }
            else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<LinkPoint> getLinkPoints(int entry_id) {
        List<LinkPoint> linkPoints = new LinkedList<LinkPoint>();
        try {
            ResultSet result = stat.executeQuery("SELECT * FROM link_points WHERE entry_id = "+entry_id);
            
            long timestamp;
            int link_id, visits, link_depth, hit_count, link_count;
            String link_url, flags;
            
            while(result.next()) {
                link_id = result.getInt("link_id");
                link_url = result.getString("link_url");
                timestamp = result.getLong("date_visited");
                link_depth = result.getInt("link_depth");
                visits = result.getInt("visits");
                hit_count = result.getInt("hit_count");
                link_count = result.getInt("link_count");
                flags = result.getString("is_external");
                linkPoints.add(new LinkPoint(link_id, entry_id, link_url, link_depth, timestamp, visits, hit_count, link_count, flags));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return linkPoints;
    }

    public LinkPoint getLinkPoint(String link_url) {
        try {
            ResultSet result = stat.executeQuery("SELECT * FROM link_points WHERE link_url = '"+link_url+"'");
            
            long timestamp;
            int entry_id, link_id, visits, link_depth, hit_count, link_count;
            String flags;
            
            if(result.next()) {
                link_id = result.getInt("link_id");
                entry_id = result.getInt("entry_id");
                timestamp = result.getLong("date_visited");
                link_depth = result.getInt("link_depth");
                visits = result.getInt("visits");
                hit_count = result.getInt("hit_count");
                link_count = result.getInt("link_count");
                flags = result.getString("is_external");
                return new LinkPoint(link_id, entry_id, link_url, link_depth, timestamp, visits, hit_count, link_count, flags);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public boolean checkIfLinkPointExists(String link_url)
    {        
    	try {
	        ResultSet result = stat.executeQuery("SELECT * FROM link_points WHERE link_url = '"+link_url+"'");
	        
	        if(result.next()) 
	        	return true;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	    return false;
    }
    
}
