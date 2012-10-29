#Wrap the android animation API
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

    def native
      @animator_set
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

    def on(event, &block)
      if [:cancel, :start, :end, :repeat ].include?(event)
        auto_wrap_block = Proc.new { |v| block.call(wrap_native_view(v))}
        self.native.addListener(Java::com.droiuby.client.core.listeners.AnimationListenerWrapper.new(_execution_bundle, event.to_s, auto_wrap_block))
      end
      self
    end
    
    protected

    def to_animator(animation)
      animator = animation
      if animation.kind_of? Animator
        animator = animation.animator_set
      end
      animator
    end
  end