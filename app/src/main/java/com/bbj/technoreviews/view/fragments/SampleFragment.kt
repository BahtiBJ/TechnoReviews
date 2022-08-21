package com.bbj.technoreviews.view.fragments

import android.animation.ArgbEvaluator
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.transition.*
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.Shop
import com.bbj.technoreviews.data.models.Sample
import com.bbj.technoreviews.databinding.FragmentSampleBinding
import com.bbj.technoreviews.view.MainActivity
import com.bbj.technoreviews.view.adapter.SampleListAdapter
import com.bbj.technoreviews.view.presenter.SampleFragmentPresenter
import com.bbj.technoreviews.view.presenter.SampleView


const val transitionDuration: Long = 500
const val productListTranslation = 1900f

class SampleFragment : MvpAppCompatFragment(), SampleView {

    init {
        Log.d("ISINITFRAG", "INIT PREVIEW")
    }

    companion object {
        const val NAME_KEY = "NAME"
        const val SHOP = "SHOP"
        const val POSITION = "POSITION"
    }

    val TAG = "PREVIEWFRAGMENT"

    private var isSearchFieldOnTop = false
    private var isResultEmpty = false

    lateinit var binding: FragmentSampleBinding

    val onProductClick = object : SampleListAdapter.OnProductClick {
        override fun click(productName: String, shop: Shop, position: Int) {
            val bundle = Bundle().apply {
                putString(NAME_KEY, productName)
                putString(SHOP, shop.toString())
                putInt(POSITION, position)
            }
            (requireActivity() as MainActivity).goToReviewsFragment(bundle)
        }
    }

    val DNSadapter by lazy { SampleListAdapter(requireContext(), onProductClick) }
    val kaspiAdapter by lazy { SampleListAdapter(requireContext(), onProductClick) }
    val BVAdapter by lazy { SampleListAdapter(requireContext(), onProductClick) }

    @InjectPresenter(tag = "main", type = PresenterType.GLOBAL)
    lateinit var presenter: SampleFragmentPresenter

    private var currentSearchRequest = ""

    private val translateOnTopSet = TransitionSet().apply {
        addTransition(ChangeTransform())
        addTransition(ChangeBounds())
        duration = 500
        interpolator = AnticipateOvershootInterpolator()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSampleBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isSearchFieldOnTop)
            binding.viewRoot.gravity = Gravity.CENTER

        val bgAnimator = createBgColorAnimator()

