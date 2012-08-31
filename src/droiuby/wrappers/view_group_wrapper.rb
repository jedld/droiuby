require 'droiuby/wrappers/view_wrapper'

class ViewGroupWrapper < ViewWrapper
  def inner=(markup)
    $current_activity_builder.parsePartialReplaceChildren(@view,markup)
  end

  #TODO: support reverse markup generation
  def inner
    puts "TODO"
  end

  def append(markup_or_view)
    if markup_or_view.kind_of? String
      $current_activity_builder.parsePartialAppendChildren(@view,markup_or_view)
    elsif markup_or_view.kind_of? ViewWrapper
      $current_activity_builder.appendChild(@view,  markup_or_view.native)
    elsif markup_or_view.kind_of? Java::android.view.View.new
      $current_activity_builder.appendChild(@view,  markup_or_view)
    end
  end

  def bring_child_to_front(child)
    @view.bringChildToFront(self.to_native(target_view))
  end

  def count
    @view.getChildCount
  end
  
  def children
    (0...self.count).collect { |i|
      wrap_native_view(self.child(i))
    }
  end

  def child(index)
    @view.getChildAt(index)
  end
end