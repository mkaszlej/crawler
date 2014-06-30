package crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntryPoint {

    long timestamp;
    int entry_id, visits, search_depth;
    String name, entry_url;
    ArrayList<String> linkUrl = null;
    ArrayList<String> wrongUrl = null;
    List<String> linkUrlList = null;
    List<String> wrongUrlList = null;
    
    public EntryPoint(int entry_id, String name, String entry_url,  long timestamp, int search_depth, int visits) {
    	this.entry_id = entry_id;
    	this.name = name;
    	this.entry_url = entry_url;
    	this.search_depth = search_depth;
    	this.timestamp = timestamp;
    	this.visits = visits;
    	this.linkUrl = new ArrayList<String>();
    	this.wrongUrl = new ArrayList<String>();
    	this.linkUrlList = Collections.synchronizedList(linkUrl);
    	this.wrongUrlList = Collections.synchronizedList(wrongUrl);
    }
	
    
    public synchronized boolean addLink(String url)
    {
    	if(linkUrlList.contains(url)) return false;
    	
    	linkUrlList.add(url);
    	return true;
    }

    public synchronized void addBrokenLink(String url)
    {
    	wrongUrlList.add(url);
    }
    
    public synchronized boolean isBroken(String url)
    {
    	return wrongUrlList.contains(url);
    }
}
