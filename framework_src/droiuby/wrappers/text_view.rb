require 'droiuby/wrappers/view_wrapper'

class TextViewWrapper < ViewWrapper
  
  java_attr_accessor :cursor_visible, :text_color, :text_scale_x, :text_size
  
  def text=(text)
      @view.setText(text)
  end
  
  def text
      @view.getText
  end
  
  def text_color
    @view.getCurrentTextColor
  end
  
  def text_color=(value)
    @view.setTextColor(parse_color(value));
  end 
  
  
end