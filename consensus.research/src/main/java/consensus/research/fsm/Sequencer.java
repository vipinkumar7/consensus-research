package consensus.research.fsm;

import java.util.concurrent.TimeUnit;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.Timeout;
import consensus.research.messages.ClientRequest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Sequencer extends AbstractLoggingActor {

	private int cid = 0;
	private Timeout tc = new Timeout(Duration.create(2, TimeUnit.SECONDS));

	private Future<ActorRef> raftMember = getContext().actorSelection("/user/member*").resolveOne(tc);

	public Future<Object> decide(String command) throws Exception {

		ActorRef ref = Await.result(raftMember, Duration.create(2, TimeUnit.MICROSECONDS));
		log().info("Client Request send to  actor " + ref.toString());
		Future<Object> member = Patterns.ask(ref, new ClientRequest(tick(), command), tc);
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

		getContext().system().scheduler().scheduleOnce(Duration.create(50, TimeUnit.MILLISECONDS), getSelf(),
				"sequence", getContext().dispatcher(), null);
	}

	@Override
	public void postRestart(Throwable reason) throws Exception {

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

			}, getContext().dispatcher());

			getContext().system().scheduler().scheduleOnce(Duration.create(50, TimeUnit.MILLISECONDS), getSelf(),
					"sequence", getContext().dispatcher(), null);

		}).build();

		return build;

	}

}
