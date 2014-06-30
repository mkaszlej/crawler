package parser;

import org.jsoup.nodes.Document;

public class ParserFactory {
	
	public enum ParserType {
        PatternParser
    }
 
    public static Parser createParser(ParserType parserType, Document data) {
        switch (parserType) {
            case PatternParser:
                return new PatternParser(data);
        }
        throw new IllegalArgumentException("Parser type " + parserType + " is not recognized.");
    }
}
