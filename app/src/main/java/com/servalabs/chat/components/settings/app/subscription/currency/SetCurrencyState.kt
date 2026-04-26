package com.servalabs.chat.components.settings.app.subscription.currency

import java.util.Currency

data class SetCurrencyState(
  val selectedCurrencyCode: String = "",
  val currencies: List<Currency> = listOf()
)
