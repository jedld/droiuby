puts 'initializing bootstrap'

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
  elsif (view.class == Java::android.webkit.WebView)
    WebViewWrapper.new(view)
  elsif (view.class < Java::android.view.ViewGroup)
    ViewGroupWrapper.new(view)
  elsif (view.class < Java::android.view.View)
    ViewWrapper.new(view)
  else
    view
  end
end

def V(selectors)
  view = $current_activity_builder.findViewByName(selectors)
  wrap_native_view(view) if view
end

def _P
  Preferences.new($current_activity.getCurrentPreferences)
end

def async
  AsyncWrapper.new
end

def async_get(url, &block)
  async.perform {
    http_get(url)
  }.done { |result|
    block.call result
  }
end

def http_get(url)
  Java::com.dayosoft.activeapp.utils.Utils.load($current_activity, url);
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
