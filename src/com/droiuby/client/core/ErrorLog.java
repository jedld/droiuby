package com.droiuby.client.core;

import java.util.Date;

public class ErrorLog {

	public static final int DEBUG = 0;
	public static final int WARN = 1;
	public static final int ERROR = 2;
	
	Date timestamp;
	int logLevel;

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	String message;

}
