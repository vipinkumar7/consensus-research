package consensus.research.messages;

import java.util.List;

import akka.actor.ActorRef;
import consensus.research.election.Term;
import consensus.research.log.Entry;

public class AppendEntries extends Message {

	private Term term;
	private ActorRef leaderId;
	private int prevLogIndex;
	private Term prevLogTerm;
	private List<Entry> entries;
	private int leaderCommit;

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
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public ActorRef getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(ActorRef leaderId) {
		this.leaderId = leaderId;
	}

	public int getPrevLogIndex() {
		return prevLogIndex;
	}

	public void setPrevLogIndex(int prevLogIndex) {
		this.prevLogIndex = prevLogIndex;
	}

	public Term getPrevLogTerm() {
		return prevLogTerm;
	}

	public void setPrevLogTerm(Term prevLogTerm) {
		this.prevLogTerm = prevLogTerm;
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	public int getLeaderCommit() {
		return leaderCommit;
	}

	public void setLeaderCommit(int leaderCommit) {
		this.leaderCommit = leaderCommit;
	}

}
