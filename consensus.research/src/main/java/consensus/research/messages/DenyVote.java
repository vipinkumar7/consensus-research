package consensus.research.messages;

import consensus.research.election.Term;

public class DenyVote extends Vote{

	private Term term;
	public DenyVote(Term term) {
		this.term=term;
	}
	public Term getTerm() {
		return term;
	}
	public void setTerm(Term term) {
		this.term = term;
	}
}
