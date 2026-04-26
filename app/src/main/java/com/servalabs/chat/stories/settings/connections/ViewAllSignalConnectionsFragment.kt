package com.servalabs.chat.stories.settings.connections

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.servalabs.chat.R
import com.servalabs.chat.components.ViewBinderDelegate
import com.servalabs.chat.components.WrapperDialogFragment
import com.servalabs.chat.contacts.LetterHeaderDecoration
import com.servalabs.chat.contacts.paged.ContactSearchAdapter
import com.servalabs.chat.contacts.paged.ContactSearchConfiguration
import com.servalabs.chat.contacts.paged.ContactSearchPagedDataSourceRepository
import com.servalabs.chat.contacts.paged.ContactSearchRepository
import com.servalabs.chat.contacts.paged.ContactSearchViewModel
import com.servalabs.chat.database.RecipientTable
import com.servalabs.chat.databinding.ViewAllSignalConnectionsFragmentBinding
import com.servalabs.chat.groups.SelectionLimits
import com.servalabs.chat.search.SearchRepository

class ViewAllSignalConnectionsFragment : Fragment(R.layout.view_all_signal_connections_fragment) {

  private val binding by ViewBinderDelegate(ViewAllSignalConnectionsFragmentBinding::bind)

  private val contactSearchViewModel: ContactSearchViewModel by viewModels {
    ContactSearchViewModel.Factory(
      selectionLimits = SelectionLimits(0, 0),
      isMultiSelect = false,
      repository = ContactSearchRepository(),
      performSafetyNumberChecks = false,
      arbitraryRepository = null,
      searchRepository = SearchRepository(requireContext().getString(R.string.note_to_self)),
      contactSearchPagedDataSourceRepository = ContactSearchPagedDataSourceRepository(requireContext())
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.toolbar.setNavigationOnClickListener {
      requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    binding.recycler.bind(
      viewModel = contactSearchViewModel,
      fragmentManager = childFragmentManager,
      displayOptions = ContactSearchAdapter.DisplayOptions(
        displayCheckBox = false,
        displaySecondaryInformation = ContactSearchAdapter.DisplaySecondaryInformation.NEVER
      ),
      mapStateToConfiguration = { getConfiguration() },
      itemDecorations = listOf(LetterHeaderDecoration(requireContext()) { false })
    )
  }

  private fun getConfiguration(): ContactSearchConfiguration {
    return ContactSearchConfiguration.build {
      addSection(
        ContactSearchConfiguration.Section.Individuals(
          includeHeader = false,
          includeSelfMode = RecipientTable.IncludeSelfMode.Exclude,
          includeLetterHeaders = true,
          transportType = ContactSearchConfiguration.TransportType.PUSH
        )
      )
    }
  }

  class Dialog : WrapperDialogFragment() {
    override fun getWrappedFragment(): Fragment {
      return ViewAllSignalConnectionsFragment()
    }

    companion object {
      fun show(fragmentManager: FragmentManager) {
        Dialog().show(fragmentManager, null)
      }
    }
  }
}
