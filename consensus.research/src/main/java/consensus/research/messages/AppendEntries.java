package consensus.research.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import akka.actor.ActorRef;
import consensus.research.election.Term;
import consensus.research.log.Entry;

public class AppendEntries extends Message {

	private final Term term;
	private final ActorRef leaderId;
	private final int prevLogIndex;
	private final Term prevLogTerm;
	private final List<Entry> entries;
	private final int leaderCommit;

	public AppendEntries(Term term, ActorRef leaderId, int prevLogIndex, Term prevLogTerm, List<Entry> entries,
			int leaderCommit) {
		this.term = term;
		this.leaderCommit = leaderCommit;
		this.entries = entries;
		this.prevLogTerm = prevLogTerm;
		this.prevLogIndex = prevLogIndex;
		this.leaderId = leaderId;
	}

	public Term getTerm() {
		return term.copy();
	}

	public ActorRef getLeaderId() {
		return leaderId;
	}

	public int getPrevLogIndex() {
		return prevLogIndex;
	}

	public Term getPrevLogTerm() {
		return prevLogTerm.copy();
	}

	public List<Entry> getEntries() {
		return entries == null ? null : new ArrayList<>(entries);
	}

	public int getLeaderCommit() {
		return leaderCommit;
	}

	@Override
	public String toString() {
		return "Appendentries(" + term.toString() + ", Leader " + leaderId + ", PreviousLogIndex: " + prevLogIndex
				+ " , " + prevLogTerm.toString() + " , Entries" + Arrays.toString(entries.toArray()) + "LeaderCommit: "
				+ leaderCommit + ")";
	}

}
