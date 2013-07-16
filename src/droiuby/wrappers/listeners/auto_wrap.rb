module Droiuby
  module Wrappers
    module Listeners
      class AutoWrap
        
        def initialize(execution_bundle, auto_wrap_block)
          @auto_wrap_block = auto_wrap_block
        end
        
        def method_missing(meth, *args, &block)
          if meth.to_s =~ /^on(.+)$/
            @auto_wrap_block.call(*args)
          else
            super # You *must* call super if you don't handle the
                  # method, otherwise you'll mess up Ruby's method
                  # lookup.
          end
        end
              
        def to_native(klass)
          eval("Java::com.droiuby.wrappers::#{klass.to_s}RubyWrapper").new(execution_bundle, self)
        end
      end
    end
  end
end