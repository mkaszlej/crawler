package crawler;

public class App {
	
	public App() {
		
	}
	
	public static void main(String [] args) 
	{
		DatabaseEngine dbConnection = new DatabaseEngine();

		EntryPoint e = new EntryPoint(0, "jastkow.pl", "http://www.jastkow.pl/" , System.currentTimeMillis() , 0, 0);

		//EntryPoint e = new EntryPoint(0, "wieliszew.pl", "http://www.wieliszew.pl/" , System.currentTimeMillis() , 2, 0);
		//EntryPoint e = new EntryPoint(0, "rozan.eur.pl", "http://www.rozan.eur.pl/" , System.currentTimeMillis() , 2, 0);

		PageProcessor p = new PageProcessor(dbConnection, e);
		p.run();
	}
	
}
