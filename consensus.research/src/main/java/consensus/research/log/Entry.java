package consensus.research.log;

import consensus.research.election.Term;

public class Entry {
	private final String command;
	private final Term term;
	private final InternalClientRef client;

	public Entry(String command, Term term, InternalClientRef client) {

		this.command = command;
		this.term = term;
		this.client = client;
	}

	public String getCommand() {
		return command;
	}

	public Term getTerm() {
		return term.copy();
	}

	public InternalClientRef getClient() {
		return client;
	}

	@Override
	public String toString() {
		return "[ command: "+ command+" Term: "+term.toString()+ " Client: "+ client+  "]";
	}
}
