package com.servalabs.chat.stories.my

import androidx.fragment.app.Fragment
import com.servalabs.chat.components.FragmentWrapperActivity

class MyStoriesActivity : FragmentWrapperActivity() {
  override fun getFragment(): Fragment {
    return MyStoriesFragment()
  }
}
