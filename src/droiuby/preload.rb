#trigger pre load
def start_droiuby_plugins
  puts "starting plugins"
  $droiuby_plugins = []
  puts "loading plugins"
  Droiuby::Plugins.descendants.each { |klass|
    puts "plugin -> #{klass.to_s}"
    $droiuby_plugins << klass.new
  }

  $droiuby_plugins.each do |plugin|
    puts "plugin attach #{plugin.class.to_s}"
    plugin.after_bootstrap
  end
end

def after_view_setup
  $droiuby_plugins.each do |plugin|
    puts "plugin attach #{plugin.class.to_s}"
    plugin.after_view_setup
  end  
end

def after_partial_setup(view_group)
  $droiuby_plugins.each do |plugin|
    puts "plugin attach #{plugin.class.to_s}"
    plugin.after_partial_setup(view_group)
  end
end
