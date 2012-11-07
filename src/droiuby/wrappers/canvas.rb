
class Rect
  def initialize(rect = nil, left = 0, top = 0, right = 0, bottom = 0)
    if (rect.nil?)
      @native = Java::android.graphics.Rect.new(left, top, right, bottom)
    else
      @native = rect
    end
    
  end
  
  def native
    @native
  end
  
  def height
    @native.height
  end
  
  def width
    @native.width
  end
  
  def set(left, top, right, bottom)
    @native.set(left, top, right, bottom)
  end
end

def _rect(left = 0, top = 0, right = 0, bottom = 0)
  Rect.new(nil, left, top, right, bottom)
end

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
    if (value.kind_of String)
      value = value.to_color
    end
    @native.setColor(value)
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
    @paint = make_paint
  end
  
  def native
    @native
  end
  
  def make_paint
    Paint.new
  end
  
  def paint=(p)
    @paint = p
  end
  
  def draw_color(value)
    if (value.kind_of? String)
      value = value.to_color
    end
    @native.drawColor(value)
  end
  
  def circle(x, y, size, paint = nil)
    paint = @paint if paint.nil?
    @native.drawCircle(x, y, size, paint.native);
  end
  
  def line(x, y, x1, y1, paint = nil)
    paint = @paint if paint.nil?
    @native.drawLine(x, y, x1, y1, paint.native);
  end
  
  def bitmap(bitmap, x, y, paint = nil, options = {})
    paint = @paint if paint.nil?
    if bitmap.kind_of? BitmapDrawableWrapper
      bitmap = bitmap.to_bitmap
    end
    @native.drawBitmap(bitmap, x.to_f, y.to_f, paint.native)
  end
  
end