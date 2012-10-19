#trigger pre load
$droiuby_plugins = []
puts "loading plugins"
Droiuby::Plugins.descendants.each { |klass|
  puts "plugin -> #{klass.to_s}"
  $droiuby_plugins << klass.new
}

puts "starting plugins"

def start_droiuby_plugins
  $droiuby_plugins.each do |plugin|
    plugin.after_bootstrap
  end
end