        binding.sampleListRoot.layoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
        binding.sampleListRoot.layoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)

        binding.run {
            dnsPreviewList.adapter = DNSadapter
            kaspiPreviewList.adapter = kaspiAdapter
            bvPreviewList.adapter = BVAdapter
        }

        binding.searchField.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN && isSearchFieldOnTop == false) {
                bgAnimator.start()
                TransitionManager.beginDelayedTransition(binding.viewRoot, translateOnTopSet)

                binding.viewRoot.gravity = Gravity.TOP and Gravity.CENTER_HORIZONTAL
                binding.searchField.scaleX = 1f
                binding.searchField.scaleY = 1f

                Handler(Looper.getMainLooper()).postDelayed({
                    TransitionManager.endTransitions(binding.viewRoot)
                    binding.sampleScrollView.visibility = View.VISIBLE
                    isSearchFieldOnTop = true
                }, 1000)
                false
            } else false

        }

        binding.searchField.setOnEditorActionListener { v, actionId, keyEvent ->
            Log.d(TAG, "Something pressed")
            val searchRequest = v.text.toString()
            if (actionId == EditorInfo.IME_ACTION_GO && searchRequest != currentSearchRequest) {
                currentSearchRequest = searchRequest
                Log.d(TAG, "Enter pressed")

                hideAll()

                if (binding.dnsPreviewList.visibility != View.GONE) {
                    clickOnShopPreview(Shop.DNS)
                }
                if (binding.bvPreviewList.visibility != View.GONE) {
                    clickOnShopPreview(Shop.BELIY_VETER)
                }

                binding.progressAnim.visibility = View.VISIBLE
                binding.progressAnim.setAnimation(R.raw.search_anim)
                binding.progressAnim.playAnimation()

                hideKeyboard(requireContext(), requireView())

                presenter.getObservablePreviews(searchRequest)
                true
            } else false
        }
    }

    private fun setOnTouchListenerToView(
        shop: Shop
    ) {
        val viewToSet: View = when (shop) {
            Shop.DNS -> binding.dnsShopArea
            Shop.KASPI -> binding.kaspiShopArea
            Shop.BELIY_VETER -> binding.bvShopArea
            else -> error("Unknown shop name")
        }
        viewToSet.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_UP -> {
                        if (view != null) {
                            clickOnShopPreview(shop)
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
        kaspiAdapter.clearElements()
        BVAdapter.clearElements()
    }

    override fun addToList(sample: Sample) {
        Log.d(TAG, "Add to List")
        when (sample.shopName) {
            Shop.DNS -> {
                DNSadapter.addElement(sample)
                if (binding.dnsShopArea.visibility == View.GONE) {
                    binding.run {
                        dnsShopArea.visibility = View.VISIBLE
                        dnsPreviewList.visibility = View.VISIBLE
                        setOnTouchListenerToView(Shop.DNS)
                    }
                }
            }
            Shop.BELIY_VETER -> {
                BVAdapter.addElement(sample)
                Log.d(TAG, "adapter list size = ${BVAdapter.itemCount}")
                if (binding.bvShopArea.visibility == View.GONE) {
                    binding.run {
                        bvShopArea.visibility = View.VISIBLE
                        bvPreviewList.visibility = View.VISIBLE
                        setOnTouchListenerToView(Shop.BELIY_VETER)
                    }
                }
            }
            Shop.KASPI -> {
                kaspiAdapter.addElement(sample)
                Log.d(TAG, "adapter list size = ${kaspiAdapter.itemCount}")
                if (binding.kaspiShopArea.visibility == View.GONE) {
                    binding.run {
                        kaspiShopArea.visibility = View.VISIBLE
                        kaspiPreviewList.visibility = View.VISIBLE
                        setOnTouchListenerToView(Shop.KASPI)
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
        binding.progressAnim.pauseAnimation()
        binding.progressAnim.visibility = View.GONE
        if (DNSadapter.itemCount == 0
            && kaspiAdapter.itemCount == 0
            && BVAdapter.itemCount == 0
        ) {
            hideAll()
            binding.progressAnim.setAnimation(R.raw.not_found)
            binding.progressAnim.visibility = View.VISIBLE
            binding.progressAnim.playAnimation()
            isResultEmpty = true
        }
    }

    private fun createBgColorAnimator(): ValueAnimator {
        val gradientBg = binding.viewRoot.background as GradientDrawable
        val argbEvaluator = ArgbEvaluator()
        val grayColor = requireActivity().resources.getColor(R.color.gray_light, null)
        val redColor = requireActivity().resources.getColor(R.color.primary, null)
        val roseColor = requireActivity().resources.getColor(R.color.primary_light, null)
        return ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            interpolator = AccelerateInterpolator()
            duration = 1000
            addUpdateListener {
                val startColor =
                    argbEvaluator.evaluate(it.animatedFraction, redColor, grayColor) as Int
                val endColor =
                    argbEvaluator.evaluate(it.animatedFraction, roseColor, grayColor) as Int
                gradientBg.colors = intArrayOf(startColor, endColor)
            }
        }
    }


    private fun clickOnShopPreview(shop: Shop) {
        val productCard: View
        val spinnedView: View
        when (shop) {
            Shop.DNS -> {
                productCard = binding.dnsPreviewList
                spinnedView = binding.dnsShopShow
            }
            Shop.BELIY_VETER -> {
                productCard = binding.bvPreviewList
                spinnedView = binding.bvShopShow
            }
            Shop.KASPI -> {
                productCard = binding.kaspiPreviewList
                spinnedView = binding.kaspiShopShow
            }
            else -> return
        }
        if (productCard.visibility != View.VISIBLE) {
            Log.d("ADAPTER", "Gone")
            spinnedView
                .animate().rotationBy(180f).apply {
                    duration = 300
                    interpolator = AccelerateInterpolator()
                    start()
                }
            productCard.translationY = -productListTranslation
            Handler(Looper.getMainLooper()).postDelayed(
                { productCard.visibility = View.VISIBLE },
                150
            )
            productCard.animate().apply {
                duration = transitionDuration - 150
                translationY(0f)
                interpolator = LinearOutSlowInInterpolator()
            }.start()
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
                this.translationY(-productListTranslation)
                interpolator = AccelerateInterpolator()
            }.start()
            Handler(Looper.getMainLooper()).postDelayed(
                { productCard.visibility = View.GONE },
                transitionDuration - 250
            )
        }
    }

    private fun hideAll() {
        val gone = View.GONE
        binding.run {
            changeVisibilitySeveralViews(
                gone,
                dnsShopArea,
                dnsPreviewList,
                kaspiShopArea,
                kaspiPreviewList,
                bvShopArea,
                bvPreviewList,
                progressAnim
            )
        }
    }

    fun hideKeyboard(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun changeVisibilitySeveralViews(visibility: Int, vararg views: View) {
        for (view in views) {
            view.visibility = visibility
        }
    }

}