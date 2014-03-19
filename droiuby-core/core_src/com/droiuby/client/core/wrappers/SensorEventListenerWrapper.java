package com.droiuby.client.core.wrappers;

import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.droiuby.client.core.ExecutionBundle;

public class SensorEventListenerWrapper implements SensorEventListener {

	RubyProc accuracyblock;
	RubyProc sensorblock;
	ExecutionBundle bundle;
	ScriptingContainer container;

	public SensorEventListenerWrapper(ExecutionBundle bundle) {
		this.bundle = bundle;
		this.container = bundle.getContainer();
	}

	public RubyProc getAccuracyblock() {
		return accuracyblock;
	}

	public void setAccuracyblock(RubyProc accuracyblock) {
		this.accuracyblock = accuracyblock;
	}

	public RubyProc getSensorblock() {
		return sensorblock;
	}

	public void setSensorblock(RubyProc sensorblock) {
		this.sensorblock = sensorblock;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		try {
			IRubyObject wrapped_sensor = JavaUtil.convertJavaToRuby(container
					.getProvider().getRuntime(), sensor);
			IRubyObject wrapped_accuracy = JavaUtil.convertJavaToRuby(container
					.getProvider().getRuntime(), accuracy);
			IRubyObject args[] = new IRubyObject[] { wrapped_sensor, wrapped_accuracy };
			accuracyblock.call19(container.getProvider()
					.getRuntime().getCurrentContext(), args, null);
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		
	}

	public void onSensorChanged(SensorEvent event) {
		try {
			IRubyObject wrapped_sensor = JavaUtil.convertJavaToRuby(container
					.getProvider().getRuntime(), event);
			IRubyObject args[] = new IRubyObject[] { wrapped_sensor};
			sensorblock.call19(container.getProvider()
					.getRuntime().getCurrentContext(), args, null);
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		
		
	}
}
