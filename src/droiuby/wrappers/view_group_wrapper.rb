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

  def to_front!(child)
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

  def form_fields
    fields = {}
    collect_fields(self, fields)
    fields
  end

  private

  def set_hash_from_tag(child, field_hash, &block)
    unless child.native.getTag.nil?
      tag = child.native.getTag
      if tag.kind_of? Java::com.dayosoft.activeapp.core.ViewExtras
        unless tag.getView_name.nil?
          field_hash[tag.getView_name.to_sym] = block.call(child)
        end
      end
    end
  end

  def collect_fields(view, field_hash)
    view.children.each do |child|
      if child.kind_of? EditTextWrapper
        set_hash_from_tag(child, field_hash) do |c|
          c.text
        end
      elsif child.kind_of? CompoundButtonWrapper
        set_hash_from_tag(child, field_hash) do |c|
          c.checked? ? 'true' : 'false'
        end
      elsif child.kind_of? ViewGroupWrapper
        collect_fields(child, field_hash)
      end
    end
  end

end