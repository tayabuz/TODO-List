package com.tayabuz.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.tayabuz.todolist.adapter.DONE_LIST_PAGE_INDEX
import com.tayabuz.todolist.adapter.IN_PROGRESS_LIST_PAGE_INDEX
import com.tayabuz.todolist.adapter.StartPagerAdapter
import com.tayabuz.todolist.adapter.TODO_LIST_PAGE_INDEX
import com.tayabuz.todolist.databinding.StartFragmentBinding

class StartFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = StartFragmentBinding.inflate(inflater, container, false)
        val tabs = binding.tabs
        val viewPager = binding.viewpager
        viewPager.adapter = StartPagerAdapter(this)

        TabLayoutMediator(tabs, viewPager)
        { tab, position -> tab.text = getTabTitle(position) }.attach()

        return binding.root
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            TODO_LIST_PAGE_INDEX -> getString(R.string.tab_header_todo)
            IN_PROGRESS_LIST_PAGE_INDEX -> getString(R.string.tab_header_inprocess)
            DONE_LIST_PAGE_INDEX -> getString(R.string.tab_header_done)
            else -> null
        }
    }
}