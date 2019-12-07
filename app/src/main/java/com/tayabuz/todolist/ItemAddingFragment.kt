package com.tayabuz.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.tayabuz.todolist.database.TODOItem
import com.tayabuz.todolist.util.InjectUtil
import com.tayabuz.todolist.viewmodel.TODOItemsViewModel


//TODO: при переходе назад, клавиатура остается в фокусе, нужно исправить
class ItemAddingFragment: Fragment() {
    private val viewModel: TODOItemsViewModel by viewModels {
        InjectUtil.provideTODOItemsViewModelFactory(requireContext(), TODOItem.ProgressOfItem.ToDo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.item_adding_fragment, container, false)
        val buttonCancel : Button = view.findViewById(R.id.add_cancel)
        val buttonOk : Button = view.findViewById(R.id.add_ok)

        buttonCancel.setOnClickListener {
            val navController = findNavController(requireView())
            navController.popBackStack()
        }
        buttonOk.setOnClickListener {
            val header = view.findViewById<TextInputEditText>(R.id.header_text).text.toString()
            val explanation = view.findViewById<TextInputEditText>(R.id.explanation_text).text.toString()
            val todoItem = TODOItem(header, explanation, TODOItem.ProgressOfItem.ToDo)
            viewModel.insert(todoItem)
            val navController = findNavController(requireView())
            navController.popBackStack()
        }
        return view
    }
}