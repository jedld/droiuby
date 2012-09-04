def on_create
  puts 'Hello world from controller file v1'
  puts "Access to current activity = #{$current_activity.getClass.toString}"

  V('#test_button').on(:click) { |v|
    puts "test_button #{v.id} was clicked!!!!!! via on clicked" 
    toast 'test_button was clicked!!!'
    V('#section').inner = '<t size="20">Clicked!!!!</t>'
    
    #animation
    V('#section').animate { |t|
      t.alpha 0, 1, {duration: 2000}
    }.start
  }
  
  V('#test_button').on(:long_click) { |v|
    puts "This button was long clicked!!!!!!"
    activity_instance_method('hi')
    V('#section').inner = '<t size="20">Long Clicked!!!!</t>'
    true #consume long click
  }
  
end

def activity_instance_method(str)
  puts "This instance method was called #{str}"
end



