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
end


def on_activity_result(request_code, result_code, intent)
  app_url = V('#app_url')
  scanResult = Java::com.droiuby.client.utils.intents.IntentIntegrator.parseActivityResult(request_code, result_code, intent.native)
  if scanResult != nil
    app_url.text= scanResult.getContents()
  end
end