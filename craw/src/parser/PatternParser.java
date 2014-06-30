package parser;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

public class PatternParser extends Parser {
	
	public PatternParser(Document htmlData) {
	    super(htmlData);
	}
	
    public void parse(){
        Pattern pattern = Pattern.compile("\\s+[0-9]{2}-[0-9]{3}\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(data);
        HashSet<String> set = new HashSet<String>();
        
        // check all occurences
        while (matcher.find()) {
          set.add(matcher.group());
        }
        
        System.out.print(set);
    }
}
