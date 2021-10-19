package com.webgeoservices.sanginfo.sampleapp.views

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.webgeoservices.sanginfo.sampleapp.adapters.SearchAdapter
import com.webgeoservices.sanginfo.sampleapp.listeners.RecyclerItemClickListener
import com.webgeoservices.sanginfo.sampleapp.utils.Util
import com.webgeoservices.sanginfo.sampleapp.viewModels.AutocompleteViewModel
import com.webgeoservices.multisearch.searchdatamodels.AutocompleteResponseItem
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem
import com.webgeoservices.sanginfo.sampleapp.R
import fr.castorflex.android.circularprogressbar.CircularProgressBar


class HomeActivity : AppCompatActivity(), RecyclerItemClickListener {
    private val TAG = "HomeActivity"
    private lateinit var autoCompleteViewModel: AutocompleteViewModel
    private lateinit var recycler: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var editText: EditText
    private lateinit var editTextInputLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initializeSupportBar()
        editTextNormalDrawable()
        autoCompleteViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(AutocompleteViewModel::class.java)
        autoCompleteViewModel.initializeProviders()
        initializeActivityControl()
        showBottomHintText()
        hideSearchError()
        hideRecyclerView()
        hideApiInfo()

    }

    private fun initializeSupportBar() {
        val back: ImageView
        val header: TextView
        try {
            supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            supportActionBar!!.setDisplayShowCustomEnabled(true)
            supportActionBar!!.setCustomView(R.layout.custom_action_bar)
            val parent = supportActionBar?.customView?.parent as Toolbar
            parent.setPadding(0, 0, 0, 0)//for tab otherwise give space in tab
            parent.setContentInsetsAbsolute(0, 0)
            val view = supportActionBar!!.customView
            back =view.findViewById(R.id.back)
            back.visibility = View.GONE
            header = view.findViewById(R.id.header_text)
            header.text = getString(R.string.app_name)
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    private fun initializeActivityControl() {
        try {
            layoutManager = LinearLayoutManager(applicationContext)
            recycler = findViewById(R.id.recycler)
            recycler.layoutManager = layoutManager
            recycler.itemAnimator = DefaultItemAnimator()
            val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
            itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.recyler_divider)!!)
            recycler.addItemDecoration(itemDecorator)
            autoCompleteViewModel.autocompleteResult.observe(this, {
                hideSearchLoader()
                if (this::editText.isInitialized && editText.text.trim().isBlank()) {
                    hideRecyclerView()
                    hideSearchError()
                    hideApiInfo()
                    showBottomHintText()
                    return@observe
                }
                if (it.size > 0) {
                    hideBottomHintText()
                    showRecyclerView()
                    hideSearchError()
                    showApiInfo(it[0].api.toString())
                    populateSearchList(it)
                    editTextSuccessApiDrawable()
                } else {
                    hideBottomHintText()
                    showSearchError()
                    editTextErrorApiDrawable()
                    hideApiInfo()
                    hideRecyclerView()
                }
            })
            autoCompleteViewModel.detailsResult.observe(this, {
                moveToMapActivity(it)
            })
            editText = findViewById(R.id.edit_text)
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    coount: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if (s != null) {
                        if (s.toString().isNotEmpty()) {
                            if (Util.isNetworkAvailable(this@HomeActivity)) {
                                showSearchLoader()
                                autoCompleteViewModel.autocomplete(s.toString())
                            } else {
                                hideSearchLoader()
                                showNoInternetMessage()
                                hideBottomHintText()
                                hideRecyclerView()
                                showSearchError()
                                hideApiInfo()
                                editTextErrorApiDrawable()
                            }

                        } else {
                            hideRecyclerView()
                            hideSearchError()
                            hideApiInfo()
                            showBottomHintText()
                            editTextNormalDrawable()
                            hideSearchLoader()
                        }
                    }

                }

            })
            autoCompleteViewModel.searchApiError.observe(this, Observer {
                showErrorToast(it)
                showSearchError()
                hideRecyclerView()
                hideBottomHintText()
                hideApiInfo()
                hideSearchLoader()
                editTextErrorApiDrawable()
            })
            autoCompleteViewModel.detailSearchApiError.observe(this,{
                showErrorToast(it)
            })
            editTextInputLayout = findViewById(R.id.edit_text_input_layout)
            editTextInputLayout.setEndIconOnClickListener {
                editText.text.clear()
            }


        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    private fun populateSearchList(searchList: ArrayList<AutocompleteResponseItem>) {
        try {
            if (this::searchAdapter.isInitialized) {
                searchAdapter.updateSearchList(searchList)
            } else {
                searchAdapter = SearchAdapter(searchList, this)
                recycler.adapter = searchAdapter
            }

        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    private fun showApiInfo(infoText: String) {
        val apiInfText: TextView = findViewById(R.id.api_info)
        if (!apiInfText.isVisible) {
            apiInfText.visibility = View.VISIBLE
        }
        apiInfText.text =
            getString(R.string.from_text) + " " + infoText.toLowerCase().capitalize()
    }

    private fun hideApiInfo() {
        val apiInfText: TextView = findViewById(R.id.api_info)
        if (apiInfText.isVisible) {
            apiInfText.visibility = View.GONE
        }
    }

    private fun showSearchError() {
        val apiInfText: TextView = findViewById(R.id.search_error)
        if (!apiInfText.isVisible) {
            apiInfText.visibility = View.VISIBLE
        }
    }

    private fun hideSearchError() {
        val apiInfText: TextView = findViewById(R.id.search_error)
        if (apiInfText.isVisible) {
            apiInfText.visibility = View.GONE
        }
    }

    private fun showBottomHintText() {
        val bottomHintText: TextView = findViewById(R.id.bottom_hint_text)
        if (!bottomHintText.isVisible) {
            bottomHintText.visibility = View.VISIBLE
        }
    }

    private fun hideBottomHintText() {
        val bottomHintText: TextView = findViewById(R.id.bottom_hint_text)
        if (bottomHintText.isVisible) {
            bottomHintText.visibility = View.GONE
        }
    }

    private fun showRecyclerView() {
        if (this::recycler.isInitialized && !recycler.isVisible) {
            recycler.visibility = View.VISIBLE
        }
    }

    private fun hideRecyclerView() {
        if (this::recycler.isInitialized && recycler.isVisible) {
            recycler.visibility = View.GONE
        }
    }

    private fun showNoInternetMessage() {
        Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    }

    private fun showErrorToast(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    private fun moveToMapActivity(detailsResponseItem: DetailsResponseItem) {
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra("details", detailsResponseItem)
        startActivityForResult(intent, 150)
    }

    override fun onItemClickListener(autocompleteResponseItem: AutocompleteResponseItem?) {
        if (autocompleteResponseItem != null) {
            if (Util.isNetworkAvailable(this)) {
                autoCompleteViewModel.details(autocompleteResponseItem)
            } else {
                showNoInternetMessage()
            }
        }
    }

    private fun editTextSuccessApiDrawable() {
        val editTextInput: TextInputLayout = findViewById(R.id.edit_text_input_layout)
        editTextInput.boxStrokeColor = getColor(R.color.search_success_color)
        editTextInput.boxStrokeWidth = Util.dpToPx(this, 3.0f)
        editTextInput.boxStrokeWidthFocused = Util.dpToPx(this, 3.0f)
    }

    private fun editTextErrorApiDrawable() {
        val editTextInput: TextInputLayout = findViewById(R.id.edit_text_input_layout)
        editTextInput.boxStrokeColor = getColor(R.color.search_error_color)
        editTextInput.boxStrokeWidth = Util.dpToPx(this, 3.0f)
        editTextInput.boxStrokeWidthFocused = Util.dpToPx(this, 3.0f)
    }

    private fun editTextNormalDrawable() {
        val editTextInput: TextInputLayout = findViewById(R.id.edit_text_input_layout)
        editTextInput.boxStrokeColor = getColor(R.color.search_success_color)
        editTextInput.boxStrokeWidth = Util.dpToPx(this, 3.0f)
        editTextInput.boxStrokeWidthFocused = Util.dpToPx(this, 3.0f)
    }

    private fun showSearchLoader() {
        val loader: CircularProgressBar = findViewById(R.id.loader)
        if (!loader.isVisible) {
            loader.visibility = View.VISIBLE
        }
    }

    private fun hideSearchLoader() {
        val loader: CircularProgressBar = findViewById(R.id.loader)
        if (loader.isVisible) {
            loader.visibility = View.GONE
        }
    }


}