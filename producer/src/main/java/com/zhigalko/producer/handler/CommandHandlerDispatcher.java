package com.zhigalko.producer.handler;

import com.zhigalko.producer.command.Command;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CommandHandlerDispatcher {
	private final Map<Class<? extends Command>, CommandHandler<? extends Command>> handlerMap = new HashMap<>(4);

	public CommandHandlerDispatcher(Set<CommandHandler<? extends Command>> handlers) {
		handlers.forEach(handler -> handlerMap.put(handler.getCommandClass(), handler));
	}

	@SuppressWarnings("unchecked")
	public <T extends Command> void dispatch(T command) {
		CommandHandler<T> commandHandler = (CommandHandler<T>) handlerMap.get(command.getClass());
		commandHandler.handle(command);
	}
}
