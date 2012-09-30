require 'droiuby/wrappers/animation'
require 'droiuby/wrappers/java_helpers/view_helper'

class ViewWrapper
  
  include Droiuby::ViewHelper
  
  java_attr_accessor :right, :left, :top, :bottom, :x, :y, :alpha, :rotation, :pivot_x, :pivot_y, :translation_x, 
    :translation_y, :scroll_x, :scroll_y, :scale_x, :scale_y, :rotation_x, :rotation_y, :camera_distance,
    :padding_left, :padding_top, :padding_right, :padding_bottom
    
  java_attr_reader :id, :width, :height, :measured_with, :measured_height, :background
  
  def initialize(view = nil)
    unless view.nil?
      @view = view
    else
      @view = Java::android.view.View.new
    end
  end

  def native
    @view
  end

  def background_color=(value)
    @view.setBackgroundColor(Java::android.graphics.Color.parseColor(value));
  end

  def gone=(flag)
    @view.setVisibility(Java::android.view.View::GONE) if flag
  end

  def gone?
    hidden?
  end

  def hidden?
    @view.getVisibility == Java::android.view.View::GONE
  end
  
  def show!
    @view.setVisibility(Java::android.view.View::VISIBLE)
  end
  
  def hide!
    @view.setVisibility(Java::android.view.View::GONE)
  end
  
  def visible=(flag)
    @view.setVisibility(flag ? Java::android.view.View::VISIBLE : Java::android.view.View::INVISIBLE)
  end

  def visible?
    @view.getVisibility == Java::android.view.View::VISIBLE
  end

  def enabled=(flag)
    @view.setEnabled(flag)
  end

  def enabled?
    @view.isEnabled
  end

  def parent
    wrap_native_view(@view.getParent)
  end
  
  def blink
    orig_alpha = self.alpha
    view = self
    self.animate { |a|
      a.alpha 0, 1
    }.on(:end) { |v|
      view.alpha = orig_alpha
    }.start
  end
  
  def animate(&block)
    animator = Animator.new(self)
    block.call(animator)
    animator.done
    animator
  end

  def to_native(target)
    if child.kind_of? ViewWrapper
      target.native
    elsif target.kind_of? Java::android.view.View.new
      target
    end
  end

  def click
    self.native.performClick
  end
  
  def p_tree(level = 0)
    spaces = ''
    level.times { |i| spaces << '  '}
      
    id_attr = self.id
    name_attr = ""
    class_attr = "" 
    
    if tag
      id_attr = tag.getView_id
      name_attr = tag.getView_name
      class_attr = tag.getView_class
    else
      rid = reverse_resolve(id_attr)
      id_attr = "@#{rid}" unless rid.nil?
    end
    
    data_attribute_list = []
    if _extras
      attributes = _extras.getDataAttributes
      
      attributes.keySet.each do |key|
        data_attribute_list << "data-#{key}=\"#{self.data(key)}\""
      end
    end
    
    
    puts "#{spaces}#{self.class.name} id=\"#{id_attr}\" name=\"#{name_attr}\" class=\"#{class_attr}\" #{data_attribute_list.join(' ')}\n"
    self.children.each { |c|
      c.p_tree(level + 1)
    } if self.respond_to? :children
  end

  def tag
    unless native.getTag.nil?
      tag = native.getTag
      if tag.kind_of? Java::com.droiuby.client.core.ViewExtras
        return tag
      end
    end
    nil
  end
  
  def data(key)
    if _extras
      data_attributes = _extras.getDataAttributes
      if data_attributes.containsKey(key)
        data_attributes.get(key)
      end
    end
  end

  def on(event,&block)
    case(event.to_sym)
    when :click
      self.native.setOnClickListener(Java::com.droiuby.client.core.listeners.ViewOnClickListener.new(_scripting_container, &block))
    when :long_click
      self.native.setOnLongClickListener(Java::com.droiuby.client.core.listeners.ViewOnLongClickListener.new(_scripting_container, &block))
    when :focus_changed
      self.native.setOnFocusChangeListener(Java::com.droiuby.client.core.listeners.FocusChangeListenerWrapper.new(_scripting_container, &block))
    end
  end
  
  protected
  
  def _extras
    unless native.getTag.nil?
      tag = native.getTag
      if tag.kind_of? Java::com.droiuby.client.core.ViewExtras
        tag
      end
     end
  end
end