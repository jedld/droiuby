class Main < Activity
  def on_create
      V('#run').on(:click) do |view|
          puts view.class.to_s
          puts view.data('url')
          app_url = V('#app_url')
          launch app_url.text
      end
      
      V('#qrcode').on(:click) do |view|
        integrator = Java::com.droiuby.client.utils.intents.IntentIntegrator.new(me)
        integrator.initiateScan
      end
      
    async.perform {
      Java::com.droiuby.client.utils.Utils.getLocalIpAddress(_current_activity)
    }.done { |result|
      V('#ip_address').text = "#{result}:4000"
    }.start
  end
  
  def on_activity_result(request_code, result_code, intent)
    app_url = V('#app_url')
    scanResult = Java::com.droiuby.client.utils.intents.IntentIntegrator.parseActivityResult(request_code, result_code, intent.native)
    if scanResult != nil
      app_url.text= scanResult.getContents()
    end
  end
end