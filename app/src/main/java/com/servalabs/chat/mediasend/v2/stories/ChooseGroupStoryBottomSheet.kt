package com.servalabs.chat.mediasend.v2.stories

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.signal.core.ui.FixedRoundedCornerBottomSheetDialogFragment
import org.signal.core.util.DimensionUnit
import org.signal.core.util.getParcelableArrayListCompat
import com.servalabs.chat.R
import com.servalabs.chat.contacts.paged.ContactSearchAdapter
import com.servalabs.chat.contacts.paged.ContactSearchConfiguration
import com.servalabs.chat.contacts.paged.ContactSearchKey
import com.servalabs.chat.contacts.paged.ContactSearchPagedDataSourceRepository
import com.servalabs.chat.contacts.paged.ContactSearchRepository
import com.servalabs.chat.contacts.paged.ContactSearchSortOrder
import com.servalabs.chat.contacts.paged.ContactSearchView
import com.servalabs.chat.contacts.paged.ContactSearchViewModel
import com.servalabs.chat.recipients.RecipientId
import com.servalabs.chat.search.SearchRepository
import com.servalabs.chat.sharing.ShareContact
import com.servalabs.chat.sharing.ShareSelectionAdapter
import com.servalabs.chat.sharing.ShareSelectionMappingModel
import com.servalabs.chat.util.RemoteConfig

class ChooseGroupStoryBottomSheet : FixedRoundedCornerBottomSheetDialogFragment() {

  companion object {
    const val GROUP_STORY = "group-story"
    const val RESULT_SET = "groups"
  }

  private lateinit var divider: View
  private lateinit var contactSearch: ContactSearchView
  private lateinit var innerContainer: View

  private var animatorSet: AnimatorSet? = null

  private val contactSearchViewModel: ContactSearchViewModel by viewModels {
    ContactSearchViewModel.Factory(
      selectionLimits = RemoteConfig.shareSelectionLimit,
      isMultiSelect = true,
      repository = ContactSearchRepository(),
      performSafetyNumberChecks = false,
      arbitraryRepository = null,
      searchRepository = SearchRepository(requireContext().getString(R.string.note_to_self)),
      contactSearchPagedDataSourceRepository = ContactSearchPagedDataSourceRepository(requireContext())
    )
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.cloneInContext(ContextThemeWrapper(inflater.context, themeResId)).inflate(R.layout.stories_choose_group_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    view.minimumHeight = resources.displayMetrics.heightPixels

    val container = view.parent.parent.parent as FrameLayout
    val bottomBar = LayoutInflater.from(requireContext()).inflate(R.layout.stories_choose_group_bottom_bar, container, true)

    innerContainer = bottomBar.findViewById(R.id.inner_container)
    divider = bottomBar.findViewById(R.id.divider)

    val adapter = ShareSelectionAdapter()
    val selectedList: RecyclerView = bottomBar.findViewById(R.id.selected_list)
    selectedList.adapter = adapter

    val confirmButton: View = bottomBar.findViewById(R.id.share_confirm)
    confirmButton.setOnClickListener {
      onDone()
    }

    contactSearch = view.findViewById(R.id.contact_recycler)
    contactSearch.bind(
      viewModel = contactSearchViewModel,
      fragmentManager = childFragmentManager,
      displayOptions = ContactSearchAdapter.DisplayOptions(
        displayCheckBox = true,
        displaySecondaryInformation = ContactSearchAdapter.DisplaySecondaryInformation.NEVER
      ),
      mapStateToConfiguration = { state ->
        ContactSearchConfiguration.build {
          query = state.query

          addSection(
            ContactSearchConfiguration.Section.Groups(
              includeHeader = false,
              shortSummary = true,
              sortOrder = ContactSearchSortOrder.RECENCY
            )
          )
        }
      },
      contentBottomPaddingDp = 44f
    )

    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        contactSearchViewModel.selectionState.collect { state ->
          adapter.submitList(
            state.filterIsInstance(ContactSearchKey.RecipientSearchKey::class.java)
              .map { it.recipientId }
              .mapIndexed { index, recipientId ->
                ShareSelectionMappingModel(
                  ShareContact(recipientId),
                  index == 0
                )
              }
          )

          if (state.isEmpty()) {
            animateOutBottomBar()
          } else {
            animateInBottomBar()
          }
        }
      }
    }

    val searchField: EditText = view.findViewById(R.id.search_field)
    searchField.doAfterTextChanged {
      contactSearchViewModel.setQuery(it?.toString())
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    animatorSet?.cancel()
  }

  private fun animateInBottomBar() {
    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(innerContainer, View.TRANSLATION_Y, 0f),
        ObjectAnimator.ofFloat(divider, View.TRANSLATION_Y, 0f)
      )
      start()
    }
  }

  private fun animateOutBottomBar() {
    val translationY = DimensionUnit.SP.toPixels(68f)

    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(innerContainer, View.TRANSLATION_Y, translationY),
        ObjectAnimator.ofFloat(divider, View.TRANSLATION_Y, translationY)
      )
      start()
    }
  }

  private fun onDone() {
    setFragmentResult(
      GROUP_STORY,
      Bundle().apply {
        putParcelableArrayList(
          RESULT_SET,
          ArrayList(
            contactSearchViewModel.getSelectedContacts()
              .filterIsInstance(ContactSearchKey.RecipientSearchKey::class.java)
              .map { it.recipientId }
          )
        )
      }
    )
    dismissAllowingStateLoss()
  }

  object ResultContract {
    fun getRecipientIds(bundle: Bundle): List<RecipientId> {
      return bundle.getParcelableArrayListCompat(RESULT_SET, RecipientId::class.java)!!
    }
  }
}
