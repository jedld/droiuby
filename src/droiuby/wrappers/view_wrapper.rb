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
  
  def initialize(view)
    @view = view
  end

  def id
    @view.getId
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
  
  def animate(&block)
    animator = Animator.new(self)
    block.call(animator)
    animator.start
  end
  
end