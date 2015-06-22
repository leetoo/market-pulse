package actors

object Common {
  type UserId = String
  type Currency = String
  type Exchange = (Currency, Currency)
  type Amount = BigDecimal
  type Rate = BigDecimal
  type DateTime = String //TODO: Replace with something more relevant (e.g. JDK8 DateTime)
  type Country = String
}
