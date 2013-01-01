require "net/http"
require "uri"
require 'cgi'

task :default => [:launch]

desc "Launches an app on a device with droiuby installed (droiuby must be running)"
task :launch, [:device_ip, :url] do |t, args|
  url_str = "http://#{args.device_ip}:4000/control?cmd=launch&url=#{CGI::escape(args.url)}"
  puts "loading application at url #{args.url}"
  puts url_str
  uri = URI.parse(url_str)
  # Shortcut
  response = Net::HTTP.get_response(uri)
  # Will print response.body
  Net::HTTP.get_print(uri)
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