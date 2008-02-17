import java.io.IOException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

public class Joe6botMain {
	public static void main(String[] args) throws NickAlreadyInUseException, IOException, IrcException {

		String nickName;

		//Name can be passed as a parameter, otherwise we take the default joe6bot
		if (args.length > 0){
			nickName = args[0] ;
		}else{
			nickName = "joe6bot";
		}

		Joe6bot bot = new Joe6bot( nickName );

		bot.setVerbose(true);

		bot.changeNick( nickName );
		
		bot.connect("irc.quakenet.org");
		bot.joinChannel("#joetest");
		bot.joinChannel("#rgrd");
	}
}
