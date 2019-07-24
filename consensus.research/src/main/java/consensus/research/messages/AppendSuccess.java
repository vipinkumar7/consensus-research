package consensus.research.messages;

import consensus.research.election.Term;

public class AppendSuccess extends AppendReply {

	private Term term;
	private int index;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public AppendSuccess(Term term, int index) {
		this.term = term;
		this.index = index;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}
}
