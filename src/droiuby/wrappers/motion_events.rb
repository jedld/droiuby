class MotionEventsWrapper
  
  def initialize(event)
    @native = event
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