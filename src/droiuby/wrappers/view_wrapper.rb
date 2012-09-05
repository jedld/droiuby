class ViewWrapper
  class Animator
    def initialize(target)
      @animator_set = Java::android.animation.AnimatorSet.new
      @mode = :together
      @animators = []
      @target = target
      @done = false
    end

    def animator_set
      @animator_set
    end

    def animators
      @animators
    end

    def method_missing(name, *args, &block)
      anim = Java::android.animation.ObjectAnimator.ofFloat(@target.native, name.to_s.camelize(:lower), args[0], args[1]);
      if args[2] && args[2].kind_of?(Hash)
        duration = args[2][:duration]
        anim.setDuration(duration)
      end
      @animators << anim
      anim
    end

    def sequentially
      @mode = :one_after_the_other
      self
    end

    def together
      @mode = :together
      self
    end

    def before(animation)
      Java::android.animation.AnimatorSet.new.tap { |s|
        s.play(to_animator(animation)).before(self.animator_set)
      } if @done
    end

    def after(animation)
      s = Java::android.animation.AnimatorSet.new.tap { |s|
        s.play(to_animator(animation)).after(self.animator_set)
      } if @done
    end

    def wait(milliseconds)
      Java::android.animation.AnimatorSet.new.tap { |s|
        s.play(self.animator_set).after(milliseconds.to_i)
      } if @done
    end

    def with(animation)
      Java::android.animation.AnimatorSet.new.tap { |s|
        s.play(to_animator(animation)).with(self.animator_set)
      } if @done
    end

    def together
      @mode = :together
    end
    
    def one_after_the_other
      @mode = :one_after_the_other
    end
    
    def done
      @done = true
      case @mode
      when :together
        @animator_set.playTogether(*@animators)
      when :one_after_the_other
        @animator_set.playSequentially(*@animators)
      end
    end

    def start
      @animator_set.start
    end

    protected

    def to_animator(animation)
      animator = animation
      if animation.kind_of? ViewWrapper::Animator
        animator = animation.animator_set
      end
      animator
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

  def on(event,&block)
    case(event.to_sym)
    when :click
      self.native.setOnClickListener(Java::com.dayosoft.activeapp.core.listeners.ViewOnClickListener.new($scripting_container, &block))
    when :long_click
      self.native.setOnLongClickListener(Java::com.dayosoft.activeapp.core.listeners.ViewOnLongClickListener.new($scripting_container, &block))
    end
  end
end