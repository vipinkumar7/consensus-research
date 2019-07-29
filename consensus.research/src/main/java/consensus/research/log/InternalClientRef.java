package consensus.research.log;

import akka.actor.ActorRef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalClientRef {

	private ActorRef sender;
	private Integer cid;
	public InternalClientRef(ActorRef sender, Integer cid) {
	}
}
