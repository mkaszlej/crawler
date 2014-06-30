package parser;

import org.jsoup.nodes.Document;

public abstract class Parser {
	protected String data;
	protected Document htmlData;
	
	Parser(Document htmlData){
	    this.htmlData = htmlData;
	    this.data = htmlData.getAllElements().toString();
	}
	
    public abstract void parse();
}
