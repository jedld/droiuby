require 'droiuby/wrappers/view_wrapper'

class ViewGroupWrapper < ViewWrapper
  def inner=(markup)
    $current_activity_builder.parsePartialReplaceChildren(@view,markup)
  end
end