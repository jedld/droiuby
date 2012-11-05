class AsyncWrapper
  def initialize
    @native = Java::com.droiuby.client.core.async.AsyncWrapper.new(_execution_bundle)
  end
  
  def native
    @native
  end
  
  def before(&block)
    @native.setPre_execute(block)
    self
  end
  
  def perform(&block)
    @native.setBackground_task(block)
    self
  end
  
  def done(&block)
    @native.setPost_execute(block)
    self
  end
  
  def execute
    @native.execute(nil, nil, nil)
    self
  end
  
  def start
    execute
  end
end