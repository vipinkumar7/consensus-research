/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package consensus.research.fsm.simple;

import akka.actor.AbstractFSM;
import akka.japi.pf.UnitMatch;
import consensus.research.fsm.simple.Data;

import static consensus.research.fsm.simple.State.*;
import static consensus.research.fsm.simple.Uninitialized.*;

import java.util.Arrays;
import java.util.LinkedList;
import scala.concurrent.duration.Duration;

//https://downloads.lightbend.com/paradox/akka-docs-new/20170510-wip/java/fsm.html
public class Buncher extends AbstractFSM<State, Data> {
	{
		startWith(Idle, Uninitialized);

		// #when-syntax
		when(Idle, matchEvent(SetTarget.class, Uninitialized.class,
				(setTarget, uninitialized) -> stay().using(new Todo(setTarget.getRef(), new LinkedList<>()))));
		// #when-syntax

		// #transition-elided
		onTransition(matchState(Active, Idle, () -> {
			// reuse this matcher
			final UnitMatch<Data> m = UnitMatch
					.create(matchData(Todo.class, todo -> todo.getTarget().tell(new Batch(todo.getQueue()), self())));
			m.match(stateData());
		}).state(Idle, Active, () -> {
			/* Do something here */}));
		// #transition-elided

		when(Active, Duration.create(1, "second"), matchEvent(Arrays.asList(Flush.class, StateTimeout()), Todo.class,
				(event, todo) -> goTo(Idle).using(todo.copy(new LinkedList<>()))));

		// #unhandled-elided
		whenUnhandled(matchEvent(Queue.class, Todo.class,
				(queue, todo) -> goTo(Active).using(todo.addElement(queue.getObj()))).anyEvent((event, state) -> {
					log().warning("received unhandled request {} in state {}/{}", event, stateName(), state);
					return stay();
				}));
		// #unhandled-elided
		
		initialize();
		// #fsm-body
	}

}