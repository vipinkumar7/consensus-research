package consensus.research.messages;

import consensus.research.election.Term;

public class GrantVote extends Vote{
	private Term term;
	public GrantVote(Term term) {
		this.term=term;
	}
	public Term getTerm() {
		return term;
	}
	public void setTerm(Term term) {
		this.term = term;
	}
}
