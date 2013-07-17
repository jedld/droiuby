require 'active_support'

module ActiveSupport
  autoload :Duration, 'active_support/duration'
  autoload :TimeWithZone, 'active_support/time_with_zone'
  autoload :TimeZone, 'active_support/values/time_zone'
end

require 'date'
require 'time'

require 'active_support/core_ext/time/marshal'
require 'active_support/core_ext/time/acts_like'
require 'active_support/core_ext/time/calculations'
require 'active_support/core_ext/time/conversions'
require 'active_support/core_ext/time/zones'

require 'active_support/core_ext/date/acts_like'
require 'active_support/core_ext/date/calculations'
require 'active_support/core_ext/date/conversions'
require 'active_support/core_ext/date/zones'

require 'active_support/core_ext/date_time/acts_like'
require 'active_support/core_ext/date_time/calculations'
require 'active_support/core_ext/date_time/conversions'
require 'active_support/core_ext/date_time/zones'

require 'active_support/core_ext/integer/time'
require 'active_support/core_ext/numeric/time'
