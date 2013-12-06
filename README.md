Droiuby
=======

What is it?
-----------

Droiuby is an android client and framework that allows you to quickly create android apps for rapid 
prototyping capitalizing on the expressive and dynamic nature of the ruby language. Since it sits on 
top of the Android Framework (and not HTML), droiuby applications are also able to access features comparable
to a native app, this includes the devices' hardware (GPS, Camera,accelerometer) and facilitate 
on-device interaction using an event driven framework. A library is provided 
that wraps native controls that significantly simplifies common tasks usually done in 
a very verbose manner using Java.

The droiuby framework also treats web apps as first class citizens and allows you to 
create droiuby apps with code and assets that are served entirely from the web.

How does it work?
-----------------

The Droiuby framework consists of the following:

1.) An opensource Android app (droiuby-core) - Pure Java code that handles Android bootstrapping, 
    debugging and JRuby instance management etc.

2.) The Droiuby Framework - Consists of ruby code that serves as the bridge between your app and Java and 
    wraps Android Objects to easy in to easy to use Ruby Wrappers.


Remember this is still highly experimental and things could change really quickly.

Why use Droiuby?
----------------

- You are experienced with ruby and rails projects and want to try mobile app development.

- The Android development workflow feels too cumbersome and slow.

- You are already an android developer and is interested to find out how a language like ruby can lessen the
  frustration on working with the Android SDK and Framework. (see Animation - Ruby vs Java)

- You want something to quickly demo before you dwell into hardcore Android development. The rapid feedback 
  and "code and refresh" nature of droiuby is perfect for rapid prototyping.
  
- You want to create a hybrid native/ web app. Droiuby allows all or part of your app assets to be served
  by a web app.

Installation / Documentation / Usage
------------------------------------

The recommended way is using through the droiuby gem

    gem install droiuby

Then create your first app

    drby new sample
    
Please refer to the wiki for additional docs

https://github.com/jedld/droiuby/wiki


Samples
-------

Animation - Ruby vs Java

Below is an example on how the droiuby framework simplifies some tasks:

scaling a view and then exit left (JAVA):

    View view = (View)context.findViewById(R.id.Text1);
	AnimatorSet set = new AnimatorSet();
	ObjectAnimator animator = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.5f);
	animator.setDuration(250);
	ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.5f);
	animator.setDuration(250);
	set.playTogether(animator, animator2);
	set.addListener(new AnimatorListener() {
	
	        @Override
	        public void onAnimationCancel(Animator animation) {
	                // TODO Auto-generated method stub
	                
	        }
	
	        @Override
	        public void onAnimationEnd(Animator animation) {
	                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	                Display display = wm.getDefaultDisplay();
	                
	                ObjectAnimator animator = ObjectAnimator.ofInt(view, "left", view.getLeft(), display.getWidth());
	                                animator.setDuration(250);
	                animator.addListener(finalListener);
	                animator.start();
	        }
	
	        @Override
	        public void onAnimationRepeat(Animator animation) {
	                // TODO Auto-generated method stub
	                
	        }
	
	        @Override
	        public void onAnimationStart(Animator animation) {
	                // TODO Auto-generated method stub
	                
	        }
	        
	});
	set.start();

Ruby (Droiuby Framework):

    view = ('#Text1')
    view.animate { |v| 
      v.scale_x 1.0, 0.5, {duration: 250}
      v.scale_y 1.0, 0.5, {duration: 250}
    }.after( 
      view.animate { |v|
        v.left v.left, Display.width, {duration: 250}
      }
    ).start


Issues
------

- Still alpha, a lot still needs to be done to support all of features of the Android SDK
- Slow initial loading of apps

Similar Projects
----------------

- ruboto
- phonegap
- appcelerator


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

