require 'active_support/time'
require 'active_support/core_ext/array/wrap'
require 'active_support/core_ext/hash/reverse_merge'
require 'active_support/core_ext/object/blank'
require 'active_support/core_ext/string/inflections'

class Hash
  class << self

    private
      def typecast_xml_value(value)
        case value.class.to_s
          when 'Hash'
            if value['type'] == 'array'
              _, entries = Array.wrap(value.detect { |k,v| not v.is_a?(String) })
              if entries.nil? || (c = value['__content__'] && c.blank?)
                []
              else
                case entries.class.to_s   # something weird with classes not matching here.  maybe singleton methods breaking is_a?
                when 'Array'
                  entries.collect { |v| typecast_xml_value(v) }
                when 'Hash'
                  [typecast_xml_value(entries)]
                else
                  raise "can't typecast #{entries.inspect}"
                end
              end
            elsif value['type'] == 'file' ||
               (value['__content__'] && (value.keys.size == 1 || value['__content__'].present?))
              content = value['__content__']
              if parser = ActiveSupport::XmlMini::PARSING[value['type']]
                parser.arity == 1 ? parser.call(content) : parser.call(content, value)
              else
                content
              end
            elsif value['type'] == 'string' && value['nil'] != 'true'
              ''
            # blank or nil parsed values are represented by nil
            elsif value.blank? || value['nil'] == 'true'
              nil
            # If the type is the only element which makes it then
            # this still makes the value nil, except if type is
            # a XML node(where type['value'] is a Hash)
            elsif value['type'] && value.size == 1 && !value['type'].is_a?(::Hash)
              nil
            else
              xml_value = Hash[value.map { |k,v| [k, typecast_xml_value(v)] }]

              # Turn { :files => { :file => #<StringIO> } } into { :files => #<StringIO> } so it is compatible with
              # how multipart uploaded files from HTML appear
              xml_value['file'].is_a?(StringIO) ? xml_value['file'] : xml_value
            end
          when 'Array'
            value.map! { |i| typecast_xml_value(i) }
            value.length > 1 ? value : value.first
          when 'String'
            value
          else
            raise "can't typecast #{value.class.name} - #{value.inspect}"
        end
      end

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
