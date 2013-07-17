class Accelerometer
  
  attr_accessor :sensor, :rate
  include Droiuby::Wrappers::Listeners
  
  def initialize
    @native = _execution_bundle.getSensor(Java::Sensor.TYPE_ACCELEROMETER)
  end
  
  def on(event, &block)
    unless @listener
      @listener = Droiuby::Wrappers::Listeners::AutoWrapMultiple.new(_execution_bundle)
      @native.registerListener(@listener.to_native('SensorEventListener'), @sensor, @rate)
    end
    @listener.impl(event, &block) 
  end
end