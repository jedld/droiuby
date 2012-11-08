class MotionEventsWrapper
  
  ACTION_DOWN = Java::android.view.MotionEvent::ACTION_DOWN
  ACTION_MOVE = Java::android.view.MotionEvent::ACTION_MOVE
  
  include Droiuby::ViewHelper
  
  java_fast_reader Java::android.view.MotionEvent, :x, :y
  
  def initialize(event)
    @native = event
  end
  
  def native
    @native
  end
  
  def action
    @native.getAction
  end

   
  def each(&block)
    pointerCount = @native.getPointerCount();
    (0...@native.getHistorySize()).each do |h|
      current_time = @native.getHistoricalEventTime(h)
      (0...pointerCount).each do |p|
        block.call(@native.getPointerId(p), @native.getHistoricalX(p, h), @native.getHistoricalY(p, h))
      end
    end
  end
  
end