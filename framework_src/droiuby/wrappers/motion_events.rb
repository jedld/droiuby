class MotionEventsWrapper
  
  ACTION_DOWN = Java::android.view.MotionEvent::ACTION_DOWN
  ACTION_MOVE = Java::android.view.MotionEvent::ACTION_MOVE
  
  include Droiuby::ViewHelper
  include JavaMethodHelper
  
  java_fast_reader Java::android.view.MotionEvent, :x, :y, :action, :pointer_count, :history_size
  java_native_method Java::android.view.MotionEvent, :getHistoricalEventTime, [Java::int]
  java_native_method Java::android.view.MotionEvent, :getPointerId, [Java::int]
  java_native_method Java::android.view.MotionEvent, :getHistoricalX, [Java::int, Java::int]
  java_native_method Java::android.view.MotionEvent, :getHistoricalY, [Java::int, Java::int]
  
  
  def initialize(event)
    @native = event
  end
  
  def native
    @native
  end
  
  def each(&block)
    pointerCount = pointer_count
    (0...history_size).each do |h|
      current_time = java_getHistoricalEventTime(h)
      (0...pointerCount).each do |p|
        block.call(java_getPointerId(p), java_getHistoricalX(p, h), java_getHistoricalY(p, h))
      end
    end
  end
  
end