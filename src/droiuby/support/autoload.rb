
class Object
  class << self
    alias :const_missing_old :const_missing
    def const_missing(name)
      puts "constant missing #{name}"
      @looked_for ||= {}
      str_name = name.to_s
      raise "Class not found: #{name}" if @looked_for[str_name]
      @looked_for[str_name] = 1
      
      name_parts = name.split('::').collect { |n| n.underscore }
      require_path = File.join(*name_parts)
      
      puts "autoloading #{require_path}"
      require require_path
      klass = const_get(name)
      return klass if klass
      raise "Class not found: #{name}"
    end
  end
end
