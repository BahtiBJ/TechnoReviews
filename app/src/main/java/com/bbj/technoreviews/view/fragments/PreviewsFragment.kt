package com.bbj.technoreviews.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.modeks.Preview
import com.bbj.technoreviews.data.modeks.ResultType
import com.bbj.technoreviews.view.MainActivity
import com.bbj.technoreviews.view.presenter.MainPresenter
import com.bbj.technoreviews.view.presenter.MainViewState
import com.bbj.technoreviews.view.adapter.PreviewListAdapter

const val NAME_KEY = "NAME"
const val ACTION_DONE_ID = 1

class PreviewsFragment : MvpAppCompatFragment(), MainViewState {

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
        PreviewListAdapter(requireContext(),
            object : PreviewListAdapter.OnProductClick {
                override fun click(productName: String) {
                    val bundle = Bundle().apply { putString(NAME_KEY, productName) }
                    (requireActivity() as MainActivity).goToReviewsFragment(bundle)
                }
            })
    }

    @InjectPresenter(tag = "main", type = PresenterType.GLOBAL)
    lateinit var presenter: MainPresenter

    private var currentSearchRequest = ""

    private lateinit var progressBar : ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progress_bar)

        view.setFocusableInTouchMode(true)
        view.requestFocus()
        view.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                return if (keyCode == KeyEvent.KEYCODE_BACK) {
                    TransitionManager.go(scene1, set)
                    true
                } else false
            }
        })

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
            Log.d(TAG,"Something pressed")
            val searchRequest = searchField.text.toString()
            if (actionId == EditorInfo.IME_ACTION_GO && searchRequest != currentSearchRequest) {
                currentSearchRequest = searchRequest
                Log.d(TAG,"Enter pressed")
                progressBar.visibility = View.VISIBLE
                presenter.getObservablePreviews(searchRequest)
                presenter.getObservableReviews()
                true
            }else false
        }
    }


    override fun onNewSubscribe() {
        adapter.clearElements()
    }

    override fun addToList(result: ResultType) {
        Log.d(TAG,"Add to List")
        if (result is Preview)
            adapter.addElement(result)
    }

    override fun showError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    override fun onComplete() {
        progressBar.visibility = View.GONE
    }
}