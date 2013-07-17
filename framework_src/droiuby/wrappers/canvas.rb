
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
  
  include JavaMethodHelper
  include Droiuby::ViewHelper
  
  java_fast_reader Java::android.graphics.Canvas, :width
  java_fast_reader Java::android.graphics.Canvas, :height
  
  java_native_method Java::android.graphics.Canvas, :drawColor, [Java::int]
  java_native_method Java::android.graphics.Canvas, :drawText, [Java::java.lang.String, Java::float, Java::float, Java::android.graphics.Paint]
  java_native_method Java::android.graphics.Canvas, :drawBitmap, [Java::android.graphics.Bitmap, Java::float, Java::float, Java::android.graphics.Paint]
  java_native_method Java::android.graphics.Canvas, :drawCircle, [Java::float, Java::float, Java::float, Java::android.graphics.Paint]
  java_native_method Java::android.graphics.Canvas, :drawLine, [Java::float, Java::float, Java::float, Java::float, Java::android.graphics.Paint]
  java_native_method Java::android.graphics.Canvas, :drawRoundRect, [Java::android.graphics.RectF, Java::float, Java::float, Java::android.graphics.Paint]
  
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
  
  def make_rect(left, top, right, bottom)
    Java::android.graphics.RectF.new(left, top, right, bottom)
  end
  
  def paint
    @paint
  end
  
  def paint=(p)
    @paint = p
  end
  
  def draw_color(value)
    if (value.kind_of? String)
      value = value.to_color
    end
    
    java_drawColor(value)
  end
  
  def text(msg, x, y, paint = nil)
    paint = @paint if paint.nil?
    
    java_drawText(msg, x, y, paint.native);
  end
  
  def circle(x, y, size, paint = nil)
    paint = @paint if paint.nil?
    java_drawCircle(x, y, size, paint.native);
  end
  
  def line(x, y, x1, y1, paint = nil)
    paint = @paint if paint.nil?
    java_drawLine(x, y, x1, y1, paint.native);
  end
  
  def bitmap(bitmap, x, y, paint = nil, options = {})
    paint = @paint if paint.nil?
    if bitmap.class == BitmapDrawableWrapper
      bitmap = bitmap.to_bitmap
    end
#    native.drawBitmap(bitmap, x.to_f, y.to_f, paint.native)
    java_drawBitmap(bitmap, x.to_f, y.to_f, paint.native)
  end
  
  def round_rect(rect, x, y, paint = nil)
    paint = @paint if paint.nil?
    java_drawRoundRect(rect, x, y, paint.native) 
  end  
end