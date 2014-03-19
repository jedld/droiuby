package com.droiuby.client.core;

import java.util.ArrayList;

import org.jdom2.Document;
import org.jruby.embed.EmbedEvalUnit;

public class AppCache {
	EmbedEvalUnit evalUnit;
	ArrayList<EmbedEvalUnit> evalUnits;
	ExecutionBundle executionBundle;

	public ExecutionBundle getExecutionBundle() {
		return executionBundle;
	}

	public void setExecutionBundle(ExecutionBundle executionBundle) {
		this.executionBundle = executionBundle;
	}

	public ArrayList<EmbedEvalUnit> getEvalUnits() {
		return evalUnits;
	}

	public void setEvalUnits(ArrayList<EmbedEvalUnit> evalUnits) {
		this.evalUnits = evalUnits;
	}

	Document mainActivityDocument;

	public EmbedEvalUnit getEvalUnit() {
		return evalUnit;
	}

	public void setEvalUnit(EmbedEvalUnit evalUnit) {
		this.evalUnit = evalUnit;
	}

	public Document getMainActivityDocument() {
		return mainActivityDocument;
	}

	public void setMainActivityDocument(Document mainActivityDocument) {
		this.mainActivityDocument = mainActivityDocument;
	}
	
}
