package com.servalabs.chat.components.settings.app.subscription.donate.gateway

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.util.concurrent.LifecycleDisposable
import org.signal.core.util.dp
import org.signal.donations.InAppPaymentType
import com.servalabs.chat.R
import com.servalabs.chat.badges.Badges
import com.servalabs.chat.badges.models.BadgeDisplay112
import com.servalabs.chat.components.settings.DSLConfiguration
import com.servalabs.chat.components.settings.DSLSettingsAdapter
import com.servalabs.chat.components.settings.DSLSettingsBottomSheetFragment
import com.servalabs.chat.components.settings.DSLSettingsIcon
import com.servalabs.chat.components.settings.DSLSettingsText
import com.servalabs.chat.components.settings.app.subscription.DonationSerializationHelper.toFiatMoney
import com.servalabs.chat.components.settings.app.subscription.GooglePayComponent
import com.servalabs.chat.components.settings.app.subscription.models.GooglePayButton
import com.servalabs.chat.components.settings.app.subscription.models.IdealWeroButton
import com.servalabs.chat.components.settings.app.subscription.models.PayPalButton
import com.servalabs.chat.components.settings.configure
import com.servalabs.chat.components.settings.models.IndeterminateLoadingCircle
import com.servalabs.chat.database.InAppPaymentTable
import com.servalabs.chat.database.model.databaseprotos.InAppPaymentData
import com.servalabs.chat.payments.FiatMoneyUtil
import com.servalabs.chat.payments.currency.CurrencyUtil
import com.servalabs.chat.util.fragments.requireListener
import com.servalabs.chat.util.viewModel
import org.signal.core.ui.R as CoreUiR

/**
 * Entry point to capturing the necessary payment token to pay for a donation
 */
class GatewaySelectorBottomSheet : DSLSettingsBottomSheetFragment() {

  private val lifecycleDisposable = LifecycleDisposable()

  private val args: GatewaySelectorBottomSheetArgs by navArgs()

  private val viewModel: GatewaySelectorViewModel by viewModel {
    GatewaySelectorViewModel(args, requireListener<GooglePayComponent>().googlePayRepository)
  }

  override fun bindAdapter(adapter: DSLSettingsAdapter) {
    BadgeDisplay112.register(adapter)
    GooglePayButton.register(adapter)
    PayPalButton.register(adapter)
    IndeterminateLoadingCircle.register(adapter)
    IdealWeroButton.register(adapter)

    lifecycleDisposable.bindTo(viewLifecycleOwner)

    lifecycleDisposable += viewModel.state.subscribe { state ->
      adapter.submitList(getConfiguration(state).toMappingModelList())
    }
  }

  private fun getConfiguration(state: GatewaySelectorState): DSLConfiguration {
    return when (state) {
      GatewaySelectorState.Loading -> {
        configure {
          space(16.dp)
          customPref(IndeterminateLoadingCircle)
          space(16.dp)
        }
      }
      is GatewaySelectorState.Ready -> {
        configure {
          customPref(
            BadgeDisplay112.Model(
              badge = state.inAppPayment.data.badge!!.let { Badges.fromDatabaseBadge(it) },
              withDisplayText = false
            )
          )

          space(12.dp)

          presentTitleAndSubtitle(requireContext(), state.inAppPayment)

          space(16.dp)

          state.gatewayOrderStrategy.orderedGateways.forEach { gateway ->
            when (gateway) {
              InAppPaymentData.PaymentMethodType.GOOGLE_PLAY_BILLING -> error("Unsupported payment method.")
              InAppPaymentData.PaymentMethodType.GOOGLE_PAY -> renderGooglePayButton(state)
              InAppPaymentData.PaymentMethodType.PAYPAL -> renderPayPalButton(state)
              InAppPaymentData.PaymentMethodType.CARD -> renderCreditCardButton(state)
              InAppPaymentData.PaymentMethodType.SEPA_DEBIT -> renderSEPADebitButton(state)
              InAppPaymentData.PaymentMethodType.IDEAL -> renderIDEALButton(state)
              InAppPaymentData.PaymentMethodType.UNKNOWN -> error("Unsupported payment method.")
            }
          }

          space(16.dp)
        }
      }
    }
  }

