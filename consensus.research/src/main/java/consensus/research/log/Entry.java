package consensus.research.log;

import consensus.research.election.Term;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Entry {
	public String command;
	public Term term;
	public InternalClientRef client;

	public Entry(String command, Term term, InternalClientRef client) {
		
		this.command = command;
		this.term = term;
		this.client = client;
	}

}
