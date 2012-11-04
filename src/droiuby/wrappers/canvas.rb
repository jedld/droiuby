class Paint
  def initialize
    @native = Java::android.graphics.Paint.new
  end
  
  def native
    @native
  end
  
  def alpha=(value)
      @native.setAlpha(Java::android.graphics.Color.parseColor(value))
  end
    
  def alpha
    @native.getAlpha
  end
    
  def color=(value)
    @native.setColor(Java::android.graphics.Color.parseColor(value))
  end
  
  def color
    @native.getColor
  end
  
  def stroke_width=(width)
    @native.setStrokeWidth(width)
  end
  
  def stroke_width
    @native.getStrokeWidth
  end
end

class Canvas
  def initialize(native)
    @native = native
  end
  
  def native
    @native
  end
  
  def paint
    Paint.new
  end
  
  def circle(x, y, size, paint)
    @native.drawCircle(x, y, size, paint.native);
  end
  
  def line(x, y, x1, y1, paint)
    @native.drawLine(x, y, x1, y1, paint.native);
  end
  
end