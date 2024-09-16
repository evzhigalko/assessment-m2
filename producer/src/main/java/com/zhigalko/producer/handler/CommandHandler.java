package com.zhigalko.producer.handler;

import com.zhigalko.producer.command.Command;

public interface CommandHandler<T extends Command> {
	void handle(T command);
	Class<T> getCommandClass();
}
