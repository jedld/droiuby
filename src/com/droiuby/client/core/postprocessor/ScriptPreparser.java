package com.droiuby.client.core.postprocessor;

import java.io.StringReader;

import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;

import com.droiuby.client.core.AssetDownloadCompleteListener;
import com.droiuby.client.core.ExecutionBundle;

public class ScriptPreparser implements AssetDownloadCompleteListener {

	public Object onComplete(ExecutionBundle bundle, String name, Object result) {
		ScriptingContainer container = bundle.getContainer();
		EmbedEvalUnit parsed = container.parse(new StringReader((String)result), name, 0);
		return parsed;
	}

}
