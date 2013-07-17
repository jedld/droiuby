require 'droiuby/wrappers/java_helpers/view_helper'

class IntentWrapper
  
  include Droiuby::ViewHelper
  
  java_attr_reader :action 
  
  def initialize(intent)
    if (intent.nil?)
      @native = Java::android.content.Intent.new
    else
      @native = intent
    end
  end
  
  def get_string_extra(name)
    @native.getStringExtra(name)
  end
  
  def native
    @native
  end
end