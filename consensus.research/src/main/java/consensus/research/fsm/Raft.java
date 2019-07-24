package consensus.research.fsm;

import static consensus.research.state.Role.Follower;
import static consensus.research.state.Role.Initialise;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.MutablePair;

import akka.actor.AbstractFSM;
import consensus.research.election.Meta;
import consensus.research.election.Term;
import consensus.research.messages.AppendEntries;
import consensus.research.messages.AppendFailure;
import consensus.research.messages.AppendReply;
import consensus.research.messages.AppendSuccess;
import consensus.research.messages.DenyVote;
import consensus.research.messages.GrantVote;
import consensus.research.messages.Init;
import consensus.research.messages.Message;
import consensus.research.messages.MessageType;
import consensus.research.messages.RequestVote;
import consensus.research.messages.Vote;
import consensus.research.state.Role;
import scala.concurrent.duration.Duration;

public class Raft extends AbstractFSM<Role, Meta> {

	{
		startWith(Initialise, new Meta(new ArrayList<>()));

		when(Initialise, matchEvent(Init.class, Meta.class, (init, data) -> goTo(Follower).using(initialised(init))));

		when(Follower, matchEvent(Message.class, Meta.class, (rpc, data) -> eventMatcher(rpc, data)));

	}

	public State<Role, Meta> eventMatcher(Message rpc, Meta data) {
		if (rpc instanceof RequestVote) {
			MutablePair<Vote, Meta> res = vote((RequestVote) rpc, data);
			if (res.getKey() instanceof GrantVote) {
				resetTimer();
				return stay().using(res.getValue()).replying(res.getKey());
			}
			if (res.getKey() instanceof DenyVote) {
				return stay().using(res.getValue()).replying(res.getKey());
			}
		}

		if (rpc instanceof AppendEntries) {
			data.setLeader(((AppendEntries) rpc).getLeaderId());
			resetTimer();

		}

		return stay().using(data).replying(data);
	}

	/*
	 * AppendEntries handling
	 */
	public AppendReply append(AppendEntries rpc, Meta data) {
		if (leaderIsBehind(rpc, data))
			return appendFail(rpc, data);
		else if (!hasMatchingLogEntryAtPrevPosition(rpc, data))
			return appendFail(rpc, data);
		else
			return appendSuccess(rpc, data);
	}

	public boolean leaderIsBehind(AppendEntries rpc, Meta data) {
		return rpc.getTerm().compareTo(data.getTerm()) == -1;
	}

	private AppendFailure appendFail(AppendEntries rpc, Meta data) {
		data.selectTerm(rpc.getTerm());
		return new AppendFailure(data.getTerm());
	}

	private boolean hasMatchingLogEntryAtPrevPosition(AppendEntries rpc, Meta data) {
		return (rpc.getPrevLogIndex() == 0 || // guards for bootstrap case
				(data.getLog().getEntries().hasEntryAt(rpc.getPrevLogIndex())
						&& (data.getLog().getEntries().termOf(rpc.getPrevLogIndex()) == rpc.getPrevLogTerm())));
	}

	public AppendReply appendSuccess(AppendEntries rpc, Meta data) {
		data.append(rpc.getEntries(), rpc.getPrevLogIndex());
		data.selectTerm(rpc.getTerm().copy());
		return new AppendSuccess(data.getTerm(), data.getLog().getEntries().lastIndex());
	}

	public Meta initialised(Init cluster) {
		return new Meta(cluster.getNodes());
	}

	public void resetHeartbeatTimer() {
		cancelTimer("heartbeat");
		int nextTimeout = (int) ((Math.random() * 100) + 100);
		setTimer("heartbeat", MessageType.HEARTBEAT, Duration.create(nextTimeout, TimeUnit.SECONDS), false);
	}

	public void resetTimer() {
		cancelTimer("timeout");
		int nextTimeout = (int) ((Math.random() * 100) + 200);
		setTimer("timeout", MessageType.TIMEOUT, Duration.create(nextTimeout, TimeUnit.SECONDS), false);
	}

	public MutablePair<Vote, Meta> vote(RequestVote rpc, Meta data) {
		if (alreadyVoted(data)) {
			return deny(rpc, data);
		} else if (rpc.getTerm().compareTo(data.getTerm()) == -1) {
			return deny(rpc, data);
		} else if (rpc.getTerm().compareTo(data.getTerm()) == 1) {
			if (candidateLogTermIsBehind(rpc, data)) {
				return deny(rpc, data);
			} else if (candidateLogTermIsEqualButHasShorterLog(rpc, data)) {
				return deny(rpc, data);
			} else
				return grant(rpc, data);

		} else
			return grant(rpc, data);

	}

	public boolean candidateLogTermIsBehind(RequestVote rpc, Meta data) {
		return data.getLog().getEntries().lastTerm().compareTo(rpc.getLastLogTerm()) == 1;

	}

	private boolean candidateLogTermIsEqualButHasShorterLog(RequestVote rpc, Meta data) {
		return (data.getLog().getEntries().lastTerm().compareTo(rpc.getLastLogTerm()) == 0
				&& (data.getLog().getEntries().lastIndex() - 1 > rpc.getLastLogIndex()));

	}

	public MutablePair<Vote, Meta> deny(RequestVote rpc, Meta data) {

		data.setTerm(Term.max(data.getTerm(), rpc.getTerm()));
		return new MutablePair<>(new DenyVote(data.getTerm()), data);
	}

	public MutablePair<Vote, Meta> grant(RequestVote rpc, Meta data) {
		data.setVotes(data.getVotes().vote(rpc.getCandidateId()));
		data.setTerm(Term.max(data.getTerm(), rpc.getTerm()));
		return new MutablePair<>(new GrantVote(data.getTerm()), data);
	}

	public boolean alreadyVoted(Meta data) {
		if (data.getVotes().getVotedFor() != null)
			return true;
		else
			return false;
	}

}
