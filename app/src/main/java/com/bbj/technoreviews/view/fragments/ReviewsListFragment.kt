package com.bbj.technoreviews.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.Shop
import com.bbj.technoreviews.data.models.Review
import com.bbj.technoreviews.view.adapter.ReviewListAdapter
import com.bbj.technoreviews.view.presenter.ReviewFragmentPresenter
import com.bbj.technoreviews.view.presenter.ReviewView

class ReviewsListFragment : MvpAppCompatFragment(), ReviewView {

    @InjectPresenter
    lateinit var presenter: ReviewFragmentPresenter

    private var position : Int = 0
    private lateinit var shopName : Shop

    lateinit var progressBar: ProgressBar

    val adapter: ReviewListAdapter by lazy { ReviewListAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            position = it.getInt(SampleFragment.POSITION)
           shopName = Shop.valueOf(it.getString(SampleFragment.SHOP) ?: "ALL")
        }

        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.getObservableReviews(position, shopName)

        progressBar = view.findViewById(R.id.review_fragment_progress_bar)
        progressBar.visibility = View.VISIBLE
        val reviewList: RecyclerView = view.findViewById(R.id.review_list)
        reviewList.adapter = adapter
    }

    override fun onNewSubscribe() {
        adapter.clearElements()
    }

    override fun addToList(review: Review) {
            adapter.addElement(review)
    }

    override fun showError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    override fun onComplete() {
        progressBar.visibility = View.GONE
    }
}