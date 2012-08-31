puts 'initializing bootstrap'

require 'json'
require 'active_support'
require 'droiuby/wrappers/view_group_wrapper'
require 'droiuby/wrappers/linear_layout'
require 'droiuby/wrappers/text_view'
require 'droiuby/wrappers/edit_text'


$current_activity = container_payload.getCurrentActivity
$current_activity_builder = container_payload.getActivityBuilder
$scripting_container = container_payload.getContainer

puts $current_activity.getClass.toString

def wrap_native_view(view)
  return nil unless view
  if (view.class == Java::android.widget.TextView)
      TextViewWrapper.new(view)
    elsif (view.class == Java::android.widget.EditText)
      EditTextWrapper.new(view)
    elsif (view.class == Java::android.widget.LinearLayout)
      LinearLayoutWrapper.new(view)
    elsif (view.class < Java::android.view.ViewGroup)
      ViewGroupWrapper.new(view)
    else
      ViewWrapper.new(view)
    end
end

def V(selectors)
  view = $current_activity_builder.findViewByName(selectors)
  wrap_native_view(view) if view
end

class ActivityWrapper
  def initialize
  end

  def me
    $current_activity
  end

  class << self
    def on_click(name, &block)
      view = V(name).tap { |v|
        v.native.setOnClickListener(Java::com.dayosoft.activeapp.core.OnClickListenerBridge.new($scripting_container, v.id))
      }
      define_method("on_click_listener_for_#{view.id.to_s}".to_sym) do |n_view|
        $main_activty.instance_exec(wrap_native_view(n_view),&block)
      end
    end
  end

  protected

  def toast(text = '', duration = :short)
    j_duration = Java::android.widget.Toast::LENGTH_SHORT

    j_duration = case(duration)
    when :short
      Java::android.widget.Toast::LENGTH_SHORT
    when :long
      Java::android.widget.Toast::LENGTH_LONG
    end

    Java::android.widget.Toast.makeText(me, text, j_duration).show();
  end
end
