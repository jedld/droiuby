require 'rubygems'

require 'net/http'
require 'net/http/post/multipart'
require 'zip'
require 'thor'

class Project < Thor

  include Thor::Actions

  source_root 'templates'

  desc "create NAME [WORKSPACE_DIR]","create a new droiuby project with NAME"
  def create(name, output_dir = 'projects')
    @name = name
    @description = name
    @launcher_icon = ''
    @base_url = ''
    @main_xml = 'index.xml'

    dest_folder = File.join(output_dir,"#{name}")
    template File.join('ruby','config.droiuby.erb'), File.join(dest_folder,"config.droiuby")
    template File.join('ruby','gitignore.erb'), File.join(dest_folder,".gitignore")
    template File.join('ruby','index.xml.erb'), File.join(dest_folder,"index.xml")
    template File.join('ruby','application.css.erb'), File.join(dest_folder,"application.css")
    template File.join('ruby','index.rb.erb'), File.join(dest_folder,"index.rb")
    empty_directory File.join(dest_folder,"lib")
  end

  desc "package NAME [WORKSPACE_DIR] [true|false]","package a project"

  def package(name, source_dir = 'projects', force = "false")
    src_folder = File.join(source_dir,"#{name}")
    say "compress #{src_folder}"
    compress(src_folder, force)
  end

  
  desc "upload NAME DEVICE_IP [WORKSPACE_DIR]","uploads a droiuby application to target device running droiuby client"
  def upload(name, device_ip, source_dir = 'projects')
    src_package = File.join(source_dir,name,'build',"#{name}.zip")

    url_str = "http://#{device_ip}:4000/upload"
    uri = URI.parse(url_str)
    say "uploading to #{url_str} -> #{uri.host}:#{uri.port}"
    File.open(src_package) do |zip|
      req = Net::HTTP::Post::Multipart.new uri.path,
      "name" => name,
      "run" => "true",
      "file" => UploadIO.new(zip, "application/zip", src_package,"content-disposition" => "form-data; name=\"file\"; filename=\"#{File.basename(src_package)}\"\r\n")
      
      retries = 0
      while (retries < 3)
        sleep 1 + retries
        begin
          res = Net::HTTP.start(uri.host, uri.port) do |http|
            http.request(req)
          end
          break
        rescue Errno::EHOSTUNREACH
          retries += 1
          next
        end
      end
      
      
      if res.code == "200"
        say_status 'upload', src_package
      else
        say 'upload failed.'
        say res.body
      end
      
    end
  end
  
  desc "execute NAME DEVICE_IP [WORKSPACE_DIR]","package and execute a droiuby application to target device running droiuby client"
  def execute(name, device_ip, source_dir = 'projects')
    `adb shell am start -W -S --activity-clear-top --activity-brought-to-front -n com.droiuby.client/.CanvasActivity`
    package name, source_dir, "true"
    upload name, device_ip, source_dir
  end
  
  private

  def compress(path, force = "false")
    path.sub!(%r[/$],'')
    unless Dir.exists?(File.join(path,'build'))
      Dir.mkdir(File.join(path,'build'))
    end
    archive = File.join(path,'build',"#{File.basename(path)}.zip")
    if force=='true' || file_collision(archive)

      FileUtils.rm archive, :force=>true

      Zip::File.open(archive, Zip::File::CREATE) do |zipfile|
        Dir["#{path}/**/**"].reject{ |f| f==archive || f.match(/\/build/) }.each do |file|
          say_status 'adding', file
          zipfile.add(file.sub(path+'/',''),file)
        end
      end
      say_status 'create', archive
    end
  end
end