#speed up java native calls by binding the methods
module JavaMethodHelper
  
  def self.included(klass)
     klass.extend(ClassMethods)
  end
  
  module ClassMethods
    
    def java_native_singleton_on(object, java_klass, method_sym, params = [])
         unbound_method = java_klass.java_method(method_sym, params)
         define_singleton_method "java_#{method_sym.to_s}".to_sym do |*args|
           unbound_method.bind(object).call(*args)
         end
     end
     
    def java_native_method_on(object, java_klass, method_sym, params = [], method_name = nil)
      unbound_method = java_klass.java_method(method_sym, params)
      method_name = "java_#{method_sym.to_s}" if method_name.nil?
      define_method method_name.to_sym do |*args|
        unbound_method.bind(object).call(*args)
      end
    end
    
    
    def java_native_method(java_klass, method_sym, params = [], method_name = nil)
        unbound_method = java_klass.java_method(method_sym, params)
        method_name = "java_#{method_sym.to_s}" if method_name.nil?
        define_method method_name.to_sym do |*args|
          unbound_method.bind(self.native).call(*args)
        end
    end
    
    def java_native_singleton(java_klass, method_sym, params = [])
        unbound_method = java_klass.java_method(method_sym, params)
        define_singleton_method "java_#{method_sym.to_s}".to_sym do |*args|
          unbound_method.call(*args)
        end
    end
  end
  
end