require 'droiuby/loader'

#Droiuby Framework class  
class DroiubyFramework
  
  def before_activity_setup
    fname = "#{File.dirname(__FILE__)}/bootstrap.rb"
    @bootstrap = @bootstrap || File.read(fname) 
    eval(@bootstrap, TOPLEVEL_BINDING, fname, __LINE__)
  end
  
  def preload
    fname = "#{File.dirname(__FILE__)}/preload.rb"
    @preload = @preload || File.read(fname) 
    eval(@preload, TOPLEVEL_BINDING, fname, __LINE__)
  end
  
  def on_click(view)
  end
  
  def script(controller)
    klass = controller.camelize.constantize
    instance = klass.new
    instance.on_create
    instance
  end
  
end

$framework = DroiubyFramework.new