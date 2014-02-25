package com.droiuby.launcher;

public class Options {

	boolean overwrite;
	boolean newActivity;
	boolean newRuntime;
	
	WebConsoleInterface console;

	public WebConsoleInterface getConsole() {
		return console;
	}

	public void setConsole(WebConsoleInterface console) {
		this.console = console;
	}

	public boolean isNewRuntime() {
		return newRuntime;
	}

	public void setNewRuntime(boolean newRuntime) {
		this.newRuntime = newRuntime;
	}

	public boolean isCloseParentActivity() {
		return closeParentActivity;
	}

	public void setCloseParentActivity(boolean closeParentActivity) {
		this.closeParentActivity = closeParentActivity;
	}

	boolean closeParentActivity;

	public Options() {
		overwrite = true;
		newActivity = true;
		newRuntime = false;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public boolean isNewActivity() {
		return newActivity;
	}

	public void setNewActivity(boolean newActivity) {
		this.newActivity = newActivity;
	}


}
