package com.bbj.technoreviews.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.TransitionSet
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.modeks.Sample
import com.bbj.technoreviews.data.modeks.ResultType
import com.bbj.technoreviews.view.MainActivity
import com.bbj.technoreviews.view.adapter.SampleListAdapter
import com.bbj.technoreviews.view.presenter.SampleFragmentPresenter
import com.bbj.technoreviews.view.presenter.SampleView

class SampleFragment : MvpAppCompatFragment(), SampleView {

    val NAME_KEY = "NAME"
    val ACTION_DONE_ID = 1

    init {
        Log.d("ISINITFRAG","INIT PREVIEW")
    }

    val TAG = "PREVIEWFRAGMENT"

    val set = TransitionSet().apply {
        //        set.addTransition(Fade())
        addTransition(Slide())
        setOrdering(TransitionSet.ORDERING_TOGETHER)
        setDuration(300)
        setInterpolator(AccelerateInterpolator())
    }

    val sceneRoot: LinearLayoutCompat by lazy { requireView().findViewById(R.id.view_root) }
    val scene2 by lazy {
        Scene.getSceneForLayout(
            sceneRoot,
            R.layout.main_scene_2,
            requireContext()
        )
    }
    val scene1 by lazy {
        Scene.getSceneForLayout(
            sceneRoot,
            R.layout.main_scene_1,
            requireContext()
        )
    }

    val adapter by lazy {
        SampleListAdapter(requireContext(),
            object : SampleListAdapter.OnProductClick {
                override fun click(productName: String) {
                    val bundle = Bundle().apply { putString(NAME_KEY, productName) }
                    (requireActivity() as MainActivity).goToReviewsFragment(bundle)
                }
            })
    }

    @InjectPresenter(tag = "main", type = PresenterType.GLOBAL)
    lateinit var presenter: SampleFragmentPresenter

    private var currentSearchRequest = ""

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sample, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progress_bar)

//        val animatedBg: AnimationDrawable = sceneRoot.background as AnimationDrawable
//        animatedBg.setEnterFadeDuration(2000);
//        animatedBg.setExitFadeDuration(3000);

        val searchField: EditText = view.findViewById(R.id.search_field)

        val previewList: RecyclerView = view.findViewById(R.id.previewList)
        previewList.adapter = this.adapter

//        searchField.setOnTouchListener { view, event ->
//            if (MotionEvent.ACTION_UP == event.action) {
//                TransitionManager.go(scene2, set)
////                animatedBg.start()
//                true
//            } else false
//        }

        searchField.setOnEditorActionListener { v, actionId, keyEvent ->
            Log.d(TAG, "Something pressed")
            val searchRequest = searchField.text.toString()
            if (actionId == EditorInfo.IME_ACTION_GO && searchRequest != currentSearchRequest) {
                currentSearchRequest = searchRequest
                Log.d(TAG, "Enter pressed")
                progressBar.visibility = View.VISIBLE
                presenter.getObservablePreviews(searchRequest)
                true
            } else false
        }
    }


    override fun onNewSubscribe() {
        adapter.clearElements()
    }

    override fun addToList(result: ResultType) {
        Log.d(TAG, "Add to List")
        if (result is Sample)
            adapter.addElement(result)
    }

    override fun showError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    override fun onComplete() {
       progressBar.visibility = View.GONE
    }
}