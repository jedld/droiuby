require 'droiuby/wrappers/animation'

class ViewWrapper
  def initialize(view = nil)
    unless view.nil?
      @view = view
    else
      @view = Java::android.view.View.new
    end
  end

  def id
    @view.getId
  end

  def x
    @view.getLeft
  end

  def y
    @view.getTop
  end

  def width
    @view.getWidth
  end

  def height
    @view.getHeight
  end

  def measured_width
    @view.getMeasuredWidth
  end

  def measured_height
    @view.getMeasuredHeight
  end

  def native
    @view
  end

  def background
    @view.getBackground
  end

  def background_color=(value)
    @view.setBackgroundColor(Java::android.graphics.Color.parseColor(value));
  end

  def gone=(flag)
    @view.setVisibility(Java::android.view.View::GONE) if flag
  end

  def gone?
    @view.getVisibility == Java::android.view.View::GONE
  end

  def visible=(flag)
    @view.setVisibility(flag ? Java::android.view.View::VISIBLE : Java::android.view.View::INVISIBLE)
  end

  def visible?
    @view.getVisibility == Java::android.view.View::VISIBLE
  end

  def alpha
    @view.getAlpha
  end

  def alpha=(a)
    @view.setAlpha(a)
  end

  def enabled=(flag)
    @view.setEnabled(flag)
  end

  def enabled?
    @view.isEnabled
  end

  def rotation
    @view.getRotation
  end

  def rotation=(rotation)
    @view.setRotation(rotation)
  end

  def pivot_x
    @view.getPivotX
  end

  def pivot_x=(pivot)
    @view.setPivotX(pivot)
  end

  def pivot_y
    @view.getPivotY
  end

  def pivot_y=(pivot)
    @view.setPivotY(pivot)
  end

  def translation_x=(translation)
    @view.setTranslationX(translation)
  end

  def translation_x
    @view.getTranslationX
  end

  def translation_y=(translation)
    @view.setTranslationY(translation)
  end

  def translation_y
    @view.getTranslationY
  end

  def scale_x
    @view.getScaleX
  end

  def scale_x=(scale)
    @view.setScaleX(scale)
  end

  def scale_y
    @view.getScaleY
  end

  def scale_y=(scale)
    @view.setScaleY(scale)
  end

  def camera_distance
    @view.getCameraDistance
  end

  def camera_distance=(value)
    @view.setCameraDistance(value)
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
    
    if tag
      id_attr = tag.getView_id
      name_attr = tag.getView_name
    end
    
    puts "#{spaces}#{self.class.name} id=\"#{id_attr}\" name=\"#{name_attr}\"\n"
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
    unless native.getTag.nil?
      tag = native.getTag
      if tag.kind_of? Java::com.droiuby.client.core.ViewExtras
        data_attributes = tag.getDataAttributes
        if data_attributes.containsKey(key)
          data_attributes.get(key)
        end
      end
    end
  end

  def on(event,&block)
    case(event.to_sym)
    when :click
      self.native.setOnClickListener(Java::com.droiuby.client.core.listeners.ViewOnClickListener.new($scripting_container, &block))
    when :long_click
      self.native.setOnLongClickListener(Java::com.droiuby.client.core.listeners.ViewOnLongClickListener.new($scripting_container, &block))
    when :focus_changed
      self.native.setOnFocusChangeListener(Java::com.droiuby.client.core.listeners.FocusChangeListenerWrapper.new($scripting_container, &block))
    end
  end
end