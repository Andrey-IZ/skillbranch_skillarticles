package ru.skillbranch.skillarticles.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.text.getSpans
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.activity_root.toolbar
import kotlinx.android.synthetic.main.layout_bootombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import kotlinx.android.synthetic.main.search_view_layout.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import ru.skillbranch.skillarticles.viewmodels.base.ViewModelFactory
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.custom.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.SearchSpan

class RootActivity : BaseActivity<ArticleViewModel>(), IArticleView {

    override val layout: Int = R.layout.activity_root
    private var searchQuery: String? = null
    private var isSearching: Boolean = false
    override lateinit var viewModel: ArticleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vmFactory =
            ViewModelFactory("0")
        viewModel = ViewModelProvider(this, vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this) {
            renderUI(it)

            // restore search mode
            if (it.isSearch) {
                isSearching = true
                searchQuery = it.searchQuery
            }
        }
        viewModel.observeNotifications(this) {
            renderNotifications(it)
        }
    }

    override fun setupViews() {
        setupToolbar()
        setupBottomBar()
        setupSubmenu()
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        val content = tv_text_content.text as Spannable
        val bgColor = Color.RED
        val fgColor = Color.WHITE

        // clear entry search result
        clearSearchResult()

        searchResult.forEach { (start, end) ->
            content.setSpan(
                SearchSpan(bgColor, fgColor),
                start,
                end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // scroll to first search element
        renderSearchPosition(0)
    }

    override fun renderSearchPosition(searchPosition: Int) {
        val content = tv_text_content.text as Spannable
        val bgColor = Color.RED
        val fgColor = Color.WHITE

        val spans = content.getSpans<SearchSpan>()
        // clear last search position
        content.getSpans<SearchFocusSpan>().forEach { content.removeSpan(it) }

        if (spans.isNotEmpty()) {
            // find position span
            val result = spans[searchPosition]
            Selection.setSelection(content, content.getSpanStart(result))
            content.setSpan(
                SearchFocusSpan(bgColor, fgColor),
                content.getSpanStart(result),
                content.getSpanEnd(result),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun clearSearchResult() {
        val content = tv_text_content.text as Spannable
        content.getSpans<SearchSpan>()
            .forEach { content.removeSpan(it) }
    }

    override fun showSearchBar() {
        bottombar.setSearchState(true)
        scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        bottombar.setSearchState(false)
        scroll.setMarginOptionally(bottom = dpToIntPx(0))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu?.findItem(R.id.action_search)
        val searchView = menuItem?.actionView as? SearchView
        searchView?.queryHint = getString(R.string.article_search_placeholder)

        // restore searchView
        if (isSearching) {
            menuItem?.expandActionView()
            searchView?.setQuery(searchQuery, false)
            searchView?.clearFocus()
        }

        menuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return false
            }
        })

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun renderNotifications(notify: Notify) {
        val snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)

        when (notify) {
            is Notify.TextMessage -> { /* nothing */ }

            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler.invoke()
                }
            }

            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
        }
        snackbar.show()
    }

    private fun setupSubmenu() {
        btn_text_down.setOnClickListener { viewModel.handleDownText() }
        btn_text_up.setOnClickListener { viewModel.handleUpText() }
        switch_mode.setOnClickListener { viewModel.handleNightMode() }
    }

    private fun setupBottomBar() {
        btn_like.setOnClickListener { viewModel.handleLike() }
        btn_bookmark.setOnClickListener { viewModel.handleBookmark() }
        btn_settings.setOnClickListener { viewModel.handleToggleMenu() }
        btn_share.setOnClickListener { viewModel.handleShare() }

        btn_result_up.setOnClickListener{
            if (search_view.hasFocus()) search_view.clearFocus()
            viewModel.handleUpResult()
        }
        btn_result_down.setOnClickListener {
            if (search_view.hasFocus()) search_view.clearFocus()
            viewModel.handDownResult()
        }
        btn_search_close.setOnClickListener {
            viewModel.handleSearchMode(false)
            invalidateOptionsMenu()
        }
    }

    private fun renderUI(data: ArticleState) {
        Log.e("SkillArticles", "renderUI: $data")

        if (data.isSearch) showSearchBar() else hideSearchBar()

        if (data.searchResults.isNotEmpty()) renderSearchResult(data.searchResults)
        if (data.searchResults.isNotEmpty()) renderSearchPosition(data.searchPosition)

        // bind submenu state
        btn_settings.isChecked = data.isShowMenu
        if (data.isShowMenu) submenu.open() else submenu.close()

        // bind article person data
        btn_like.isChecked = data.isLike
        btn_bookmark.isChecked = data.isBookmark

        // bind submenu views
        switch_mode.isChecked = data.isDarkMode
        delegate.localNightMode =
            if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if (data.isBigText) {
            tv_text_content.textSize = 18f
        } else {
            tv_text_content.textSize = 14f
        }
        btn_text_up.isChecked = data.isBigText
        btn_text_down.isChecked = !data.isBigText

        // bind content
        if (data.isLoadingContent) {
            tv_text_content.text = "loading..."
        } else if (tv_text_content.text == "loading...") { // don't overload content
            val content = data.content.first() as String
            tv_text_content.setText(content, TextView.BufferType.SPANNABLE)
            tv_text_content.movementMethod = ScrollingMovementMethod()

        }

        //bind toolbar
        toolbar.title = data.title ?: "loading"
        toolbar.subtitle = data.category ?: "loading"
        if (data.category !=null) toolbar.logo = getDrawable(data.categoryIcon as Int)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val logo = if (toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP

        val lp = logo?.layoutParams as? Toolbar.LayoutParams
        lp?.let {
            it.width = dpToIntPx(40)
            it.height = dpToIntPx(40)
            it.marginEnd = dpToIntPx(16)
            logo.layoutParams = it
        }

    }

}
