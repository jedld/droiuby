require "net/http"
require "uri"
require 'cgi'

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