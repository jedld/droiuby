require 'droiuby/wrappers/canvas'

class SurfaceHolderWrapper
  
  include JavaMethodHelper
  
  java_native_method Java::com.droiuby.client.core.wrappers.SurfaceViewHolderWrapper, :lockCanvas, [], "java_lockCanvasVoid"
  java_native_method Java::com.droiuby.client.core.wrappers.SurfaceViewHolderWrapper, :lockCanvas, [Java::android.graphics.Rect], "java_lockCanvas"
  java_native_method Java::com.droiuby.client.core.wrappers.SurfaceViewHolderWrapper, :unlockCanvasAndPost, [Java::android.graphics.Canvas]
    
  def initialize(surface)
    @native = surface
  end
  
  def native
    @native
  end
  
  def lock(rect = nil, &block)
    if rect.nil?
#      canvas = Canvas.new(native.lockCanvas)
      canvas = Canvas.new(java_lockCanvasVoid)
    else
#      canvas = Canvas.new(native.lockCanvas(rect))
      canvas = Canvas.new(java_lockCanvas(rect.native))
    end
    block.call(canvas)
#    native.unlockCanvasAndPost(canvas.native)
    java_unlockCanvasAndPost(canvas.native)
  end
  
end

class SurfaceViewWrapper < ViewWrapper
  
  def initialize(view = nil)
    unless view.nil?
      @view = view
    else
      @view = Java::com.droiuby.client.core.wrappers.SurfaceViewWrapper.new(_current_activity, _execution_bundle)
    end
    @builder = Java::com.droiuby.client.core.builder.ViewBuilder.new
    @builder.setContext(_current_activity)
  end
  
  def on(event,&block)
    super(event,&block)
    case(event.to_sym)
        when :surface_created
          auto_wrap_block = Proc.new { |surface| block.call(SurfaceHolderWrapper.new(surface))}
          self.native.setSurfaceCreatedBlock(auto_wrap_block)
        when :surface_destroyed
          auto_wrap_block = Proc.new { |surface| block.call(SurfaceHolderWrapper.new(surface))}
          self.native.setSurfaceDestroyedBlock(auto_wrap_block)
      end
  end
    
end