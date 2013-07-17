module Droiuby
  class SystemWrapper
    
    include JavaMethodHelper
    
    java_native_singleton Java::java.lang.System, :nanoTime, []
    java_native_singleton Java::java.lang.System, :currentTimeMillis, []
    java_native_singleton Java::java.lang.System, :currentTimeMillis, []
    java_native_singleton Java::java.lang.Thread, :sleep, [Java::long]
    java_native_singleton Java::java.lang.System, :gc, []
    java_native_singleton Java::android.util.Log, :d, [Java::java.lang.String, Java::java.lang.String]
    
  end
end