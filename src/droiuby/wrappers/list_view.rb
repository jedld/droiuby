class ListViewWrapper < ViewGroupWrapper
  
  def initialize(native = nil)
    @native = native
  end
  
  def set_adapter(adapter)
    @native.setAdapter(adapter.native)
  end
end