def on_create
    V('#run').on(:click) do |view|
        app_url = V('#app_url')
        launch app_url.text
    end
end