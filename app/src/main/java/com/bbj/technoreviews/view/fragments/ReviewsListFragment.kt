package com.bbj.technoreviews.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.Shop
import com.bbj.technoreviews.data.models.Review
import com.bbj.technoreviews.view.adapter.ReviewListAdapter
import com.bbj.technoreviews.view.presenter.ReviewFragmentPresenter
import com.bbj.technoreviews.view.presenter.ReviewView
import com.bbj.technoreviews.view.util.isOnline

class ReviewsListFragment : MvpAppCompatFragment(), ReviewView {

    @InjectPresenter
    lateinit var presenter: ReviewFragmentPresenter

    private var position: Int = 0
    private lateinit var shopName: Shop

    lateinit var progressAnim: LottieAnimationView
    lateinit var reviewList: RecyclerView

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

        progressAnim = view.findViewById(R.id.result_anim)
        progressAnim.visibility = View.VISIBLE
        progressAnim.setAnimation(R.raw.search_anim)
        progressAnim.playAnimation()

        reviewList = view.findViewById(R.id.review_list)
        reviewList.adapter = adapter

        if (requireContext().isOnline()) {
            presenter.getObservableReviews(position, shopName)
        } else {
            showError()
        }

    }

    override fun onNewSubscribe() {
        adapter.clearElements()
    }

    override fun addToList(review: Review) {
        reviewList.visibility = View.VISIBLE
        adapter.addElement(review)
    }

    override fun showError() {
        Toast.makeText(requireContext()
            , requireContext().resources.getText(R.string.error)
            , Toast.LENGTH_LONG).show()
        onComplete()
    }

    override fun onComplete() {
        progressAnim.visibility = View.GONE
        progressAnim.pauseAnimation()
        if (adapter.itemCount == 0) {
            progressAnim.visibility = View.VISIBLE
            progressAnim.setAnimation(R.raw.not_found)
            progressAnim.playAnimation()
        }
    }
}