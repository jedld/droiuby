puts 'initializing bootstrap'

include JavaMethodHelper::ClassMethods

class PayloadWrapper
  [:getContainer, :getActiveApp, :getExecutionBundle, :getActivityBuilder].each do |method|
    java_native_singleton_on($container_payload, Java::com.droiuby.client.core.RubyContainerPayload, method, [])
  end
  
  java_native_singleton_on($container_payload.getExecutionBundle, Java::com.droiuby.client.core.ExecutionBundle, 
    :getCurrentActivity, [])
      
end

def _container_payload
  $container_payload
end

def _scripting_container
  PayloadWrapper.java_getContainer
end

def _current_app
  PayloadWrapper.java_getActiveApp
end

def _execution_bundle
  PayloadWrapper.java_getExecutionBundle
end

def _current_activity
  PayloadWrapper.java_getCurrentActivity
end

def _activity_builder
  PayloadWrapper.java_getActivityBuilder
end

def _current_page_url
  _execution_bundle.getCurrentUrl
end

def reverse_resolve(view_id)
  _activity_builder.reverseLookupId(view_id)
end

def _active_bundles
  Java::com.droiuby.client.core.ExecutionBundleFactory.listActiveBundles.collect { |i| i.to_s }
end

def _switch(bundle)
  bundle = Java::com.droiuby.client.core.ExecutionBundleFactory.getBundle(bundle)
  $container_payload = bundle.getPayload
end

def _namespace
  _execution_bundle.getName
end

def launch(url)
  Java::com.droiuby.client.core.ActivityBuilder.loadApp(_current_activity, url) 
end

def render(url, params = {})
  http_method = Java::com.droiuby.client.core.utils.Utils::HTTP_GET
  if params[:method] && (params[:method] == :post)
    http_method = Java::com.droiuby.client.core.utils.Utils::HTTP_POST
  end
  
  new_activity = params[:activity] ? true : false;
  Java::com.droiuby.client.core.ActivityBuilder.loadLayout(_execution_bundle, _current_app, url,
    new_activity, http_method, _current_activity, nil, nil, Java::com.droiuby.client.core.ActivityBuilder.getViewById(_current_activity, 'mainLayout'))
  #execute plugins
  after_view_setup
end

def log_debug(message = '', tag = 'ruby')
  Droiuby::SystemWrapper.java_d(tag.to_s, message.to_s)
  nil
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

def _gc
  Droiuby::SystemWrapper.java_gc
end

def _nano_time
  Droiuby::SystemWrapper.java_nanoTime
end

def _time
  Droiuby::SystemWrapper.java_currentTimeMillis
end

def _sleep(delay = 0)
  Droiuby::SystemWrapper.java_sleep(delay)
end

def _thread(&block)
  Java::com.droiuby.client.core.wrappers.ThreadWrapper.new(block, _execution_bundle)
end

def _pool(&block)
  thread_pool = ThreadPoolWrapper.new
  block.call(thread_pool)
  thread_pool.start
end

def wrap_native_view(view)
  return nil unless view

  wrapped = if (view.class < Java::android.view.View)
    if (view.class == Java::android.widget.TextView)
        TextViewWrapper.new(view)
      elsif (view.class == Java::android.widget.EditText)
        EditTextWrapper.new(view)
      elsif (view.class == Java::android.widget.LinearLayout)
        LinearLayoutWrapper.new(view)
      elsif (view.class == Java::android.webkit.WebView)
        WebViewWrapper.new(view)
      elsif (view.class == Java::android.widget.ListView)
        ListViewWrapper.new(view)
      elsif (view.class < Java::android.view.ViewGroup)
        ViewGroupWrapper.new(view)
      elsif (view.class < Java::android.widget.CompoundButton)
        CompoundButtonWrapper.new(view)
      elsif (view.class < Java::com.droiuby.client.core.wrappers.SurfaceViewWrapper)
        SurfaceViewWrapper.new(view)
      elsif (view.class < Java::android.view.View)
        ViewWrapper.new(view)
      else
        view
     end
  elsif (view.class == Java::android.view.MotionEvent)
    MotionEventsWrapper.new(event)
  end
  
  wrapped
end

def wrap_native(object)
  if (object.class == Java::android.content.Intent)
    return IntentWrapper.new(object)
  end
end


def surface(&block)
  s = SurfaceViewWrapper.new
  block.call(s)
  s
end

def canvas(&block)
  auto_wrap_block = Proc.new { |v| block.call(Canvas.new(v))}
  wrap_native_view(Java::com.droiuby.client.core.wrappers.ViewWrapper.new(auto_wrap_block, _execution_bundle))
end

def Android
  Droiuby::Android
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
  Preferences.new(Java::com.droiuby.client.core.utils.Utils.getCurrentPreferences(_current_app, _current_activity))
end

def async
  AsyncWrapper.new
end

def async_get(url, params = {}, options ={}, &block)
  async.perform {
    http_get(url, params, options)
  }.done { |result|
    block.call result
  }
end

def http_get(url, params = {}, options = {})
  
  encoded_params = []
    
  params.each do |k,v|
    encoded_params << "#{k.to_s}=#{CGI::escape(v.to_s)}"
  end
  
  url_string = url
  
  if encoded_params.size > 0
    url_string = "#{url}?#{encoded_params.join('&')}"
  end
  
  Java::com.droiuby.client.core.utils.Utils.load(_current_activity, url_string, _execution_bundle);
end

class Activity
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

  def on_activity_result(request_code, result_code, intent)
    
  end
  
  def on_activity_reload
  end
  
  def no_action_bar
    _current_activity.requestWindowFeature(Java::android.view.Window::FEATURE_NO_TITLE);
    nil
  end
  
end

