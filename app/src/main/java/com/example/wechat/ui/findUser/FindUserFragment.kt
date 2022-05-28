package com.example.wechat.ui.findUser

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.wechat.R
import com.example.wechat.Util.CLICKED_USER
import com.example.wechat.databinding.FindUserFragmentBinding
import com.google.gson.Gson

class FindUserFragment : Fragment(){
    private lateinit var adapter: UsersAdapter
    private lateinit var binding: FindUserFragmentBinding

    companion object {
        fun newInstance() = FindUserFragment()
    }
    private lateinit var viewModel: FindUserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        activity?.title = "Search for friends"
        binding = DataBindingUtil.inflate(inflater, R.layout.find_user_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[FindUserViewModel::class.java]

        viewModel.loadUsers().observe(this, { usersList ->
            binding.loadingImage.visibility = View.GONE
            if (usersList.isNullOrEmpty()) {
                binding.noUsersLayout.visibility = View.VISIBLE
            } else {
                adapter.submitList(usersList)
                adapter.userList = usersList
            }
        })

        adapter = UsersAdapter(UserClickListener { clickedUser ->
            val gson = Gson()
            val clickedUser = gson.toJson(clickedUser)

            val bundle = bundleOf(
                CLICKED_USER to clickedUser
            )
            findNavController().navigate(
                R.id.action_findUserFragment_to_differentUserProfile,
                bundle
            )
        })
        binding.recycler.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

    }
}