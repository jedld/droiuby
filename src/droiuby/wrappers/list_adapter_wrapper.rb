class ListAdapterWrapper
  
  def initialize
    @native = Java::com.droiuby.client.core.wrappers.ListViewAdapterWrapper.new(_execution_bundle)
  end
  
  def native
    @native
  end
  
  def get_count(&block)
    @native.setGetCountBody(block)
    self
  end
  
  def get_item(&block)
    @native.setGetItemBody(block)
    self
  end
  
  def get_item_id(&block)
    @native.setGetItemIdBody(block)
    self
  end
  
  def get_item_view_type(&block)
    @native.setGetItemViewTypeBody(block)
    self
  end
  
  def get_view(&block)
    @native.setGetViewBody(block)
    self
  end
  
  def get_view_type_count(&block)
    @native.setGetViewTypeCountBody(block)
    self
  end
  
  def has_stable_id?(&block) 
    @native.setHasStableIdBody(block)
    self
  end
  
  def is_empty?(&block)
    @native.setIsEmptyBody(block)
    self
  end
  
  def register_data_set_observer(&block)
    @native.setRegisterDataSetObserverBody(block)
    self
  end
  
  def unregister_data_set_observer(&block)
    @native.unregisterDataSetObserver(block)
    self
  end
  
end