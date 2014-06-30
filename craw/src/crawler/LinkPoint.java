package crawler;

public class LinkPoint {

	private long timestamp;
	private int link_id, entry_id, visits, link_depth, hit_count, link_count;
	private String link_url,flags;
	
    public LinkPoint(int link_id, int entry_id, String link_url, int link_depth, long timestamp, int visits, int hit_count, int link_count, String flags ) {
    	this.link_id = link_id;
    	this.entry_id = entry_id;
    	this.link_url = link_url;
    	this.link_depth = link_depth;
    	this.timestamp = timestamp;
    	this.visits = visits;
    	this.hit_count = hit_count;
    	this.link_count = link_count;
    	this.flags = flags;
    }
    
    public LinkPoint(EntryPoint entryPoint)
    {
    	this.link_id = -1;
    	this.entry_id = entryPoint.entry_id;
    	this.link_url = entryPoint.entry_url;
    	this.link_depth = 0;
    	this.timestamp = entryPoint.timestamp;
    	this.visits = entryPoint.visits;
    	this.hit_count = -1;
    	this.link_count = -1;
    	this.flags = "";
    }
    
    public boolean isExternal()
    {
    	return this.flags.contains("E");
    }
    
    public void addFlag(String flag)
    {
    	this.flags = this.flags + flag;
    }
    
    public String getFlags()
    {
    	return this.flags;
    }
    
    public int getHitCount()
    {
    	return this.hit_count;
    }
    
    public String getUrl()
    {
    	return this.link_url;
    }
    
    public void setUrl(String url)
    {
    	this.link_url = url;
    }
    
    public int getLinkDepth()
    {
    	return this.link_depth;
    }
    
    public String toString()
    {
    	return ">"+this.link_depth+"|entryID: "+entry_id+"|linkID: "+link_id+"|URL: "+link_url+" |LinkCount: "+link_count+"|HitCount: "+hit_count+"|FLAGS: "+this.flags+"|VISITS: "+visits+"|";
    }
    
}
