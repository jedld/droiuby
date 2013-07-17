module Droiuby
  class Android
    class Otherwise
      
      def initialize
      end
      
      def otherwise(&block)
        @return_value = block.call
      end
      
      def value
        @return_value
      end
    end

    class DummyOtherwise
      
      def initialize(return_value)
        @return_value = return_value
      end
      
      def otherwise(&block)
        @return_value
      end
      
      def value
        @return_value
      end
    end

    def self.when_api(verb, level, &block)
      current_api_level = Java::android.os.Build::VERSION::SDK_INT
      case verb
      when :greater_than
        if current_api_level > level
          return DummyOtherwise.new(block.call)
        end
      when :less_than
        if current_api_level < level
          block.call
          return DummyOtherwise.new(block.call)
        end
      when :at_least
        if current_api_level >= level
          block.call
          return DummyOtherwise.new(block.call)
        end
      when :at_most
        if current_api_level <= level
          block.call
          return DummyOtherwise.new(block.call)
        end
      end
      return Otherwise.new
    end

  end
end