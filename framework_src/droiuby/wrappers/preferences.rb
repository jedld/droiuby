class Preferences
  
  def initialize(preferences)
    @preferences = preferences
  end
  
  def has_key?(key)
    @preferences.contains(key.to_s)
  end
  
  def contains?(key)
    has_key? key
  end
  
  def get(key, default = nil)
    prefs = (safe_get(key, nil) { |k, d| @preferences.getBoolean(k, nil) }) ||
    (safe_get(key, nil) { |k, d| @preferences.getFloat(k, nil) }) ||
    (safe_get(key, nil) { |k, d| @preferences.getInt(k, nil) }) ||
    (safe_get(key, nil) { |k, d| @preferences.getLong(k, nil) }) ||
    (safe_get(key, nil) { |k, d| @preferences.getString(k, nil) })
    prefs.nil? ? default : prefs
  end
  
  def update_attributes(attributes = {})
    editor = @preferences.edit
    attributes.each { |k,v|
      k = k.to_s
      if v.kind_of? String
        editor.putString(k,v)
      elsif v.kind_of? Integer
        editor.putInt(k,v)
      elsif v.kind_of? Long
        editor.putLong(k,v)
      elsif v.kind_of? Float
        editor.putFloat(k,v)
      elsif v == :remove
        editor.remove(k)
      end
    }
    editor
  end
  
  def update_attributes!(attributes = {})
    update_attributes(attributes).commit
  end
  
  private
   
  def safe_get(key, default, &block)
    begin
      block.call key.to_s, default
    rescue Java::java.lang.ClassCastException=>e
      nil
    end
  end
  
end