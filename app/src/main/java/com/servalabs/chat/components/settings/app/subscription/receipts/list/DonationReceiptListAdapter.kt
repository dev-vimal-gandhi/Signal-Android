package com.servalabs.chat.components.settings.app.subscription.receipts.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.servalabs.chat.R
import com.servalabs.chat.components.settings.DSLSettingsText
import com.servalabs.chat.components.settings.SectionHeaderPreference
import com.servalabs.chat.components.settings.SectionHeaderPreferenceViewHolder
import com.servalabs.chat.components.settings.TextPreference
import com.servalabs.chat.components.settings.TextPreferenceViewHolder
import com.servalabs.chat.util.StickyHeaderDecoration
import com.servalabs.chat.util.adapter.mapping.LayoutFactory
import com.servalabs.chat.util.adapter.mapping.MappingAdapter
import com.servalabs.chat.util.toLocalDateTime
import org.signal.core.ui.R as CoreUiR

class DonationReceiptListAdapter(onModelClick: (DonationReceiptListItem.Model) -> Unit) : MappingAdapter(), StickyHeaderDecoration.StickyHeaderAdapter<SectionHeaderPreferenceViewHolder> {

  init {
    registerFactory(TextPreference::class.java, LayoutFactory({ TextPreferenceViewHolder(it) }, R.layout.dsl_preference_item))
    DonationReceiptListItem.register(this, onModelClick)
  }

  override fun getHeaderId(position: Int): Long {
    return when (val item = getItem(position)) {
      is DonationReceiptListItem.Model -> item.record.timestamp.toLocalDateTime().year.toLong()
      else -> StickyHeaderDecoration.StickyHeaderAdapter.NO_HEADER_ID
    }
  }

  override fun onCreateHeaderViewHolder(parent: ViewGroup?, position: Int, type: Int): SectionHeaderPreferenceViewHolder {
    return SectionHeaderPreferenceViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.dsl_section_header, parent, false))
  }

  override fun onBindHeaderViewHolder(viewHolder: SectionHeaderPreferenceViewHolder?, position: Int, type: Int) {
    viewHolder?.itemView?.run {
      val color = ContextCompat.getColor(context, CoreUiR.color.signal_colorBackground)
      setBackgroundColor(color)
    }

    viewHolder?.bind(SectionHeaderPreference(DSLSettingsText.from(getHeaderId(position).toString())))
  }
}
