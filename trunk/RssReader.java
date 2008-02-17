import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;

import com.sun.cnpi.rss.elements.Item;
import com.sun.cnpi.rss.elements.Rss;
import com.sun.cnpi.rss.parser.RssParser;
import com.sun.cnpi.rss.parser.RssParserFactory;

public class RssReader {
	public static Collection<Item> readRSSDocument(String url) throws Exception {
		RssParser parser = RssParserFactory.createDefault();
		URL feedUrl = new URL(url);
		URLConnection urlc=feedUrl.openConnection();
		urlc.setRequestProperty("User-Agent","");
		urlc.connect();
		Rss rss = parser.parse(urlc.getInputStream());
		return rss.getChannel().getItems();
	}

	public static void main(String[] args) throws Exception {
		int links=0;
		for(Item item : RssReader.readRSSDocument("http://groups.google.com/group/rec.games.roguelike.development/feed/rss_v2_0_msgs.xml")){
			if(links++>3)
				break;
			System.out.println("Title: " + item.getTitle());
			System.out.println("Link: " + item.getLink());
		}
	}
}
