require 'droiuby/wrappers/view_wrapper'

class CompoundButtonWrapper < ViewWrapper
  
  
  def checked?
    self.native.isChecked
  end
  
  def checked=(value)
    self.native.setChecked(value)
  end
  
  
end