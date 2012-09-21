Droiuby
=======

What is it?
-----------

Droiuby is an android client and framework that enables web applications to
render content on your android device using native controls. Using ruby, web
applications are also able to access the devices' hardware (GPS, Camera,
accelerometer) and facilitate on-device interaction using an event driven
framework. A library is provided that wraps native controls that significantly
simplifies common tasks usually done in Java.

Although its primary goal is to enable web applications to look and act like
native apps on the phone, you can still use droiuby on your existing android
applications to leverage the power of ruby.

How does it work?
-----------------

Using the droiuby application running on your device, it parses specially formatted
websites that uses droiuby markup and contains ruby scripts. The markup is 
designed to correspond directly to native controls and widgets. To facilitate client 
side interaction (like javascript) the website can use ruby scripts to handle events. 
Using JRuby, scripts are able to access all libraries and services available to native applications.

To allow developers to 'debug' the application (similar to the Chrome "inspect" and Rails Console)
Droiuby opens up a webserver at port 4000 on your phone that provides a web console
to allow you to interact with the app in realtime.

Remember this is still highly experimental and things could change really quickly.

Why use Droiuby?
----------------

- Droiuby allows you to easily convert your existing site so that it uses native controls on your
device. It is not based on HTML/javascript but rather uses a custom markup that translates
"directly" to a native control. Also it uses JRuby as a "frontend" scripting language
which enables you to have direct access to native classes while taking advantage of ruby's
"programmer friendliness".

- If you already have a website, you only need to create a separate set of markup for droiuby and switch
to that markup when a droiuby user agent is detected (a mechanism existing sites already use
when handling mobile devices)

- The "code and refresh" nature of droiuby is perfect for rapid prototyping a native app to your
product managers and clients.

So what does the "app" look like?
-----------------------------

A droiuby enabled web app consists of a minimum of 2 files (3 if there is a ruby script)

- A config file
- The main template file (The template can contain a reference to a ruby script that allows it to handle 'events'
- (optional) A ruby script

The structure is designed so that a web server (rails app) can easily host these files

Config file. Basically this is where you point the droiuby client in order for it to 'load' the app

	<app_descriptor>
	    <name>Hello World App</name>
	    <description>An android active app that displays hello world!!!</description>
	    <launcher_icon>http://www.android.com/images/brand/droid.gif</launcher_icon>
	    <base_url>http://localhost:3000/hello_world/</base_url>
	    <main>main.xml</main>
	</app_descriptor>
	
Right now it connects to http://localhost:3000/hello_world/ but you can also tell it to load it from
the apps assets folder (e.g. asset:hello_world/) 
	
main.xml - The template which renders the layout. Although it looks like HTML each tag corresponds to and android widget.

	<activity controller="main.rb">
	    <layout type="linear" width="match">
	        <t class="exclamation text" > what!!!!</t>
	        <t size="20">Different font size</t>
	        <t size="18">Different font size</t>
	        <input id="test_field" value="This is a text field"></input>
	        <button id='store_text' width="match">Store</button>
	        <button id='test_button' width="match">test</button>
	        <button alpha="0.5" width="match">test</button>
	        <img src="http://upload.wikimedia.org/wikipedia/en/a/a5/Android-logo.jpg" width="100"/>
	        <div id="section">
	            <layout type="relative" width="match">
	                <input parent_left="1" id="test_field2" value="Test text field 2"/>
	                <button id="some_button" right_of="#test_field2" value="test button" width="100">
	                    BUTTON
	                </button>    
	            </layout>
	        </div>
	        <div id="hello_world_section">
	            
	        </div>
	    </layout>
	</activity>

main.rb - A ruby script that allows you to hook on to events (notice the controller="main.rb" in the template)

	def on_create
	  puts 'Hello world from controller file v1'
	  puts "Access to current activity = #{$current_activity.getClass.toString}"
	  
	  #access EditText objects and store and load from shared preferences
	  
	  V('#test_field').text = 'prefs here'
	  if _P.contains? :some_text
	    some_text = _P.get(:some_text)
	    puts "Setting text #{some_text} from preferences"
	    V('#test_field').text = some_text
	  end
	  
	  #event handling
	  V('#store_text').on(:click) do |v|
	    toast 'storing in prefs'
	    _P.update_attributes!(some_text: V('#test_field').text)
	  end
	  
	  V('#test_button').on(:click) do |v|
	    puts "test_button #{v.id} was clicked!!!!!! via on clicked" 
	    toast 'test_button was clicked!!!'
	    V('#section').inner = '<t size="20">Clicked!!!!</t>'
	    
	  # Animation in android. ruby actually makes it look cool
	  
	    V('#section').animate { |t|
	      t.alpha 0, 1, {duration: 2000}
	    }.with(
	      V('#test_button').animate { |t|
	        t.alpha 1, 0, {duration: 1000}
	      } 
	    ).start
	    
	   # how to define Async tasks
	    async.perform {
	      #query stuff and pass it to "done" which hooks it internally to onPostExecute
	      
	      query_url "asset:hello_world/_hello_world.xml"
	    }.done { |result|
	      V('#hello_world_section').inner = result
	    }.start
	    
	   end
	  
	  
	  V('#test_button').on(:long_click) { |v|
	    puts "This button was long clicked!!!!!!"
	    activity_instance_method('hi')
	    
	    #yes you can do jquery style "HTML replacement"
	    V('#section').inner = '<t size="20">Long Clicked!!!!</t>'
	    true #consume long click
	  }
	  
	end
	
	def activity_instance_method(str)
	  puts "This instance method was called #{str}"
	end

Installation / Documentation / Usage
------------

Refer to the wiki here:

[https://github.com/jedld/droiuby/wiki](https://github.com/jedld/droiuby/wiki)

Issues
------

- Apps are limited to one activity only since you can't really just create new activities programmatically in android.
- Slow initial loading of apps

Similar Projects
----------------

- ruboto
- phonegap
- appcelerator

Compile Notes
-------------

- If you want to compile this app yourself make sure eclipse has at least 1.5G (--vmargs -Xmx2G) of heap memory allocated to it as
the Dex compiler uses a "lot" of memory trying to optimize jruby.jar
- A 64-bit OS since you need memory 

If you think it looks cool or not, I'm happy for some feedback and suggestions

License
-------
 Copyright 2012 Joseph Emmanuel Dayo

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

