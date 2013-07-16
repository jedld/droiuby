module Droiuby
  module Wrappers
    module Listeners
      class OnClickListener
        def initialize(execution_bundle, auto_wrap_block)
          @auto_wrap_block = auto_wrap_block
          @native = Java::com.droiuby.wrappers::OnClickListenerRubyWrapper.new(execution_bundle, self)
        end
        
        def onClick(view)
          @auto_wrap_block.call(view)
        end
        
        def to_native
          @native
        end
      end
    end
  end
end