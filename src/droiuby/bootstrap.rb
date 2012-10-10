puts 'initializing bootstrap'

def _scripting_container
  $container_payload.getContainer
end

def _current_app
  $container_payload.getActiveApp
end

def _execution_bundle
  $container_payload.getExecutionBundle
end

def _current_activity
  $container_payload.getCurrentActivity
end

def _activity_builder
  $container_payload.getActivityBuilder
end

def _current_page_url
  _execution_bundle.getCurrentUrl
end

def reverse_resolve(view_id)
  _activity_builder.reverseLookupId(view_id)
end

def launch(url)
  Java::com.droiuby.client.core.ActivityBuilder.loadApp(_current_activity, url) 
end

def render(url, params = {})
  http_method = Java::com.droiuby.client.utils.Utils::HTTP_GET
  if params[:method] && (params[:method] == :post)
    http_method = Java::com.droiuby.client.utils.Utils::HTTP_POST
  end
  
  new_activity = params[:new_activity] ? true : false;
  Java::com.droiuby.client.core.ActivityBuilder.loadLayout(_execution_bundle, _current_app, url, new_activity, http_method, _current_activity, nil, nil)
end

def toast(text = '', duration = :short)
  j_duration = Java::android.widget.Toast::LENGTH_SHORT

  j_duration = case(duration)
  when :short
    Java::android.widget.Toast::LENGTH_SHORT
  when :long
    Java::android.widget.Toast::LENGTH_LONG
  end

  Java::android.widget.Toast.makeText(_current_activity, text, j_duration).show();
end

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
  elsif (view.class < Java::android.widget.CompoundButton)
    CompoundButtonWrapper.new(view)
  elsif (view.class < Java::android.view.View)
    ViewWrapper.new(view)
  else
    view
  end
end

def wrap_motion_event(event)
  return nil unless event
  MotionEventsWrapper.new(event)  
end

def V(selectors = nil)
  if selectors.nil? # Get root node if nil
    view = _activity_builder.getRootView
  elsif (selectors == 'top')
    view = _activity_builder.getTopView
  else
    view = _activity_builder.findViewByName(selectors)
  end
  
  if (view.kind_of? Java::java.util.ArrayList)
    view.toArray.to_a.collect do |element|
      wrap_native_view(element)
    end
  else
    wrap_native_view(view) if view
  end
  
end

def _P
  Preferences.new(_current_activity.getCurrentPreferences)
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
  Java::com.droiuby.client.utils.Utils.load(_current_activity, url, _execution_bundle);
end

class ActivityWrapper
  def initialize
  end

  def me
    _current_activity
  end

  class << self
    def on_click(name, &block)
      view = V(name).tap { |v|
        v.native.setOnClickListener(Java::com.droiuby.client.core.OnClickListenerBridge.new(_execution_bundle, v.id))
      }
      define_method("on_click_listener_for_#{view.id.to_s}".to_sym) do |n_view|
        _current_activity.instance_exec(wrap_native_view(n_view),&block)
      end
    end
  end

end
