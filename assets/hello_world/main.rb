def on_create
  puts 'Hello world from controller file v1'
  puts "Access to current activity = #{$current_activity.getClass.toString}"
  
  V('#test_field').text = 'prefs here'
  if _P.contains? :some_text
    some_text = _P.get(:some_text)
    puts "Setting text #{some_text} from preferences"
    V('#test_field').text = some_text
  end
  
  V('#store_text').on(:click) do |v|
    toast 'storing in prefs'
    _P.update_attributes!(some_text: V('#test_field').text)
  end
  
  V('#test_button').on(:click) do |v|
    puts "test_button #{v.id} was clicked!!!!!! via on clicked" 
    toast 'test_button was clicked!!!'
    V('#section').inner = '<t size="20">Clicked!!!!</t>'
    
    #animation
    V('#section').animate { |t|
      t.alpha 0, 1, {duration: 2000}
    }.with(
      V('#test_button').animate { |t|
        t.alpha 1, 0, {duration: 1000}
      } 
    ).start
    
    #async task demonstration
    async.perform {
      query_url "asset:hello_world/_hello_world.xml"
    }.done { |result|
      V('#hello_world_section').inner = result
    }.start
    
   end
  
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



