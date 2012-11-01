require 'droiuby/wrappers/view_wrapper'

class TextViewWrapper < ViewWrapper
  
  java_attr_accessor :cursor_visible
  
  def text=(text)
      @view.setText(text)
  end
  
  def text
      @view.getText
  end
  
  
end