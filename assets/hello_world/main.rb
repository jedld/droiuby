def on_create
  puts 'Hello world from controller file v1'
  puts "Access to current activity = #{$current_activity.getClass.toString}"

  V('#test_button').on(:click) do |v|
    puts "test_button #{v.id} was clicked!!!!!! via on clicked" 
      toast 'tset_button was clicked!!!'
      V('#section').inner = '<t size="20">Clicked!!!!</t>'
  end
  
  V('#test_button').on(:long_click) do |v|
    puts "This button was long clicked!!!!!!"
    activity_instance_method('hi')
    V('#section').inner = '<t size="20">Long Clicked!!!!</t>'
    true #consume long click
  end
end

def activity_instance_method(str)
  puts "This instance method was called #{str}"
end



