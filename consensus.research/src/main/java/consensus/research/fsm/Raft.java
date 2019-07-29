package consensus.research.fsm;

import static consensus.research.state.Role.Candidate;
import static consensus.research.state.Role.Follower;
import static consensus.research.state.Role.Initialise;
import static consensus.research.state.Role.Leader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.MutablePair;

import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;
import akka.actor.Props;
import consensus.research.core.Get;
import consensus.research.election.Meta;
import consensus.research.election.Term;
import consensus.research.election.Votes;
import consensus.research.log.Entry;
import consensus.research.log.InternalClientRef;
import consensus.research.messages.AppendEntries;
import consensus.research.messages.AppendFailure;
import consensus.research.messages.AppendReply;
import consensus.research.messages.AppendSuccess;
import consensus.research.messages.ClientRequest;
import consensus.research.messages.DenyVote;
import consensus.research.messages.GrantVote;
import consensus.research.messages.Heartbeat;
import consensus.research.messages.Init;
import consensus.research.messages.Message;
import consensus.research.messages.MessageType;
import consensus.research.messages.RequestVote;
import consensus.research.messages.TimeOut;
import consensus.research.messages.Vote;
import consensus.research.state.Role;
import scala.concurrent.duration.Duration;

/**
 * 
 * @author kvipin
 *
 */
public class Raft extends AbstractLoggingFSM<Role, Meta> {

	@Override
	public int logDepth() {

		return 12;
	}

	public static Props props() {
		return Props.create(Raft.class, Raft::new);
	}

	{
		startWith(Initialise, new Meta(new ArrayList<>()));

		when(Initialise, matchEvent(Init.class, Meta.class, (cluster, data) -> goTo(Follower).using(initialised(cluster))));

		when(Follower, matchEvent(Message.class, Meta.class, (rpc, data) -> eventMatcher_Follower(rpc, data)));

		when(Candidate, matchEvent(Message.class, Meta.class, (rpc, data) -> eventMatcher_Candidate(rpc, data)));

		when(Leader, matchEvent(Message.class, Meta.class, (rpc, data) -> eventMatcher_Leader(rpc, data)));

		whenUnhandled(matchEvent(Message.class, Meta.class, (rpc, data) -> stay()));

		onTransition(matchState(Leader, Follower, () -> {
			cancelTimer("heartbeat");
			resetTimer();
		})

				.state(Candidate, Follower, () -> {
					resetTimer();
				})

				.state(Role.Initialise, Role.Follower, () -> {
					resetTimer();
				}));

		onTermination(matchStop(Normal(), (state, data) -> {

		}).stop(Shutdown(), (state, data) -> {

		}).stop(Failure.class, (reason, state, data) -> {
			String lastEvents = getLog().mkString("\n\t");
			log().warning("Failure in state " + state + " with data " + data + " due to $cause"
					+ "Events leading up to this: \n\t " + lastEvents);
		}));
	}

	public State<Role, Meta> eventMatcher_Follower(Message rpc, Meta data) {
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
			AppendReply msg = append((AppendEntries) rpc, data);
			return stay().using(data).replying(msg);
		}

		if (rpc instanceof ClientRequest) {
			forwardRequest((ClientRequest) rpc, data);
			return stay();
		}

