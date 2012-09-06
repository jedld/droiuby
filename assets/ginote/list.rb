def on_create
  V('#add_note').on(:click) do |v|
    async.perform { 
      
    }.done { |result|
      
    }
  end
end