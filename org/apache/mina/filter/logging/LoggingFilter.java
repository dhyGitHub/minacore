/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.logging;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingFilter extends IoFilterAdapter {
	private final String name;
	private final Logger logger;
	private LogLevel exceptionCaughtLevel;
	private LogLevel messageSentLevel;
	private LogLevel messageReceivedLevel;
	private LogLevel sessionCreatedLevel;
	private LogLevel sessionOpenedLevel;
	private LogLevel sessionIdleLevel;
	private LogLevel sessionClosedLevel;

	public LoggingFilter() {
		this(LoggingFilter.class.getName());
	}

	public LoggingFilter(Class<?> clazz) {
		this(clazz.getName());
	}

	public LoggingFilter(String name) {
		this.exceptionCaughtLevel = LogLevel.WARN;
		this.messageSentLevel = LogLevel.INFO;
		this.messageReceivedLevel = LogLevel.INFO;
		this.sessionCreatedLevel = LogLevel.INFO;
		this.sessionOpenedLevel = LogLevel.INFO;
		this.sessionIdleLevel = LogLevel.INFO;
		this.sessionClosedLevel = LogLevel.INFO;
		if (name == null) {
			this.name = LoggingFilter.class.getName();
		} else {
			this.name = name;
		}

		this.logger = LoggerFactory.getLogger(this.name);
	}

	public String getName() {
		return this.name;
	}

	private void log(LogLevel eventLevel, String message, Throwable cause) {
		switch (LoggingFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$filter$logging$LogLevel[eventLevel
				.ordinal()]) {
		case 1:
			this.logger.trace(message, cause);
			return;
		case 2:
			this.logger.debug(message, cause);
			return;
		case 3:
			this.logger.info(message, cause);
			return;
		case 4:
			this.logger.warn(message, cause);
			return;
		case 5:
			this.logger.error(message, cause);
			return;
		default:
		}
	}

	private void log(LogLevel eventLevel, String message, Object param) {
		switch (LoggingFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$filter$logging$LogLevel[eventLevel
				.ordinal()]) {
		case 1:
			this.logger.trace(message, param);
			return;
		case 2:
			this.logger.debug(message, param);
			return;
		case 3:
			this.logger.info(message, param);
			return;
		case 4:
			this.logger.warn(message, param);
			return;
		case 5:
			this.logger.error(message, param);
			return;
		default:
		}
	}

	private void log(LogLevel eventLevel, String message) {
		switch (LoggingFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$filter$logging$LogLevel[eventLevel
				.ordinal()]) {
		case 1:
			this.logger.trace(message);
			return;
		case 2:
			this.logger.debug(message);
			return;
		case 3:
			this.logger.info(message);
			return;
		case 4:
			this.logger.warn(message);
			return;
		case 5:
			this.logger.error(message);
			return;
		default:
		}
	}

	public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
		this.log(this.exceptionCaughtLevel, "EXCEPTION :", cause);
		nextFilter.exceptionCaught(session, cause);
	}

	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		this.log(this.messageReceivedLevel, "RECEIVED: {}", message);
		nextFilter.messageReceived(session, message);
	}

	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		this.log(this.messageSentLevel, "SENT: {}", writeRequest.getOriginalRequest().getMessage());
		nextFilter.messageSent(session, writeRequest);
	}

	public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
		this.log(this.sessionCreatedLevel, "CREATED");
		nextFilter.sessionCreated(session);
	}

	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		this.log(this.sessionOpenedLevel, "OPENED");
		nextFilter.sessionOpened(session);
	}

	public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
		this.log(this.sessionIdleLevel, "IDLE");
		nextFilter.sessionIdle(session, status);
	}

	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		this.log(this.sessionClosedLevel, "CLOSED");
		nextFilter.sessionClosed(session);
	}

	public void setExceptionCaughtLogLevel(LogLevel level) {
		this.exceptionCaughtLevel = level;
	}

	public LogLevel getExceptionCaughtLogLevel() {
		return this.exceptionCaughtLevel;
	}

	public void setMessageReceivedLogLevel(LogLevel level) {
		this.messageReceivedLevel = level;
	}

	public LogLevel getMessageReceivedLogLevel() {
		return this.messageReceivedLevel;
	}

	public void setMessageSentLogLevel(LogLevel level) {
		this.messageSentLevel = level;
	}

	public LogLevel getMessageSentLogLevel() {
		return this.messageSentLevel;
	}

	public void setSessionCreatedLogLevel(LogLevel level) {
		this.sessionCreatedLevel = level;
	}

	public LogLevel getSessionCreatedLogLevel() {
		return this.sessionCreatedLevel;
	}

	public void setSessionOpenedLogLevel(LogLevel level) {
		this.sessionOpenedLevel = level;
	}

	public LogLevel getSessionOpenedLogLevel() {
		return this.sessionOpenedLevel;
	}

	public void setSessionIdleLogLevel(LogLevel level) {
		this.sessionIdleLevel = level;
	}

	public LogLevel getSessionIdleLogLevel() {
		return this.sessionIdleLevel;
	}

	public void setSessionClosedLogLevel(LogLevel level) {
		this.sessionClosedLevel = level;
	}

	public LogLevel getSessionClosedLogLevel() {
		return this.sessionClosedLevel;
	}
}