class AssetHandler
  
  def initialize(url)
    @url
  end
  
  def self.download(url)
    AssetHandler.new(url)
  end
  
  def start
  async.perform {
    Java::com.droiuby.client.utils.Utils.loadAppAsset(_current_app, _current_activity,
      @url, Java::com.droiuby.client.utils.Utils::ASSET_TYPE_IMAGE, Java::com.droiuby.client.utils.Utils::HTTP_GET);
  }.done { |result|
    @block.call(result)
  }.start
  end
  
  def done(&block)
    @block = block
  end
end