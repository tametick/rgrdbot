import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jibble.pircbot.PircBot;
import com.sun.cnpi.rss.elements.Item;

import java.io.*;

public class Joe6bot extends PircBot {
	//TODO:
	//-
	//-decide which new commands to add
	
	//IDEAS:
	//-<syntax_tn> maybe it would've been easier if the bot did variable expansion like: !expand "about {random(100)}% of all started roguelikes are finished"
	//-<syntax_tn> it'd be interesting to play a roguelike through an irc bot, too, but compared to playing interactive fiction on irc, a roguelike display may result in extreme flooding
  //	<dabreegster> what do y'all think about a system that lets you create interactive fiction / a roguelike interactively, eg in mid gameplay?
  //	<dabreegster> it'd work more easily for text adventures. you go north in a room with no north exit and get the option to make a new room
  
  //HISTORY:
  //before 14.02.2008: joe6pack made the first version with the framework and "news sources", "news" and "time" commands
  //14.02.2008: bot goes to RotateMe
  //14.02.2008: chat logging and "quote", "getquote" and "decide" commands
  //14.02.2008: Comments section TODO, IDEAS and HISTORY added, added some missing comments on the sourcecode
  //15.02.2008: Changed the format of the message logging, added a log entry when the date changes
  //15.02.2008: Implemented the following from the TODO list:
			//-when onMessage, check for a changed date and print the new date if the date has changed
			//-only log the time in front of messages
			//-log stuff the bot says by replacing the sendMessage() calls with writeMessage() calls that
			//	logs the message before sending it.
			//-case insensitivity for quote keywords
	//15.02.2008: added the "randomquote" command that sends a random quote to the chat
	//15.02.2008: added the "commands" command that lists all commands and adds the note that everybody may add his own
	
	//Public declarations
	public Map<String, String> feeds = new HashMap<String, String>();
	public FileWriter          logWriter;     //The file handler for the chatlog
	public PrintWriter         logOutput;     //Printer for the chatlog
	public SimpleDateFormat    myDateFormat;  //the format for dates
	public SimpleDateFormat    myTimeFormat;  //the format for time
	public Date                curDate;       //the current date of the day

	private String             nickName;      //Take a guess

	//Constructor
	public Joe6bot( String nickName ) {
		this.setName( nickName );
		feeds.put("rt news"        , "http://www.roguetemple.com/feed/");
		feeds.put("rgrd news"      , "http://groups.google.com/group/rec.games.roguelike.development/feed/rss_v2_0_msgs.xml");
		feeds.put("rl news"        , "http://rogue-life.ourden.org/rss.xml");
		feeds.put("delicious news" , "http://feeds.delicious.com/rss/tag/roguelike");
		feeds.put("gsw news"       , "http://feed43.com/atplay.xml");
		feeds.put("rgra news"      , "http://groups.google.com/group/rec.games.roguelike.announce/feed/rss_v2_0_msgs.xml");
		// Initialize the chatlog:
		try {
			logWriter = new FileWriter("log.txt" , true);
			logOutput = new PrintWriter( logWriter );
		} catch (Exception e) {
			System.out.println( "Could not open log.txt!" );
		}
		//Print current date and time on top of the log
		String logtime = new java.util.Date().toString();
		logOutput.println("+++ Start of log: "+logtime+" +++");
		//initialize the date and time formatting
		myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
		myTimeFormat = new SimpleDateFormat("hh:mm:ss");
		//save the current date; when this changes, print the new date in the log
		curDate = new Date();
	}

	//The message event: search the messages for commands
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		
		// Check for a changed date, when this has changed, print the new date and save it as curDate
		Date thisDate = new Date();
		String myDateString = myDateFormat.format(thisDate);
		String curDateString = myDateFormat.format(curDate);
		System.out.println("myDateString: >"+myDateString+"<");
		System.out.println("curDateString: >"+curDateString+"<");
		if (!myDateString.equals(curDateString)) {
			//The date has changed
			logOutput.println( "+++ The date has changed: "+myDateFormat.format(thisDate)+" +++" );
			curDate = thisDate;
		}
		
		// Log this message to log.txt (by RotateMe)
		logOutput.println( "("+myTimeFormat.format(thisDate)+")"+"<"+sender+">"+message );
		logOutput.flush();
		
		//Check the message for bot-commands:
		
