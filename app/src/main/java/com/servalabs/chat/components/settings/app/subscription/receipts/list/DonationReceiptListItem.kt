package com.servalabs.chat.components.settings.app.subscription.receipts.list

import android.view.View
import android.widget.TextView
import com.servalabs.chat.R
import com.servalabs.chat.badges.BadgeImageView
import com.servalabs.chat.badges.models.Badge
import com.servalabs.chat.database.model.InAppPaymentReceiptRecord
import com.servalabs.chat.payments.FiatMoneyUtil
import com.servalabs.chat.util.DateUtils
import com.servalabs.chat.util.adapter.mapping.LayoutFactory
import com.servalabs.chat.util.adapter.mapping.MappingAdapter
import com.servalabs.chat.util.adapter.mapping.MappingModel
import com.servalabs.chat.util.adapter.mapping.MappingViewHolder
import java.util.Locale

object DonationReceiptListItem {

  fun register(adapter: MappingAdapter, onClick: (Model) -> Unit) {
    adapter.registerFactory(Model::class.java, LayoutFactory({ ViewHolder(it, onClick) }, R.layout.donation_receipt_list_item))
  }

  class Model(
    val record: InAppPaymentReceiptRecord,
    val badge: Badge?
  ) : MappingModel<Model> {
    override fun areContentsTheSame(newItem: Model): Boolean = record == newItem.record && badge == newItem.badge

    override fun areItemsTheSame(newItem: Model): Boolean = record.id == newItem.record.id
  }

  private class ViewHolder(itemView: View, private val onClick: (Model) -> Unit) : MappingViewHolder<Model>(itemView) {

    private val badgeView: BadgeImageView = itemView.findViewById(R.id.badge)
    private val dateView: TextView = itemView.findViewById(R.id.date)
    private val typeView: TextView = itemView.findViewById(R.id.type)
    private val moneyView: TextView = itemView.findViewById(R.id.money)

    override fun bind(model: Model) {
      itemView.setOnClickListener { onClick(model) }
      badgeView.setBadge(model.badge)
      dateView.text = DateUtils.formatDate(Locale.getDefault(), model.record.timestamp)
      typeView.setText(
        when (model.record.type) {
          InAppPaymentReceiptRecord.Type.RECURRING_DONATION -> R.string.DonationReceiptListFragment__recurring
          InAppPaymentReceiptRecord.Type.ONE_TIME_DONATION -> R.string.DonationReceiptListFragment__one_time
          InAppPaymentReceiptRecord.Type.ONE_TIME_GIFT -> R.string.DonationReceiptListFragment__donation_for_a_friend
          InAppPaymentReceiptRecord.Type.RECURRING_BACKUP -> error("Not supported in this fragment")
        }
      )
      moneyView.text = FiatMoneyUtil.format(context.resources, model.record.amount)
    }
  }
}
