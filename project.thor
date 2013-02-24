class Project < Thor
  
  include Thor::Actions
  
  source_root 'templates'
  
  desc "droiuby NAME [OUTPUT_DIR]","create a new droiuby project with NAME"
  def droiuby(name, output_dir = 'projects')
    @name = name
    @description = name
    @launcher_icon = ''
    @base_url = ''
    @main_xml = 'index.xml'
    
    dest_folder = File.join("#{output_dir}","#{name}") 
    template 'config.xml.erb', File.join("#{dest_folder}","config.xml")
    template 'index.xml.erb', File.join("#{dest_folder}","index.xml")
    template 'application.css.erb', File.join("#{dest_folder}","application.css")
    template 'index.rb.erb', File.join("#{dest_folder}","index.rb")
  end
end