		//commands: print all commands with syntax in the chat. ADD HERE WHEN YOU ADD COMMANDS!!!
		if ( message.equalsIgnoreCase("commands") || message.equalsIgnoreCase("help?") ) {
			
			writeMessage( channel, "Available commands:");
			writeMessage( channel, "time (by joe6pack)" );
			writeMessage( channel, "[subject] news (by joe6pack)" );
			writeMessage( channel, "news sources (by joe6pack)" );
			writeMessage( channel, "decide [option1],[option2] (by RotateMe)" );
			writeMessage( channel, "quote [keyword],[quote] (by RotateMe)" );
			writeMessage( channel, "getquote [keyword] (by RotateMe)" );
			writeMessage( channel, "randomquote (by RotateMe)" );
			writeMessage( channel, "roll (by konijn)" );
		
		//time: the bot prints the current time (by joe6pack)
		} else if (message.equalsIgnoreCase("time")) {
			String time = new java.util.Date().toString();
			writeMessage(channel, sender + ": The time is now " + time);
			
		//news sources: (by joe6pack)
		} else if (message.equalsIgnoreCase("news sources")) {
			for (String src : feeds.keySet()) {
				writeMessage(channel, src + ": " + feeds.get(src));
			}
			
		// news: (by joe6pack)
		} else if (message.endsWith("news")) {
			try {
				Collection<Item> items = RssReader.readRSSDocument(feeds.get(message));
				if (items == null || items.isEmpty())
					writeMessage(channel, "no news there");

				int links = 0;
				for (Item item : items) {
					if (links++ > 3)
						break;
					writeMessage(channel, item.getTitle().toString().replaceAll(
							"“", "\"").replaceAll("”", "\""));
					if(null!=item.getLink())
						writeMessage(channel, item.getLink().toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		//decide: the bot answers one of two choices, seperated by a ',' (by RotateMe)
		} else if (message.startsWith("decide") && (message.indexOf(",")>0) ) {
			int decision = (int)(Math.random()*2.0) + 1;
			if (decision  == 1 ){
				writeMessage(channel, message.substring( 6 , message.indexOf(",") ));
			} else {
				writeMessage(channel, message.substring( message.indexOf(",")+1, message.length()));
			}
			
		//quote: saves a quote to a given keyword
		} else if (message.startsWith("quote") && (message.indexOf(",")>0) ) {
			FileWriter quoteWrite = null;
			PrintWriter quoteOut = null;
			try{
				quoteWrite = new FileWriter("quotes.txt" , true);
				//|_->needed so that you can append quotes instead of overwriting the file (by RotateMe)
				quoteOut = new PrintWriter(quoteWrite);
			} catch (Exception e) {}
			String newkeyword = message.substring( 6 , message.indexOf(","));
			String newquote = message.substring( message.indexOf(",")+1 );
			quoteOut.println( "["+newkeyword+ "] "+newquote);
			quoteOut.close();
			
		//getquote: recall all quotes to a given keyword (by RotateMe)
		} else if (message.startsWith("getquote") ) {
			BufferedReader quoteReader = null;
			try{
				quoteReader = new BufferedReader ( new FileReader ("quotes.txt") );
			} catch (Exception e) {}
			String keyword = message.substring(9);
			keyword = keyword.toLowerCase();
			int counter = 0;
			String quoteLine = "";
			try{
				quoteLine = quoteReader.readLine();
				while (quoteLine != null ) {
					String thisQuote=quoteLine.toLowerCase();
					if (quoteLine.startsWith("["+keyword+"]")) {
						writeMessage(channel, keyword + "(" + counter + "): " + quoteLine.substring( keyword.length() + 3 ) );
						counter = counter + 1;
					}
					quoteLine = quoteReader.readLine();
				}
				quoteReader.close();
			} catch (Exception e) {}
		//roll: roll a set of dice with some analysis
		} else if (message.startsWith("roll") ) {
			//Does the text after "roll " ( hence 5 chars ) match the regular expression for a dice roll like 3d6 ?
			Matcher matcher = Pattern.compile( "([0-9]{1,2})?(d|D)([0-9]{1,3})((\\+|-)([0-9]{1,3}))?" ).matcher( message.substring( 5 ) );
			//group methods only work if you first call the method matches
			if( matcher.matches() ){
				//The first dice can be missing, in that case we assume 1 for dice count
				int diceCount = matcher.group(1)==null?1:Integer.parseInt( matcher.group(1) );
				int diceSides = Integer.parseInt( matcher.group(3) );
				int result =0;
				for( int i = 0 ; i < diceCount ; i++ ){
					result = result + (int)( Math.random() * diceSides ) + 1;
				}
				//Hack, if diceSides is 0, the result should be 0
				if (diceSides==0)  result = 0;
				writeMessage( channel , String.valueOf( result ) );
			}
		//randomquote: send a random quote plus keyword to the chatroom (by RotateMe)
		} else if (message.equalsIgnoreCase("randomquote") ) {
			//first: scan through the file to see how many quotes there are
			BufferedReader quoteReader = null;
			String quoteLine = "";
			
			try{
				quoteReader = new BufferedReader ( new FileReader ("quotes.txt") );
			} catch (Exception e) {}
			int nrLines = 0;
			if (quoteReader == null) {
				System.out.println("File quotes.txt could not be openend.");
			} else {
				try{
					quoteLine = quoteReader.readLine();
					while (quoteLine != null ) {
						nrLines = nrLines + 1;
						quoteLine = quoteReader.readLine();
					}
					quoteReader.close();
					quoteReader = null;
				} catch (Exception e) {}
				
				//then: choose a random line and send that quote
				String lquoteLine = "I have no quotes :(";
				int quoteNr = (int)(Math.random()*nrLines);
				try{
					quoteReader = new BufferedReader ( new FileReader ("quotes.txt") );
				} catch (Exception e) {}
				if (quoteReader != null) {
					for (int i = 0; i <= quoteNr; i++) {
						try {
							lquoteLine = quoteLine;
							quoteLine = quoteReader.readLine();
						} catch (Exception e) {}
					}
					try {
						quoteReader.close();
					} catch (Exception  e) {}
					if (quoteLine == null) quoteLine = lquoteLine;
					String keyword = quoteLine.substring( 1 , quoteLine.indexOf("]") );
					writeMessage(channel, keyword + ": " + quoteLine.substring( keyword.length() + 3 ) );
				}
			}
		}
	}
	
	
	//The bot sends a message to the channel (by RotateMe)
	public void writeMessage( String channel, String message ) {
		Date thisDate = new Date();
		// Log this message to log.txt because the bots messages won't be logged otherwise
		logOutput.println( "("+myTimeFormat.format(thisDate)+")<"+nickName+">"+message );
		logOutput.flush();
		sendMessage(channel, message);
	}
	
	
}
