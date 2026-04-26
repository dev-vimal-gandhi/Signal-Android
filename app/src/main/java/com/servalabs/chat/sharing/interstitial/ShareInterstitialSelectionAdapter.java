package com.servalabs.chat.sharing.interstitial;

import com.servalabs.chat.R;
import com.servalabs.chat.util.adapter.mapping.MappingAdapter;
import com.servalabs.chat.util.viewholders.RecipientViewHolder;

class ShareInterstitialSelectionAdapter extends MappingAdapter {
  ShareInterstitialSelectionAdapter() {
    registerFactory(ShareInterstitialMappingModel.class, RecipientViewHolder.createFactory(R.layout.share_contact_selection_item, null));
  }
}
