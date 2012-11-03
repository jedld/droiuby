package com.droiuby.ide;

import java.text.DateFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Color;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.droiuby.client.core.DroiubyActivity;
import com.droiuby.client.core.ErrorLog;
import com.droiuby.client.core.ExecutionBundle;

public class UpdateLogTask extends TimerTask {

	DroiubyActivity context;

	int resId;

	public UpdateLogTask(DroiubyActivity context, int resId) {
		this.context = context;
		this.resId = resId;
	}

	@Override
	public void run() {
		context.runOnUiThread(new Runnable() {
			public void run() {
				Timer timer = new Timer();
				LinearLayout errorListLayout = (LinearLayout) context
						.findViewById(resId);
				final ScrollView scrollview = (ScrollView) errorListLayout
						.getParent();

				ExecutionBundle executionBundle = context.getExecutionBundle();
				if (executionBundle != null
						&& executionBundle.getScriptErrors().size() > 0) {
					for (ErrorLog error : executionBundle.getScriptErrors()) {
						EditText errorText = (EditText) context
								.getLayoutInflater().inflate(R.layout.edittext,
										null);
						errorText.setText(android.text.format.DateFormat
								.format("MM-dd hh:mm:ss", error.getTimestamp())
								+ ": " + error.getMessage());
						if (error.getLogLevel() == ErrorLog.ERROR) {
							errorText.setTextColor(Color.parseColor("#FF3300"));
						} else if (error.getLogLevel() == ErrorLog.WARN) {
							errorText.setTextColor(Color.parseColor("#FFCC00"));
						} else {
							errorText.setTextColor(Color.parseColor("#000000"));
						}
						errorListLayout.addView(errorText,
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
					}
					executionBundle.clearErrors();
					scrollview.post(new Runnable() {
						public void run() {
							scrollview.scrollTo(0, scrollview.getBottom());
						}
					});
				}
				UpdateLogTask task = new UpdateLogTask(context, resId);
				timer.schedule(task, 2000);
			}
		});

	}

}
