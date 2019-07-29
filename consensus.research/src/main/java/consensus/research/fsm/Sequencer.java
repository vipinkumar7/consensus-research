package consensus.research.fsm;

import java.util.concurrent.TimeUnit;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.Timeout;
import consensus.research.messages.ClientRequest;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Sequencer extends AbstractLoggingActor {

	private int cid = 0;
	private Timeout tc = new Timeout(Duration.create(2, TimeUnit.SECONDS));

	private ActorSelection raftMember = getContext().actorSelection("/user/member*");

	public Future<Object> decide(String command) {
		Future<Object> member = Patterns.ask(raftMember, new ClientRequest(tick(), command), tc);
		return member;
	}

	public int tick() {
		cid++;
		return cid;
	}

	public static Props props() {
		return Props.create(Sequencer.class, Sequencer::new);
	}

	@Override
	public void preStart() throws Exception {

		getContext().system().scheduler().scheduleOnce(Duration.create(50L, TimeUnit.MILLISECONDS), getSelf(),
				"sequence", getContext().dispatcher(), null);
	}

	@Override
	public void postRestart(Throwable reason) throws Exception {
		super.postRestart(reason);
	}

	@Override
	public Receive createReceive() {
		Receive build = receiveBuilder().matchEquals("sequence", message -> {
			Future<Object> member = decide("get");
			member.onComplete(new OnComplete<Object>() {

				@Override
				public void onComplete(Throwable failure, Object success) throws Throwable {
					if (failure == null) {
						log().info("GOT :: " + (Integer) success);
					} else {
						log().info("GOT ERROR " + failure.getMessage());
					}

				}

			}

					, getContext().dispatcher());

		}).build();

		getContext().system().scheduler().scheduleOnce(Duration.create(50L, TimeUnit.MILLISECONDS), getSelf(),
				"sequence", getContext().dispatcher(), null);
		return build;

	}

}
