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

How does it work?
-----------------

Using the droiuby application running on your device, it parses specially formatted
websites that uses droiuby markup and contains ruby scripts. The markup is 
designed to correspond directly to native controls and widgets while using some
familiar HTML constructs. To facilitate client side interaction (like javascript)
the website can use ruby scripts to handle events. Using JRuby, scripts are able
to access all libraries and services available to native applications. 

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

