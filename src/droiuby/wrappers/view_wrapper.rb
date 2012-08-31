class ViewWrapper
  
  class Animator
    def initialize(target)
      @animators = []
      @target = target
    end
    
    def method_missing(name, *args, &block)
        anim = Java::android.animation.ObjectAnimator.ofFloat(@target.native, name.to_s, args[1], args[2]);
        anim.setDuration(args[0]);
        @animators << anim
    end
    
    def start
      @animators.each { |a|
        a.start
      }
    end
     
  end
  
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
  
  def animate(&block)
    animator = Animator.new(self)
    block.call(animator)
    animator.start
  end
  
  def to_native(target)
    if child.kind_of? ViewWrapper
      target.native
    elsif target.kind_of? Java::android.view.View.new
      target
    end
  end
end