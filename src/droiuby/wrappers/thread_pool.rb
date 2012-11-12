class ThreadPoolWrapper
  
  def initialize
    @native = com.droiuby.client.core.wrappers.ThreadPoolWorkerWrapper.new
  end
  
  def native
    @native
  end
  
  def task(&block)
    @native.addTask(Java::com.droiuby.client.core.wrappers.ThreadWrapper.new(block, _execution_bundle))
  end
  
  def start
    @native.start
  end
end