class BitmapDrawableWrapper
  def initialize(drawable)
    @native = drawable
  end
  
  def native
    @native
  end
  
  def to_bitmap
    @native.getBitmap
  end
  
  def height
    @native.getIntrinsicHeight
  end
  
  def width
    @native.getIntrinsicWidth
  end
end

class AssetHandler
  
  def initialize(url)
    @url = url
  end
  
  def self.download(url)
    AssetHandler.new(url)
  end
  
  def start
    async.perform {
      result = BitmapDrawableWrapper.new(Java::com.droiuby.client.core.utils.Utils.loadAppAssetRuby(_execution_bundle, _current_app, _current_activity,
      @url, Java::com.droiuby.client.core.utils.Utils::ASSET_TYPE_IMAGE, Java::com.droiuby.client.utils.Utils::HTTP_GET))
      result
    }.done { |result|
      @block.call(result)
    }.start
  end
  
  def done(&block)
    @block = block
    self
  end
end