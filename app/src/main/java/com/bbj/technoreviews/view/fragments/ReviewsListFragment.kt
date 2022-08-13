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
import com.arellomobile.mvp.presenter.PresenterType
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.modeks.ResultType
import com.bbj.technoreviews.data.modeks.Review
import com.bbj.technoreviews.view.adapter.ReviewListAdapter
import com.bbj.technoreviews.view.presenter.MainPresenter
import com.bbj.technoreviews.view.presenter.MainViewState

class ReviewsListFragment : MvpAppCompatFragment(), MainViewState {

    @InjectPresenter(tag = "main", type = PresenterType.GLOBAL)
    lateinit var presenter: MainPresenter

    private var productNameString = ""
        set(value) {
            field = if (value.length > 30) {
                value.removeRange(30, value.length) + "..."
            } else
                value
        }

    val adapter: ReviewListAdapter by lazy { ReviewListAdapter(requireContext()) }
    lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        productNameString = arguments?.getString("NAME") ?: "Какой-то продукт"
        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.getObservableReviews()

        progressBar = view.findViewById(R.id.review_fragment_progress_bar)
        progressBar.visibility = View.VISIBLE
        val productName: TextView = view.findViewById(R.id.review_product_name)
        productName.text = productNameString
        val reviewList: RecyclerView = view.findViewById(R.id.review_list)
        reviewList.adapter = adapter
    }

    override fun onNewSubscribe() {
        adapter.clearElements()
    }

    override fun addToList(result: ResultType) {
        if (result is Review)
            adapter.addElement(result)
    }

    override fun showError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    override fun onComplete() {
        progressBar.visibility = View.GONE
    }
}