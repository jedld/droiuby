#Droiuby app introspection class
module Droiuby
  class Application
    def self.assets
      @active_app = @active_app || _current_app
      assets = {}
      @active_app.getAssets.each { |k,v|
        assets[k] = v
      }
      assets
    end
    
    def self.name
      @active_app = @active_app || _current_app
      @active_app.getName 
    end
    
    def self.base_url
      @active_app = @active_app || _current_app
      @active_app.getBaseUrl
    end
    
    
    def self.main_url
      @active_app = @active_app || _current_app
      @active_app.getMainUrl
    end
    
  end
end