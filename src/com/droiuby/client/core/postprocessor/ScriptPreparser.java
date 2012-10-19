package com.droiuby.client.core.postprocessor;

import java.io.StringReader;

import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ParseFailedException;
import org.jruby.embed.ScriptingContainer;

import com.droiuby.client.core.AssetDownloadCompleteListener;
import com.droiuby.client.core.ExecutionBundle;

public class ScriptPreparser implements AssetDownloadCompleteListener {

	public Object onComplete(ExecutionBundle bundle, String name, Object result) {
		ScriptingContainer container = bundle.getContainer();
		EmbedEvalUnit parsed = null;
		try {
			parsed = container.parse(new StringReader((String)result), name, 0);
		} catch (ParseFailedException e) {
			e.printStackTrace();
			bundle.addError(e.getMessage());
		}
		return parsed;
	}

}
