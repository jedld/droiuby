def on_create
  puts 'Hello world from controller file v1'
  puts "Access to current activity = #{$current_activity.getClass.toString}"
end

on_click('#test_button') do |v|
  puts "test_button #{v.id} was clicked!!!!!!" 
  toast 'tset_button was clicked!!!'
  V('#section').inner = '<t size="20">Clicked!!!!</t>' 
end


