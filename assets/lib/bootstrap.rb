puts 'initializing bootstrap'

require 'json'

$current_activity = container_payload.getCurrentActivity
$current_activity_builder = container_payload.getActivityBuilder
$scripting_container = container_payload.getContainer

puts $current_activity.getClass.toString

#wraps a generic view
class ViewWrapper
  def initialize(view)
    @view = view
  end

  def id
    @view.getId
  end

  def native
    @view
  end
  
  def gone=(flag)
      @view.setVisibility(Java::android.view.View::GONE) if flag
  end
  
  def gone?
    @view.getVisibility == Java::android.view.View::GONE
  end
  
  def visible=(flag)
    @view.setVisibility(flag ? Java::android.view.View::VISIBLE : Java::android.view.View::INVISIBLE)
  end
  
  def visible?
    @view.getVisibility == Java::android.view.View::VISIBLE
  end
  
  def enabled=(flag)
    @view.setEnabled(flag)
  end
  
  def enabled?
    @view.isEnabled
  end
  
  def rotation
    @view.getRotation
  end
  
  def rotation=(rotation)
    @view.setRotation(rotation)
  end
  
  def pivot_x
    @view.getPivotX
  end
  
  def pivot_x=(pivot)
    @view.setPivotX(pivot)
  end
  
  def pivot_y
    @view.getPivotY
  end
  
  def pivot_y=(pivot)
    @view.setPivotY(pivot)
  end
  
end

class ViewGroupWrapper < ViewWrapper
  
end

class TextViewWrapper < ViewWrapper
  
  def text=(text)
      @view.setText(text)
  end
  
  def text
      @view.getText
  end
end

class EditTextWrapper < ViewWrapper
  
  def text=(text)
      @view.setText(text)
  end
  
  def text
      @view.getText.toString
  end
end

def V(selectors)
  view = $current_activity_builder.findViewByName(selectors)
  if (view.class == Java::android.widget.TextView)
    TextViewWrapper.new(view)
  elsif (view.class == Java::android.widget.EditText)
    EditTextWrapper.new(view)
  else
    ViewWrapper.new(view)
  end
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
      define_method("on_click_listener_for_#{view.id.to_s}".to_sym) do
        $main_activty.instance_exec &block
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
