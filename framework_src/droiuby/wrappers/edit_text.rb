require 'droiuby/wrappers/view_wrapper'

class EditTextWrapper < ViewWrapper
  
  def text=(text)
      @view.setText(text)
  end
  
  def text
      @view.getText.toString
  end
  
  def color=(value)
    @view.setTextColor(value)
  end
  
  def color
    @view.getTextColors.getDefaultColor
  end
end