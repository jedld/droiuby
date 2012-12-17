class Accelerometer

  def initialize
    @native = _execution_bundle.getSensor(Java::Sensor.TYPE_ACCELEROMETER)
    @listener = Java::com.droiuby.client.core.wrappers.SensorEventListenerWrapper.new(_execution_bundle)
  end
  
  def on(event, &block)
    
    case event
      when :sensor_changed
        @listener.setSensorblock(block) 
        @native.registerListener(@listener, sensor, rate)
    else
    end
  end
end