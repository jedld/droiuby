class AsyncWrapper
  def initialize
    @native = Java::com.dayosoft.activeapp.core.async.AsyncWrapper.new($scripting_container)
  end
  
  def native
    @native
  end
  
  def before(&block)
    @native.setPre_execute(&block)
    self
  end
  
  def perform(&block)
    @native.setBackground_task(&block)
    self
  end
  
  def done(&block)
    @native.setPost_execute(&block)
    self
  end
  
  def execute
    @native.execute(nil, nil, nil)
  end
  
  def start
    execute
  end
end