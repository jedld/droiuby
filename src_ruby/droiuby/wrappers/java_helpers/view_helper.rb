#convenience methods to help droiuby deal with ruby-to-java calls
module Droiuby
  module ViewHelper
    
    def self.included(klass)
      klass.extend(ClassMethods)
    end
    
    module ClassMethods
      
      def java_attr_reader(*symbols)
        symbols.each do |s|
          java_getter_method = "get#{s.to_s.camelize}".to_sym
          define_method(s) do
            native.send(java_getter_method)
          end
        end
      end
      
      def java_attr_accessor(*symbols)
        symbols.each do |s|
          java_setter_method = "set#{s.to_s.camelize}".to_sym
          java_getter_method = "get#{s.to_s.camelize}".to_sym
          define_method(s) do
            native.send(java_getter_method)
          end
          define_method("#{s}=".to_sym) do |value|
            native.send(java_setter_method,value)
          end
        end
      end
      
    end
  end
end