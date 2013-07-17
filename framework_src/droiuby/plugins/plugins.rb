module Droiuby
  class Plugins
    
    def self.descendants
      @descendants ||= []
    end

    def self.inherited(descendant)
      descendants << descendant
    end

    #ran after all classes are initialized and before executing the MainActivity on_create method
    def after_bootstrap
    end
    
    #after a partial is rendered
    def after_partial_setup(view)
    end
    
    #after a complete view is rendered
    def after_view_setup
    end

  end
end