module Droiuby
  module Wrappers
    module Listeners

      class AutoWrapBase
        def to_native(klass)
          eval("Java::com.droiuby.wrappers::#{klass.to_s}RubyWrapper").new(@execution_bundle, self)
        end
      end
      
      class AutoWrap < AutoWrapBase
        
        def initialize(execution_bundle, auto_wrap_block)
          @execution_bundle = execution_bundle
          @auto_wrap_block = auto_wrap_block
        end
        
        def method_missing(meth, *args, &block)
          if meth.to_s =~ /^on(.+)$/
            wrapped_args = args.collect { |a|
              wrap_native_view(a)
            }
            @auto_wrap_block.call(*wrapped_args)
          else
            super # You *must* call super if you don't handle the
                  # method, otherwise you'll mess up Ruby's method
                  # lookup.
          end
        end
              
        
      end
      
      class AutoWrapMultiple < AutoWrapBase
        
        def initialize(execution_bundle, impl_blocks = {})
          @execution_bundle = execution_bundle
          @auto_wrap_blocks = impl_blocks
        end
        
        def impl(method, &block)
          @auto_wrap_block["on#{meth.to_s.camelize}"] = block
        end
        
        def method_missing(meth, *args, &block)
          if meth.to_s =~ /^on(.+)$/
            wrapped_args = args.collect { |a|
              wrap_native_view(a)
            }
            @auto_wrap_block[meth.to_s].call(*wrapped_args)
          else
            super # You *must* call super if you don't handle the
                  # method, otherwise you'll mess up Ruby's method
                  # lookup.
          end
        end
      end
      
      def on(event,&block)
        listener_ref = event.to_s.camelize
        self.native.send(:"setOn#{listener_ref}Listener",_listener("On#{listener_ref}Listener", &block))
      end
      
      protected
      
      def _listener(java_class, &block)
        Droiuby::Wrappers::Listeners::AutoWrap.new(_execution_bundle, block).to_native(java_class)  
      end
      
    end
  end
end