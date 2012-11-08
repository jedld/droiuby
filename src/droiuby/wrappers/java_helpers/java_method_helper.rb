#speed up java native calls by binding the methods
module JavaMethodHelper
  
  def self.included(klass)
     klass.extend(ClassMethods)
   end
  
  module ClassMethods
    
    def java_native_method(java_klass, method_sym, params = [])
        unbound_method = java_klass.java_method(method_sym, params)
        define_method "java_#{method_sym.to_s}".to_sym do |*args|
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