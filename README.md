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
the website can use ruby scripts to handle events. Using Jruby, scripts are able
to access all libraries and services available to native applications. 

License
-------
Copyright (c) 2012, Joseph Emmanuel Dayo
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer. Redistributions in binary form
must reproduce the above copyright notice, this list of conditions and the
following disclaimer in the documentation and/or other materials provided with
the distribution. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.


