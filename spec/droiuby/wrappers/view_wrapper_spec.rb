require 'spec_helper.rb'

describe ViewWrapper do
  before do
    @fixture = ViewWrapper.new
  end
  
  it "returns a new view instance" do
    @fixture.should be
  end
end