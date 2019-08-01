package consensus.research.fsm;

import java.util.Map;
import java.util.Map.Entry;

import akka.actor.ActorRef;

public class MkLog {

	public static String makelog(String text) {
		return "<<     " + text.toUpperCase() + "    >>";
	}
	
	public static String  printMap(Map<ActorRef,Integer> map) {
		StringBuilder builder=new StringBuilder();
		builder.append("<<");
		for(Entry<ActorRef, Integer> en: map.entrySet()) {
			builder.append(en.getKey()+" : "+ en.getValue());
			builder.append(",");
		}
		builder.append(">>");
		return builder.toString();
	}
	
}