  private fun DSLConfiguration.renderGooglePayButton(state: GatewaySelectorState.Ready) {
    if (state.isGooglePayAvailable) {
      space(16.dp)

      customPref(
        GooglePayButton.Model(
          isEnabled = true,
          onClick = {
            lifecycleDisposable += viewModel.updateInAppPaymentMethod(InAppPaymentData.PaymentMethodType.GOOGLE_PAY)
              .subscribeBy {
                findNavController().popBackStack()
                setFragmentResult(REQUEST_KEY, bundleOf(REQUEST_KEY to it))
              }
          }
        )
      )
    }
  }

  private fun DSLConfiguration.renderPayPalButton(state: GatewaySelectorState.Ready) {
    if (state.isPayPalAvailable) {
      space(16.dp)

      customPref(
        PayPalButton.Model(
          onClick = {
            lifecycleDisposable += viewModel.updateInAppPaymentMethod(InAppPaymentData.PaymentMethodType.PAYPAL)
              .subscribeBy {
                findNavController().popBackStack()
                setFragmentResult(REQUEST_KEY, bundleOf(REQUEST_KEY to it))
              }
          },
          isEnabled = true
        )
      )
    }
  }

  private fun DSLConfiguration.renderCreditCardButton(state: GatewaySelectorState.Ready) {
    if (state.isCreditCardAvailable) {
      space(16.dp)

      primaryButton(
        text = DSLSettingsText.from(R.string.GatewaySelectorBottomSheet__credit_or_debit_card),
        icon = DSLSettingsIcon.from(R.drawable.credit_card, CoreUiR.color.signal_colorOnCustom),
        disableOnClick = true,
        onClick = {
          lifecycleDisposable += viewModel.updateInAppPaymentMethod(InAppPaymentData.PaymentMethodType.CARD)
            .subscribeBy {
              findNavController().popBackStack()
              setFragmentResult(REQUEST_KEY, bundleOf(REQUEST_KEY to it))
            }
        }
      )
    }
  }

  private fun DSLConfiguration.renderSEPADebitButton(state: GatewaySelectorState.Ready) {
    if (state.isSEPADebitAvailable) {
      space(16.dp)

      tonalButton(
        text = DSLSettingsText.from(R.string.GatewaySelectorBottomSheet__bank_transfer),
        icon = DSLSettingsIcon.from(R.drawable.bank_transfer),
        disableOnClick = true,
        onClick = {
          val price = state.inAppPayment.data.amount!!.toFiatMoney()
          if (state.sepaEuroMaximum != null &&
            price.currency == CurrencyUtil.EURO &&
            price.amount > state.sepaEuroMaximum.amount
          ) {
            findNavController().popBackStack()
            setFragmentResult(REQUEST_KEY, bundleOf(FAILURE_KEY to true, SEPA_EURO_MAX to state.sepaEuroMaximum.amount))
          } else {
            lifecycleDisposable += viewModel.updateInAppPaymentMethod(InAppPaymentData.PaymentMethodType.SEPA_DEBIT)
              .subscribeBy {
                findNavController().popBackStack()
                setFragmentResult(REQUEST_KEY, bundleOf(REQUEST_KEY to it))
              }
          }
        }
      )
    }
  }

  private fun DSLConfiguration.renderIDEALButton(state: GatewaySelectorState.Ready) {
    if (state.isIDEALAvailable) {
      space(16.dp)

      customPref(
        IdealWeroButton.Model(
          onClick = {
            lifecycleDisposable += viewModel.updateInAppPaymentMethod(InAppPaymentData.PaymentMethodType.IDEAL)
              .subscribeBy {
                findNavController().popBackStack()
                setFragmentResult(REQUEST_KEY, bundleOf(REQUEST_KEY to it))
              }
          }
        )
      )
    }
  }

