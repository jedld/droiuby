#!/bin/sh
rm -r src_ruby_classes
mkdir src_ruby_classes
cp -r src_ruby src_ruby_classes
mkdir src_ruby_classes/output
cd src_ruby_classes/src_ruby
export JRUBY_OPTS=--1.9
jrubyc .
find . -name '*.class' | cpio -pdm ../output
cd ../output
jar -cf ../../lib/precomplie.jar .


