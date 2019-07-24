package consensus.research.messages;

import consensus.research.election.Term;

public class AppendFailure extends AppendReply{

	private Term term;
	public AppendFailure(Term term) {
		this.term=term;
	}
	public Term getTerm() {
		return term;
	}
	public void setTerm(Term term) {
		this.term = term;
	}
}
