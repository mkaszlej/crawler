package crawler;

import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parser.ParserFactory;

public class PageProcessor implements Runnable {

	DatabaseEngine dbConnection;
	LinkPoint linkPoint;
	EntryPoint entryPoint;
	
	Document document;
	
	private int link_counter;

	public PageProcessor(DatabaseEngine dbConnection, EntryPoint entryPoint) {
		
		this.link_counter = 0;
		this.dbConnection = dbConnection;
		this.linkPoint = new LinkPoint(entryPoint);
		this.entryPoint = entryPoint;
		
	}
	
	public PageProcessor(DatabaseEngine dbConnection, EntryPoint entryPoint, LinkPoint linkPoint) {
		
		this.dbConnection = dbConnection;
		this.linkPoint = linkPoint;
		this.entryPoint = entryPoint;
		
	}

	public boolean init()
	{
		String url = linkPoint.getUrl();
		
		//pomin zepsute
		if( entryPoint.isBroken( url ) ) return false;
		
		//sprawdz czy juz istnieje
		if( !entryPoint.addLink(linkPoint.getUrl()) ) return false;
		
		try{
			System.out.println( "Processing link-point" + url +"");
			
			this.document = Jsoup.connect( url )
					.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
					.referrer("http://www.google.com")
					.timeout(5000)
					.maxBodySize(1024*500) //500KB 
					.get();
			
			//Przetworz wszystko
			for (ParserFactory.ParserType parserType : ParserFactory.ParserType.values()) {
	            ParserFactory.createParser(parserType, document).parse();
	        }
						
		}
		catch(org.jsoup.UnsupportedMimeTypeException e)
		{
			System.out.println( url +" - Skipping: unsupported MIME");
			entryPoint.addBrokenLink(url);
			return false;
		}
		catch(org.jsoup.HttpStatusException e)
		{
			System.out.println( url +" - HTTP Exception -> " + e.getCause());
			entryPoint.addBrokenLink(url);
			return false;
		}
		catch(Exception e)
		{
			System.out.println("Error while loading linkPoint");
			e.printStackTrace();
			entryPoint.addBrokenLink(url);
			return false;
		}
		return true;
	}
	
	public void run()
	{
		boolean process = true;
		
		//pomin potencjalne entry pointy
		if(linkPoint.isExternal()) process = false;
		
		if(!init()) return;
		
		if(process) processLinks();
		updateDB();
		
	}
	
	private void processLinks()
	{
		if(linkPoint.getLinkDepth() > entryPoint.search_depth) return;
		
		Elements links = document.select("a");
		Iterator<Element> link = links.iterator();
		while(link.hasNext())
		{
			String url = link.next().attr("href").toString();
			LinkPoint lp = new LinkPoint(-1, entryPoint.entry_id, url, linkPoint.getLinkDepth()+1, System.currentTimeMillis(), 0, 0, 0, "" );
			if(!convertUrl(lp)) continue;
			if( !checkUrlExternal(lp) ) lp.addFlag("E");

			PageProcessor p = new PageProcessor(dbConnection, entryPoint, lp);
			p.run();
			
			this.link_counter++;
			
		}
	}
	
	private boolean checkUrlExternal(LinkPoint lp)
	{
		return lp.getUrl().contains(entryPoint.name); 
	}
	
	private boolean convertUrl(LinkPoint lp)
	{
		if(!lp.getUrl().contains("http://") && !lp.getUrl().startsWith("www."))
		{
			if(lp.getUrl().startsWith("/")) lp.setUrl( lp.getUrl().substring(1) );
			else if(lp.getUrl().startsWith("//")) lp.setUrl( lp.getUrl().substring(2) );
			else if(lp.getUrl().startsWith("/../")) lp.setUrl( lp.getUrl().substring(4) );
			else if(lp.getUrl().startsWith("./")) lp.setUrl( lp.getUrl().substring(2) );
			else if(lp.getUrl().startsWith("../")) lp.setUrl( lp.getUrl().substring(3) );
			
			lp.setUrl(entryPoint.entry_url+lp.getUrl());
		}
		return true;
	}
	
	private void updateDB()
	{
		dbConnection.insertLinkPoint( entryPoint.entry_id, linkPoint.getUrl(), linkPoint.getLinkDepth(), linkPoint.getHitCount(), this.link_counter, linkPoint.getFlags() );
	}
	
}
