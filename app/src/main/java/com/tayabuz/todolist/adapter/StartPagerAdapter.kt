package com.tayabuz.todolist.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tayabuz.todolist.ListOfItemsFragment
import com.tayabuz.todolist.database.TODOItem

const val TODO_LIST_PAGE_INDEX = 0
const val IN_PROGRESS_LIST_PAGE_INDEX = 1
const val DONE_LIST_PAGE_INDEX = 2
class StartPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        TODO_LIST_PAGE_INDEX to { ListOfItemsFragment(TODOItem.ProgressOfItem.ToDo) },
        IN_PROGRESS_LIST_PAGE_INDEX to { ListOfItemsFragment(TODOItem.ProgressOfItem.InProcess) },
        DONE_LIST_PAGE_INDEX to { ListOfItemsFragment(TODOItem.ProgressOfItem.Done)}
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}