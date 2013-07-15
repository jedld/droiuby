require 'bundler'
Bundler.require
require "net/http"
require "uri"
require 'cgi'

require 'active_support/core_ext/object'
require 'active_support/core_ext/string'


task :default => [:launch]

def launch_app(device_ip, url)
  url_str = "http://#{device_ip}:4000/control?cmd=launch&url=#{CGI::escape(url)}"
  puts "loading application at url #{url}"
  puts url_str
  uri = URI.parse(url_str)
  # Shortcut
  response = Net::HTTP.get_response(uri)
  # Will print response.body
  Net::HTTP.get_print(uri)
end

desc "Launches an app on a device with droiuby installed (droiuby must be running)"
task :launch, [:device_ip, :url] do |t, args|
  launch_app(args.device_ip, args.url)
end

desc "reloads the current app"
task :reload, [:device_ip] do |t, args|
  url_str = "http://#{args.device_ip}:4000/control?cmd=reload"
  puts "reloading application at url #{args.url}"
  puts url_str
  uri = URI.parse(url_str)
  # Shortcut
  response = Net::HTTP.get_response(uri)
  # Will print response.body
  Net::HTTP.get_print(uri)
end

desc "create a new droiuby app"

task :new, [:name] do |t, args|
  puts `thor project:create #{args.name}`
end

desc "Allow live editing of apps by starting a web server and pointing droiuby to the local instance"
task :live, [:name, :device_ip, :host_name, :source_dir] do |t, args|
  require 'webrick'
  include WEBrick
  source_dir_args = args.source_dir ? args.source_dir : 'projects'
  host_name_args = args.host_name ? args.host_name : Socket.gethostname
  src_dir = File.join(source_dir_args, args.name)
  port = 2000

  ready = false

  Thread.new do
    while !ready
    end
    `adb shell am start -W -S --activity-clear-top --activity-brought-to-front -n com.droiuby.client/.CanvasActivity`
    launch_app(args.device_ip, "http://#{host_name_args}:#{port}/config.xml")
  end

  puts "Starting server: http://#{host_name_args}:#{port}"
  server = HTTPServer.new(:Port=>2000,:DocumentRoot=> src_dir,:StartCallback => Proc.new {
    ready = true
  })
  trap("INT"){ server.shutdown }
  server.start
end

desc "package and execute target app"
task :execute, [:name, :device_ip] do |t, args|
  puts `thor project:execute #{args.name} #{args.device_ip}`
end

require 'java'

desc "generate object ruby-java wrapper"
task :wrap, [:class_or_interface, :wrap_method] do |t, args|
  
  #read local.properties and project.properties
  local_prop = Utils::Properties.load_from_file('local.properties')
  project_prop = Utils::Properties.load_from_file('project.properties')
  sdk_directory = local_prop.get(:'sdk.dir')
  target = project_prop.get(:'target')
  
  #get android.jar location
  
  android_class_path = File.join(sdk_directory,'platforms',target,'android.jar')
  jruby_class_path = File.join(File.dirname(__FILE__),'libs_large')
  puts "adding #{android_class_path} to class path"
  $CLASSPATH << android_class_path
  $CLASSPATH << File.join(File.dirname(__FILE__),"bin","classes")
  Dir.foreach(jruby_class_path) do |x|
      path = File.join(jruby_class_path, x)
      if x == "." or x == ".."
          next
      elsif !File.directory?(path)
        puts "adding #{path}"
        $CLASSPATH << path
      end
  end
 
  
  #normalize
  klass_str = args.class_or_interface
  portions = klass_str.split('.')
  klassname = portions.pop
  full_name_space = ['Java']
  if portions.size > 0
    full_name_space << portions.join('.')
  end
  full_name_space << klassname
  class_name = full_name_space.join('::')
  
  puts "Generating for class #{class_name}"
  klass_or_interface = eval(class_name)
  puts klass_or_interface.class.to_s
  
  class_name = args.class_or_interface.split('.').last;
  
  
  java_gen_src = File.join(File.dirname(__FILE__),'gen_src')
  unless Dir.exists?(java_gen_src)
    Dir.mkdir(java_gen_src)
  end
  
  full_class_name = ['com','droiuby','wrappers',"#{klassname}RubyWrapper"].join('.')
    
  Java::com.droiuby.client.core::SourceBuilder.build(full_class_name, klass_or_interface.java_class, java_gen_src)
  
end
