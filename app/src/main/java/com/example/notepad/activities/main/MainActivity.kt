package com.example.notepad.activities.main

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.notepad.R
import com.example.notepad.databinding.ActivityMainBinding
import com.example.notepad.db.entity.Category
import com.example.notepad.utils.AppUtil
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val viewModel: MainViewModel by viewModel()

    interface ToolbarController {
        fun onSortClick()
        fun onSearchTextChanged(query: String)
        fun onAboutClick()
    }

    private var toolbarController: ToolbarController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupNavigation()
        setupToolbar()
        setupObservers()
        setupNavigationDrawer()
        setupBackPressHandler()

        AppUtil.setupKeyboardHiderForAllViews(binding.root)
    }

    fun setToolbarController(controller: ToolbarController?) {
        toolbarController = controller
    }

    fun toggleSearchMode() {
        viewModel.toggleSearchMode()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categories.collect { categories ->
                        updateCategoriesInDrawer(categories)
                    }
                }
                launch {
                    viewModel.isSearchMode.collect { isSearchMode ->
                        if (isSearchMode) {
                            binding.etSearch.visibility = View.VISIBLE
                            binding.tvTitle.visibility = View.GONE
                            binding.etSearch.requestFocus()
                            binding.ivSearch.setImageResource(R.drawable.ic_close)
                            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
                        } else {
                            binding.etSearch.visibility = View.GONE
                            binding.tvTitle.visibility = View.VISIBLE
                            binding.etSearch.text.clear()
                            binding.ivSearch.setImageResource(R.drawable.ic_search)
                        }
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.ivMenu.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        binding.ivSearch.setOnClickListener {
            toggleSearchMode()
        }

        binding.ivSort.setOnClickListener {
            toolbarController?.onSortClick()
        }

        binding.ivAbout.setOnClickListener {
            toolbarController?.onAboutClick()
        }

        binding.etSearch.addTextChangedListener { text ->
            toolbarController?.onSearchTextChanged(text.toString())
        }
    }

    private fun setupNavigation() {
        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            when (destination.id) {
                R.id.homeFragment -> {
                    binding.tvTitle.text = getString(R.string.app_name)
                    showToolbarActions(true)
                    binding.navigationView.setCheckedItem(R.id.nav_notes)
                }

                R.id.categoriesFragment -> {
                    binding.tvTitle.text = getString(R.string.categories)
                    showToolbarActions(false)
                    binding.navigationView.setCheckedItem(R.id.nav_categories)
                }

                R.id.categoryNotesFragment -> {
                    showToolbarActions(true)
                    val categoryId = arguments?.getLong("categoryId") ?: -1L
                    val category = viewModel.categories.value.find { it.categoryId == categoryId }
                    binding.tvTitle.text = "Notepad\n${category?.name}"
                    showToolbarActions(true)
                }

                R.id.editNoteFragment -> {
                    showToolbarActions(false)
                }

                R.id.trashFragment -> {
                    binding.tvTitle.text = getString(R.string.trash)
                    showToolbarActions(false)
                    binding.navigationView.setCheckedItem(R.id.nav_trash)
                }
            }
        }
    }

    private fun setupNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            val destinationId = when (menuItem.itemId) {
                R.id.nav_notes -> R.id.homeFragment
                R.id.nav_categories -> R.id.categoriesFragment
                R.id.nav_trash -> R.id.trashFragment
                else -> {
                    val bundle = bundleOf("categoryId" to menuItem.itemId.toLong())
                    navController.popBackStack(R.id.categoryNotesFragment, true)
                    navController.navigate(
                        R.id.categoryNotesFragment,
                        bundle
                    )
                    -1
                }
            }

            if (destinationId != -1) {
                if (navController.currentDestination?.id != destinationId) {
                    navController.popBackStack(R.id.homeFragment, false)
                    if (destinationId != R.id.homeFragment) {
                        navController.navigate(destinationId)
                    }
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            viewModel.exitSearchMode()
            true
        }
        binding.navigationView.setCheckedItem(R.id.nav_notes)
    }

    private fun updateCategoriesInDrawer(categories: List<Category>) {
        val menu = binding.navigationView.menu

        menu.removeGroup(R.id.group_categories)
        menu.removeGroup(R.id.group_trash)

        menu.add(
            R.id.group_categories,
            R.id.nav_categories,
            Menu.NONE,
            getString(R.string.categories)
        ).setIcon(R.drawable.ic_category)
            .isCheckable = true

        categories.forEach { category ->
            menu.add(R.id.group_categories, category.categoryId.toInt(), Menu.NONE, category.name)
                .isCheckable = true
        }
        menu.add(
            R.id.group_trash,
            R.id.nav_trash,
            Menu.NONE,
            getString(R.string.trash)
        ).setIcon(R.drawable.ic_trash)
            .isCheckable = true
    }


    private fun showToolbarActions(show: Boolean) {
        binding.ivSearch.visibility = if (show) View.VISIBLE else View.GONE
        binding.ivSort.visibility = if (show) View.VISIBLE else View.GONE
        binding.ivAbout.visibility = if (show) View.VISIBLE else View.GONE
    }


    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else if (viewModel.isSearchMode.value) {
                viewModel.toggleSearchMode()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }

}