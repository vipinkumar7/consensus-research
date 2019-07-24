package consensus.research.messages;

import akka.actor.ActorRef;
import consensus.research.election.Term;

public class RequestVote extends Message {
	private Term term;
	private ActorRef candidateId;
	private Integer lastLogIndex;
	private Term lastLogTerm;

	public RequestVote(Term term, ActorRef candidateId, Integer lastLogIndex, Term lastLogTerm) {
		this.term = term;
		this.candidateId = candidateId;
		this.lastLogIndex = lastLogIndex;
		this.lastLogTerm = lastLogTerm;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public ActorRef getCandidateId() {
		return candidateId;
	}

	public void setCandidateId(ActorRef candidateId) {
		this.candidateId = candidateId;
	}

	public Integer getLastLogIndex() {
		return lastLogIndex;
	}

	public void setLastLogIndex(Integer lastLogIndex) {
		this.lastLogIndex = lastLogIndex;
	}

	public Term getLastLogTerm() {
		return lastLogTerm;
	}

	public void setLastLogTerm(Term lastLogTerm) {
		this.lastLogTerm = lastLogTerm;
	}

}
