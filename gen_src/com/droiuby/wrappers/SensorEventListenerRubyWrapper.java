
package com.droiuby.wrappers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import com.droiuby.client.core.ExecutionBundle;
import org.jruby.Ruby;
import org.jruby.RubyInteger;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

public class SensorEventListenerRubyWrapper
    implements SensorEventListener
{

    protected RubyObject backingObject;
    protected ExecutionBundle executionBundle;
    protected ScriptingContainer container;

    public SensorEventListenerRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject) {
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
    }

    public void onSensorChanged(SensorEvent param1) {
        try {
            Ruby runtime = container.getProvider().getRuntime();
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            backingObject.callMethod(runtime.getCurrentContext(), "onSensorChanged", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

    public void onAccuracyChanged(Sensor param1, int param2) {
        try {
            Ruby runtime = container.getProvider().getRuntime();
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject wrapped_param2 = RubyInteger.int2fix(runtime, param2);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1, wrapped_param2 };
            backingObject.callMethod(runtime.getCurrentContext(), "onAccuracyChanged", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

}
