package com.servalabs.chat.components.settings.app.subscription.receipts.list

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.servalabs.chat.R
import com.servalabs.chat.badges.models.Badge
import com.servalabs.chat.components.settings.DSLSettingsText
import com.servalabs.chat.components.settings.TextPreference
import com.servalabs.chat.database.model.InAppPaymentReceiptRecord
import com.servalabs.chat.util.StickyHeaderDecoration
import com.servalabs.chat.util.livedata.LiveDataUtil
import com.servalabs.chat.util.navigation.safeNavigate
import com.servalabs.chat.util.visible
import org.signal.core.ui.R as CoreUiR

class DonationReceiptListPageFragment : Fragment(R.layout.donation_receipt_list_page_fragment) {

  private val viewModel: DonationReceiptListPageViewModel by viewModels(factoryProducer = {
    DonationReceiptListPageViewModel.Factory(type, DonationReceiptListPageRepository())
  })

  private val sharedViewModel: DonationReceiptListViewModel by viewModels(
    ownerProducer = { requireParentFragment() },
    factoryProducer = {
      DonationReceiptListViewModel.Factory(DonationReceiptListRepository())
    }
  )

  private val type: InAppPaymentReceiptRecord.Type?
    get() = requireArguments().getString(ARG_TYPE)?.let { InAppPaymentReceiptRecord.Type.fromCode(it) }

  private lateinit var emptyStateGroup: Group

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val adapter = DonationReceiptListAdapter { model ->
      findNavController().safeNavigate(DonationReceiptListFragmentDirections.actionDonationReceiptListFragmentToDonationReceiptDetailFragment(model.record.id))
    }

    view.findViewById<RecyclerView>(R.id.recycler).apply {
      this.adapter = adapter
      addItemDecoration(StickyHeaderDecoration(adapter, false, true, 0))
    }

    emptyStateGroup = view.findViewById(R.id.empty_state)

    LiveDataUtil.combineLatest(
      viewModel.state,
      sharedViewModel.state
    ) { state, badges ->
      state.isLoaded to state.records.map { DonationReceiptListItem.Model(it, getBadgeForRecord(it, badges)) }
    }.observe(viewLifecycleOwner) { (isLoaded, records) ->
      if (records.isNotEmpty()) {
        emptyStateGroup.visible = false
        adapter.submitList(
          records +
            TextPreference(
              title = null,
              summary = DSLSettingsText.from(
                R.string.DonationReceiptListFragment__if_you_have,
                DSLSettingsText.TextAppearanceModifier(CoreUiR.style.TextAppearance_Signal_Subtitle)
              )
            )
        )
      } else {
        emptyStateGroup.visible = isLoaded
      }
    }
  }

  private fun getBadgeForRecord(record: InAppPaymentReceiptRecord, badges: List<DonationReceiptBadge>): Badge? {
    return when (record.type) {
      InAppPaymentReceiptRecord.Type.ONE_TIME_DONATION -> badges.firstOrNull { it.type == InAppPaymentReceiptRecord.Type.ONE_TIME_DONATION }?.badge
      InAppPaymentReceiptRecord.Type.ONE_TIME_GIFT -> badges.firstOrNull { it.type == InAppPaymentReceiptRecord.Type.ONE_TIME_GIFT }?.badge
      else -> badges.firstOrNull { it.level == record.subscriptionLevel }?.badge
    }
  }

  companion object {

    private const val ARG_TYPE = "arg_type"

    fun create(type: InAppPaymentReceiptRecord.Type?): Fragment {
      return DonationReceiptListPageFragment().apply {
        arguments = Bundle().apply {
          putString(ARG_TYPE, type?.code)
        }
      }
    }
  }
}
