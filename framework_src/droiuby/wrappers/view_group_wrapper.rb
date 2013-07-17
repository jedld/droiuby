require 'droiuby/wrappers/view_wrapper'

class ViewGroupWrapper < ViewWrapper
  def inner=(markup)
    _activity_builder.parsePartialReplaceChildren(@view,markup)
    after_partial_setup(self)
  end

  #TODO: support reverse markup generation
  def inner
    puts "TODO"
  end

  def append(markup_or_view)
    if markup_or_view.kind_of? String
      _activity_builder.parsePartialAppendChildren(@view,markup_or_view)
    elsif markup_or_view.kind_of? ViewWrapper
      _activity_builder.appendChild(@view,  markup_or_view.native)
    elsif markup_or_view.kind_of? Java::android.view.View.new
      _activity_builder.appendChild(@view,  markup_or_view)
    end
    after_partial_setup(self)
  end

  def to_front!(child = nil)
    if child.nil?
      super
    else
      @view.bringChildToFront(self.to_native(child))
    end
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
      if tag.kind_of? Java::com.droiuby.client.core.ViewExtras
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