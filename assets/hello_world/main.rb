def on_create
  puts 'Hello world from controller file v1'
  puts "Access to current activity = #{$current_activity.getClass.toString}"
end

on_click('#test_button') do
  puts 'test_button was clicked!!!!!!' 
  toast 'tset_button was clicked!!!' 
end


