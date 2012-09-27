require 'droiuby/wrappers/view_wrapper'

class WebViewWrapper < ViewGroupWrapper
  def src
    @native.getUrl
  end

  def src=(url)
    @native.loadUrl(url)
  end

  def reload!
    @native.reload
  end

end