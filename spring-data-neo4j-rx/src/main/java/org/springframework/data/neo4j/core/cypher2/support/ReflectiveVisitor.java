package org.springframework.data.neo4j.core.cypher2.support;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ReflectiveVisitor implements Visitor {

	private enum Phase {
		ENTER("enter"),
		LEAVE("leave");

		final String methodName;

		Phase(String methodName) {
			this.methodName = methodName;
		}
	}

	private final Map<TargetAndPhase, Optional<MethodHandle>> cachedHandles = new ConcurrentHashMap<>();

	protected abstract void preEnter(Visitable visitable);

	@Override
	public final void enter(Visitable visitable) {
		preEnter(visitable);
		executeConcreteMethodIn(new TargetAndPhase(visitable.getClass(), Phase.ENTER), visitable);
	}

	@Override
	public final void leave(Visitable visitable) {
		executeConcreteMethodIn(new TargetAndPhase(visitable.getClass(), Phase.LEAVE), visitable);
		postLeave(visitable);
	}

	protected abstract void postLeave(Visitable visitable);

	void executeConcreteMethodIn(TargetAndPhase targetAndPhase, Visitable onVisitable) {
		Optional<MethodHandle> optionalHandle = this.cachedHandles.computeIfAbsent(targetAndPhase, this::findHandleFor);
		optionalHandle.ifPresent(handle -> {
			try {
				handle.invokeWithArguments(onVisitable);
			} catch (Throwable throwable) {
			}
		});
	}

	Optional<MethodHandle> findHandleFor(TargetAndPhase targetAndPhase) {

		try {
			// Using MethodHandles.lookup().findVirtual() doesn't allow to make a protected method accessible.
			Method method = this.getClass()
				.getDeclaredMethod(targetAndPhase.phase.methodName, targetAndPhase.classOfVisitable);
			method.setAccessible(true);
			return Optional.of(MethodHandles.lookup().in(this.getClass()).unreflect(method).bindTo(this));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// We don't do anything if the method doesn't exists
			return Optional.empty();
		}
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	static class TargetAndPhase {
		private final Class<? extends Visitable> classOfVisitable;

		private final Phase phase;
	}
}
