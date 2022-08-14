package com.bbj.technoreviews.view.fragments

import android.animation.Animator
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.Shop
import com.bbj.technoreviews.data.modeks.Sample
import com.bbj.technoreviews.databinding.FragmentSampleBinding
import com.bbj.technoreviews.view.MainActivity
import com.bbj.technoreviews.view.adapter.SampleListAdapter
import com.bbj.technoreviews.view.presenter.SampleFragmentPresenter
import com.bbj.technoreviews.view.presenter.SampleView

const val transitionDuration : Long = 500
const val productListTranslation = 1600f

class SampleFragment : MvpAppCompatFragment(), SampleView {

    init {
        Log.d("ISINITFRAG","INIT PREVIEW")
    }

    companion object{
        const val NAME_KEY = "NAME"
        const val SHOP = "SHOP"
        const val POSITION = "POSITION"
    }

    val TAG = "PREVIEWFRAGMENT"

   lateinit var binding : FragmentSampleBinding

    val onProductClick = object : SampleListAdapter.OnProductClick {
        override fun click(productName: String,shop : Shop,position : Int) {
            val bundle = Bundle().apply {
                putString(NAME_KEY, productName)
                putString(SHOP,shop.toString())
                putInt(POSITION,position)}
            (requireActivity() as MainActivity).goToReviewsFragment(bundle)
        }
    }

    val DNSadapter by lazy { SampleListAdapter(requireContext(),onProductClick)}
    val BVAdapter by lazy { SampleListAdapter(requireContext(),onProductClick)}
    val alserAdapter by lazy { SampleListAdapter(requireContext(),onProductClick)}
    val mechtaAdapter by lazy { SampleListAdapter(requireContext(),onProductClick)}

    @InjectPresenter(tag = "main", type = PresenterType.GLOBAL)
    lateinit var presenter: SampleFragmentPresenter

    private var currentSearchRequest = ""

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSampleBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewRoot.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

//        val transitionSet = TransitionSet().apply{
//            addTransition(ChangeBounds())
//            duration = 600
//            interpolator = AccelerateInterpolator()
//        }
//        TransitionManager.beginDelayedTransition(viewRoot,transitionSet)
//        TransitionManager.endTransitions(viewRoot)

        progressBar = view.findViewById(R.id.progress_bar)

//        val animatedBg: AnimationDrawable = sceneRoot.background as AnimationDrawable
//        animatedBg.setEnterFadeDuration(2000);
//        animatedBg.setExitFadeDuration(3000);

        binding.run {
            dnsPreviewList.adapter = DNSadapter
            bvPreviewList.adapter = BVAdapter
            mechtaPreviewList.adapter = mechtaAdapter
            alserPreviewList.adapter = alserAdapter

        }


        binding.searchField.setOnTouchListener { mVIew, event ->
            if (MotionEvent.ACTION_DOWN == event.action) {
                mVIew.performClick()
                    false

            } else if (event.action == MotionEvent.ACTION_UP){
                binding.viewRoot.run {
                    gravity = Gravity.NO_GRAVITY
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                false
            } else false
        }

        binding.searchField.setOnEditorActionListener { v, actionId, keyEvent ->
            Log.d(TAG, "Something pressed")
            val searchRequest = v.text.toString()
            if (actionId == EditorInfo.IME_ACTION_GO && searchRequest != currentSearchRequest) {
                currentSearchRequest = searchRequest
                Log.d(TAG, "Enter pressed")
                progressBar.visibility = View.VISIBLE
                presenter.getObservablePreviews(searchRequest)
                true
            } else false
        }
    }

    private fun setOnTouchListenerToView(viewToSet : ViewGroup, hiddenList : View, spinnedView : ImageView){
        viewToSet.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_UP -> {
                        if (view != null) {
                            onClick(hiddenList,view,spinnedView)
                        }
                        view?.performClick()
                    }
                }
                return true
            }
        })
    }


    override fun onNewSubscribe() {
        DNSadapter.clearElements()
        BVAdapter.clearElements()
        mechtaAdapter.clearElements()
        alserAdapter.clearElements()

    }

    override fun addToList(sample: Sample) {
        Log.d(TAG, "Add to List")
        when(sample.shopName){
            Shop.DNS -> {
                DNSadapter.addElement(sample)
                if (binding.dnsShopArea.visibility == View.GONE){
                    binding.run {
                        dnsShopArea.visibility = View.VISIBLE
                        bvShopArea.visibility = View.VISIBLE
                        dnsPreviewList.visibility = View.VISIBLE
                        setOnTouchListenerToView(dnsShopArea, dnsPreviewList, dnsShopShow)
                    }
                }
            }
            Shop.BELIY_VETER -> {
                BVAdapter.addElement(sample)
                if (binding.bvShopArea.visibility == View.GONE){
                    binding.run {
                        bvShopArea.visibility = View.VISIBLE
                        bvPreviewList.visibility = View.VISIBLE
                        setOnTouchListenerToView(bvShopArea, bvPreviewList, bvShopShow)
                    }
                }
            }
            Shop.MECHTA -> {
                mechtaAdapter.addElement(sample)
                if (binding.mechtaShopArea.visibility == View.GONE){
                    binding.run {
                        mechtaShopArea.visibility = View.VISIBLE
                        mechtaPreviewList.visibility = View.VISIBLE
                        setOnTouchListenerToView(mechtaShopArea, mechtaPreviewList, mechtaShopShow)
                    }
                }
            }
            Shop.ALSER -> {
                alserAdapter.addElement(sample)
                if (binding.alserShopArea.visibility == View.GONE){
                    binding.run {
                        alserShopArea.visibility = View.VISIBLE
                        alserPreviewList.visibility = View.VISIBLE
                        setOnTouchListenerToView(alserShopArea, alserPreviewList, alserShopShow)
                    }
                }
            }
            else -> error("Unknown shop name")
        }
    }

    override fun showError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    override fun onComplete() {
       progressBar.visibility = View.GONE
    }

    private fun onClick(productCard : View,touchedZone : View,spinnedView : View) {
        Log.d("ADAPTER", "On click")
        if (productCard.visibility != View.VISIBLE) {
            Log.d("ADAPTER", "Gone")
            spinnedView
                .animate().rotationBy(180f).apply {
                    duration = 300
                    interpolator = AccelerateInterpolator()
                    start()
                }
            productCard.translationY = -productListTranslation
            productCard.visibility = View.VISIBLE
            productCard.animate().apply {
                duration = transitionDuration
                translationY(0f)
                interpolator = AccelerateInterpolator()
            }.setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                    touchedZone.isEnabled = false
                }

                override fun onAnimationEnd(p0: Animator?) {
                    touchedZone.isEnabled = true
                }

                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationRepeat(p0: Animator?) {}
            }).start()
            productCard.visibility = View.VISIBLE
        } else {
            Log.d("ADAPTER", "Visble")
            spinnedView
                .animate().rotationBy(180f).apply {
                    duration = 300
                    interpolator = AccelerateInterpolator()
                    start()
                }
            productCard.animate().apply {
                duration = transitionDuration
                translationY(-productListTranslation)
                interpolator = AccelerateInterpolator()
            }.start()
            Handler(Looper.getMainLooper()).postDelayed(
                { productCard.visibility = View.GONE },
                transitionDuration
            )
        }
    }
}