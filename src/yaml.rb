##
# The YAML module allows you to use one of the two YAML engines that ship with
# ruby.  By default Psych is used but the old and unmaintained Syck may be
# chosen.
#
# See Psych or Syck for usage and documentation.
#
# To set the YAML engine to syck:
#
#   YAML::ENGINE.yamler = 'syck'
#
# To set the YAML engine back to psych:
#
#   YAML::ENGINE.yamler = 'psych'
module YAML
  class EngineManager # :nodoc:
    attr_reader :yamler

    def initialize
      @yamler = nil
    end

    def syck?
      'syck' == @yamler
    end

    def yamler= engine
       engine
    end
  end

  ##
  # Allows changing the current YAML engine.  See YAML for details.

  ENGINE = YAML::EngineManager.new
end

module RbYAML
  ENGINE = YAML::ENGINE
end

YAML::ENGINE.yamler = nil
