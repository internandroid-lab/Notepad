package com.example.notepad

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.notepad.databinding.ActivityMainBinding
import com.example.notepad.utils.AppUtil
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var currentFragment: String = "Home"
    private var isSearchMode = false

    interface ToolbarController {
        fun onSearchClick()
        fun onSortClick()
        fun onSearchTextChanged(query: String)
        fun updateTitle(title: String)
        fun showSearchField(show: Boolean)
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
        setupNavigationDrawer()
        setupBackPressHandler()

        AppUtil.setupKeyboardHiderForAllViews(binding.root)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    currentFragment = "Home"
                    binding.tvTitle.text = getString(R.string.app_name)
                    showToolbarActions(true)
                    binding.navigationView.setCheckedItem(R.id.nav_notes)
                }

                R.id.categoriesFragment -> {
                    currentFragment = "Categories"
                    binding.tvTitle.text = "Categories"
                    showToolbarActions(false)
                    binding.navigationView.setCheckedItem(R.id.nav_categories)
                }

                R.id.categoryNotesFragment -> {
                    currentFragment = "Category Notes"
                    showToolbarActions(true)
                }

                R.id.editNoteFragment -> {
                    currentFragment = "Edit Note"
                    showToolbarActions(false)
                }
            }
            exitSearchMode()
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
            toolbarController?.onSearchClick() ?: toggleSearchMode()
        }

        binding.ivSort.setOnClickListener {
            toolbarController?.onSortClick() ?: showSortDialog()
        }

        binding.ivAbout.setOnClickListener {
            toolbarController?.onAboutClick()
        }


        binding.etSearch.addTextChangedListener { text ->
            toolbarController?.onSearchTextChanged(text.toString())
        }
    }

    private fun setupNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            val destinationId = when (menuItem.itemId) {
                R.id.nav_notes -> R.id.homeFragment
                R.id.nav_categories -> R.id.categoriesFragment
//                R.id.nav_trash -> R.id.trashFragment
//                R.id.nav_settings -> R.id.settingsFragment
                else -> -1
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
            true
        }
        binding.navigationView.setCheckedItem(R.id.nav_notes)
    }

    private fun showToolbarActions(show: Boolean) {
        binding.ivSearch.visibility = if (show) View.VISIBLE else View.GONE
        binding.ivSort.visibility = if (show) View.VISIBLE else View.GONE
        binding.ivAbout.visibility = if (show) View.VISIBLE else View.GONE
    }


    private fun toggleSearchMode() {
        isSearchMode = !isSearchMode
        if (isSearchMode) {
            binding.etSearch.visibility = View.VISIBLE
            binding.tvTitle.visibility = View.GONE
            binding.etSearch.requestFocus()
            binding.ivSearch.setImageResource(R.drawable.ic_close)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
        } else {
            exitSearchMode()
        }
    }

    private fun exitSearchMode() {
        if (isSearchMode) {
            isSearchMode = false
            binding.etSearch.visibility = View.GONE
            binding.tvTitle.visibility = View.VISIBLE
            binding.etSearch.text.clear()
            binding.ivSearch.setImageResource(R.drawable.ic_search)
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf("Sort by Date", "Sort by Title")
        AlertDialog.Builder(this)
            .setTitle("Sort Notes")
            .setItems(sortOptions) { _, which ->
                Log.d("Sort Dialog","Selected: ${sortOptions[which]}")
            }
            .show()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else if (isSearchMode) {
                exitSearchMode()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }

    fun setToolbarController(controller: ToolbarController?) {
        toolbarController = controller
    }

    fun updateToolbarTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun showSearchField(show: Boolean) {
        if (show && !isSearchMode) {
            toggleSearchMode()
        } else if (!show && isSearchMode) {
            exitSearchMode()
        }
    }

}