		if (rpc instanceof TimeOut)
			return goTo(Role.Candidate).using(preparedForCandidate(data));
		return stay();
	}

	public State<Role, Meta> eventMatcher_Candidate(Message rpc, Meta data) {

		// voting events
		if (rpc instanceof GrantVote) {
			data.setVotes(data.getVotes().gotVoteFrom(sender()));
			if (data.getVotes().majority(data.getNodes().size())) {
				return goTo(Leader).using(preparedForLeader(data));
			} else
				return stay().using(data);
		}

		if (rpc instanceof DenyVote) {
			DenyVote denyVote = (DenyVote) rpc;
			if (denyVote.getTerm().compareTo(data.getTerm()) == 1) {
				data.selectTerm(denyVote.getTerm());
				return goTo(Follower).using(preparedForFollower(data));
			} else
				return stay();

		}

		// others

		if (rpc instanceof AppendEntries) {
			data.setLeader(((AppendEntries) rpc).getLeaderId());
			AppendReply msg = append((AppendEntries) rpc, data);
			return goTo(Follower).using(preparedForFollower(data)).replying(msg);

		}

		if (rpc instanceof ClientRequest) {
			forwardRequest((ClientRequest) rpc, data);
			return stay();
		}

		if (rpc instanceof TimeOut) {
			return goTo(Candidate).using(preparedForCandidate(data));
		}
		return stay();
	}

	public State<Role, Meta> eventMatcher_Leader(Message rpc, Meta data) {

		if (rpc instanceof ClientRequest) {
			writeToLog(sender(), (ClientRequest) rpc, data);
			sendEntries(data);
			return stay().using(data);
		}

		if (rpc instanceof AppendSuccess) {
			data.setLog(data.getLog().resetNextFor(sender()));
			data.setLog(data.getLog().matchFor(sender(), ((AppendSuccess) rpc).getIndex()));
			leaderCommitEntries((AppendSuccess) rpc, data);
			applyEntries(data);
			return stay();
		}

		if (rpc instanceof AppendFailure) {
			AppendFailure rpc_apnd = (AppendFailure) rpc;
			if (rpc_apnd.getTerm().compareTo(data.getTerm()) <= 1) {
				data.setLog(data.getLog().decrementNextFor(sender()));
				return stay();
			} else {
				data.setTerm(rpc_apnd.getTerm());
				goTo(Follower).using(preparedForFollower(data));
			}
		}

		if (rpc instanceof Heartbeat) {
			sendEntries(data);
			return stay();
		}
		return stay();

	}

	private void applyEntries(Meta data) {
		for (int i = data.getLog().getLastApplied(); i <= data.getLog().getCommitIndex(); i++) {
			Entry entry = data.getLog().getEntries().get(i);
			int result = data.getRsm().execute(new Get()); // TODO: make generic
			data.setLog(data.getLog().applied());

			InternalClientRef ref = entry.getClient();
			if (ref != null) {
				ref.getSender().tell(ref.getCid(), sender());// TODO
			}
		}
	}

	private void leaderCommitEntries(AppendSuccess rpc, Meta data) {
		if (rpc.getIndex() >= data.getLog().getCommitIndex()
				&& data.getLog().getEntries().termOf(rpc.getIndex()).compareTo(data.getTerm()) == 1) {
			Map<ActorRef, Integer> matches = data.getLog().getMatchIndex();
			int size = (int) matches.values().stream().filter(x -> (x == rpc.getIndex())).count();
			if (size >= Math.ceil(data.getNodes().size() / 2.0))
				data.setLog(data.getLog().commit(rpc.getIndex()));
		}
	}

	private void writeToLog(ActorRef sender, ClientRequest rpc, Meta data) {
		InternalClientRef ref = new InternalClientRef(sender, rpc.getCid());
		Entry entry = new Entry(rpc.getCommand(), data.getTerm(), ref);
		List<Entry> vec = new ArrayList<>();
		vec.add(entry);
		data.leaderAppend(self(), vec);
	}

	private Meta preparedForFollower(Meta state) {
		state.setVotes(new Votes());
		return state;
	}

	private Meta preparedForLeader(Meta state) {
		System.out.println("Elected to leader for term: ${state.term}");
		Map<ActorRef, Integer> nexts = state.getLog().getNextIndex();

		Map<ActorRef, Integer> nexts_new = new HashMap<ActorRef, Integer>();
		Set<ActorRef> all_keys = nexts.keySet();
		for (ActorRef en : all_keys) {
			nexts_new.put(en, state.getLog().getEntries().lastIndex() + 1);
		}

		Map<ActorRef, Integer> matches = state.getLog().getMatchIndex();
		Map<ActorRef, Integer> matches_new = new HashMap<ActorRef, Integer>();

		all_keys = matches.keySet();
		for (ActorRef en : all_keys) {
			matches_new.put(en, 0);
		}
		state.setLog(state.getLog().copy(nexts_new, matches_new));
		sendEntries(state);
		return state;
	}

	private void sendEntries(Meta data) {
		resetHeartbeatTimer();
		List<ActorRef> allrefs = data.getNodes();
		for (ActorRef ref : allrefs) {
			if (ref != getSelf()) {
				AppendEntries message = compileMessage(ref, data);
				ref.tell(message, sender());
			}
		}

	}

	private AppendEntries compileMessage(ActorRef node, Meta data) {
		int prevIndex = data.getLog().getNextIndex().get(node) - 1;
		Term prevTerm = data.getLog().getEntries().termOf(prevIndex);
		int fromMissing = missingRange(data.getLog().getEntries().lastIndex(), prevIndex);
		return new AppendEntries(data.getTerm(), self(), prevIndex, prevTerm,
				data.getLog().getEntries().takeRight(fromMissing), data.getLog().getCommitIndex());
	}

	private Integer missingRange(Integer lastIndex, Integer prevIndex) {
		if (prevIndex == 0)
			return 1;
		else
			return (lastIndex - prevIndex);
	}

	public void forwardRequest(ClientRequest rpc, Meta data) {
		if (data.getLeader() != null)
			data.getLeader().forward(rpc, getContext());

	}

	private Meta preparedForCandidate(Meta data) {
		data.nextTerm();
		List<ActorRef> ls = new ArrayList<>();
		ls.add(getSelf());
		data.setVotes(new Votes(getSelf(), ls));
		List<ActorRef> allrefs = data.getNodes();

		for (ActorRef ref : allrefs) {
			if (ref != getSelf()) {
				ref.tell(new RequestVote(data.getTerm(), getSelf(), data.getLog().getEntries().lastIndex(),
						data.getLog().getEntries().lastTerm()), sender());
			}
		}
		resetTimer();
		return data;
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
		data.setLog(data.getLog().commit(rpc.getLeaderCommit()));
		followerApplyEntries(data);
		data.selectTerm(rpc.getTerm().copy());

		return new AppendSuccess(data.getTerm(), data.getLog().getEntries().lastIndex());
	}

	private void followerApplyEntries(Meta data) {
		for (int i = data.getLog().getLastApplied(); i <= data.getLog().getCommitIndex(); i++) {
			Entry entry = data.getLog().getEntries().get(i);
			data.getRsm().execute(new Get()); // TODO: make generic
			data.setLog(data.getLog().applied());
		}
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
