class Main < Activity
  def on_create
    async.perform {
      Java::com.droiuby.client.core.utils.Utils.getLocalIpAddress(_current_activity)
    }.done { |result|
      V('#ip_address').text = "#{result}:4000"
    }.start
  end
  
  def on_activity_result(request_code, result_code, intent)
  end
end