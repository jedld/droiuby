require 'droiuby/wrappers/view_wrapper'

class TextViewWrapper < ViewWrapper
  
  def text=(text)
      @view.setText(text)
  end
  
  def text
      @view.getText
  end
end