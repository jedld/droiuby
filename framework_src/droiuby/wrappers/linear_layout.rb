require 'droiuby/wrappers/view_wrapper'

class LinearLayoutWrapper < ViewGroupWrapper
  
  java_attr_accessor :baseline_algined, :orientation
  
  def initialize(view = nil)
    if view.nil?
      @view = Java::android.widget.LinearLayout.new(_current_activity)
    else
      super(view)
    end
  end
  
end