require 'active_support/time'
require 'active_support/core_ext/array/wrap'
require 'active_support/core_ext/hash/reverse_merge'
require 'active_support/core_ext/object/blank'
require 'active_support/core_ext/string/inflections'

class Hash

  class << self

    private

      def unrename_keys(params)
        case params.class.to_s
          when 'Hash'
            Hash[params.map { |k,v| [k.to_s.tr('-', '_'), unrename_keys(v)] } ]
          when 'Array'
            params.map { |v| unrename_keys(v) }
          else
            params
        end
      end
  end
end
