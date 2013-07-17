class Array
  # *DEPRECATED*: Use +Array#uniq+ instead.
  #
  # Returns a unique array based on the criteria in the block.
  #
  #   [1, 2, 3, 4].uniq_by { |i| i.odd? } # => [1, 2]
  #
  def uniq_by(&block)
    ActiveSupport::Deprecation.warn 'uniq_by is deprecated. Use Array#uniq instead', caller
    uniq(&block)
  end

  # *DEPRECATED*: Use +Array#uniq!+ instead.
  #
  # Same as +uniq_by+, but modifies +self+.
  def uniq_by!(&block)
    ActiveSupport::Deprecation.warn 'uniq_by! is deprecated. Use Array#uniq! instead', caller
    uniq!(&block)
  end
end
