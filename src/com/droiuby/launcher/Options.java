package com.droiuby.launcher;

public class Options {

	boolean overwrite;
	boolean newActivity;

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