  companion object {
    const val REQUEST_KEY = "payment_checkout_mode"
    const val FAILURE_KEY = "gateway_failure"
    const val SEPA_EURO_MAX = "sepa_euro_max"

    fun DSLConfiguration.presentTitleAndSubtitle(context: Context, inAppPayment: InAppPaymentTable.InAppPayment) {
      when (inAppPayment.type) {
        InAppPaymentType.UNKNOWN -> error("Unsupported type UNKNOWN")
        InAppPaymentType.RECURRING_BACKUP -> error("This type is not supported")
        InAppPaymentType.RECURRING_DONATION -> presentMonthlyText(context, inAppPayment)
        InAppPaymentType.ONE_TIME_DONATION -> presentOneTimeText(context, inAppPayment)
        InAppPaymentType.ONE_TIME_GIFT -> presentGiftText(context, inAppPayment)
      }
    }

    private fun DSLConfiguration.presentMonthlyText(context: Context, inAppPayment: InAppPaymentTable.InAppPayment) {
      noPadTextPref(
        title = DSLSettingsText.from(
          context.getString(R.string.GatewaySelectorBottomSheet__donate_s_month_to_signal, FiatMoneyUtil.format(context.resources, inAppPayment.data.amount!!.toFiatMoney())),
          DSLSettingsText.CenterModifier,
          DSLSettingsText.TitleLargeModifier
        )
      )
      space(6.dp)
      noPadTextPref(
        title = DSLSettingsText.from(
          context.getString(R.string.GatewaySelectorBottomSheet__get_a_s_badge, inAppPayment.data.badge!!.name),
          DSLSettingsText.CenterModifier,
          DSLSettingsText.BodyLargeModifier,
          DSLSettingsText.ColorModifier(ContextCompat.getColor(context, CoreUiR.color.signal_colorOnSurfaceVariant))
        )
      )
    }

    private fun DSLConfiguration.presentOneTimeText(context: Context, inAppPayment: InAppPaymentTable.InAppPayment) {
      noPadTextPref(
        title = DSLSettingsText.from(
          context.getString(R.string.GatewaySelectorBottomSheet__donate_s_to_signal, FiatMoneyUtil.format(context.resources, inAppPayment.data.amount!!.toFiatMoney())),
          DSLSettingsText.CenterModifier,
          DSLSettingsText.TitleLargeModifier
        )
      )
      space(6.dp)
      noPadTextPref(
        title = DSLSettingsText.from(
          context.resources.getQuantityString(R.plurals.GatewaySelectorBottomSheet__get_a_s_badge_for_d_days, 30, inAppPayment.data.badge!!.name, 30),
          DSLSettingsText.CenterModifier,
          DSLSettingsText.BodyLargeModifier,
          DSLSettingsText.ColorModifier(ContextCompat.getColor(context, CoreUiR.color.signal_colorOnSurfaceVariant))
        )
      )
    }

    private fun DSLConfiguration.presentGiftText(context: Context, inAppPayment: InAppPaymentTable.InAppPayment) {
      noPadTextPref(
        title = DSLSettingsText.from(
          context.getString(R.string.GatewaySelectorBottomSheet__donate_s_to_signal, FiatMoneyUtil.format(context.resources, inAppPayment.data.amount!!.toFiatMoney())),
          DSLSettingsText.CenterModifier,
          DSLSettingsText.TitleLargeModifier
        )
      )
      space(6.dp)
      noPadTextPref(
        title = DSLSettingsText.from(
          R.string.GatewaySelectorBottomSheet__donate_for_a_friend,
          DSLSettingsText.CenterModifier,
          DSLSettingsText.BodyLargeModifier,
          DSLSettingsText.ColorModifier(ContextCompat.getColor(context, CoreUiR.color.signal_colorOnSurfaceVariant))
        )
      )
    }
  }
}
