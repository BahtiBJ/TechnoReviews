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
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.ChangeTransform
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
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
import com.bbj.technoreviews.view.util.isOnline

const val transitionDuration: Long = 500
const val productListTranslation = 1900f

class SampleFragment : MvpAppCompatFragment(), SampleView {

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

        binding.sampleListRoot.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        binding.run {
            dnsPreviewList.adapter = DNSadapter
            kaspiPreviewList.adapter = kaspiAdapter
            bvPreviewList.adapter = BVAdapter
        }
        setOnTouchListenerToView(Shop.DNS)
        setOnTouchListenerToView(Shop.BELIY_VETER)
        setOnTouchListenerToView(Shop.KASPI)


        binding.searchField.setOnTouchListener { _, motionEvent ->
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
            val searchRequest = v.text.toString().trim()
            if (actionId == EditorInfo.IME_ACTION_GO && searchRequest != currentSearchRequest) {
                currentSearchRequest = searchRequest

                if (binding.dnsPreviewList.visibility != View.GONE) {
                    clickOnShopPreview(Shop.DNS)
                }
                if (binding.bvPreviewList.visibility != View.GONE) {
                    clickOnShopPreview(Shop.BELIY_VETER)
                }
                if (binding.kaspiPreviewList.visibility != View.GONE) {
                    clickOnShopPreview(Shop.KASPI)
                }
                hideKeyboard(requireContext(), requireView())

                hideAll()

                binding.progressAnim.visibility = View.VISIBLE
                binding.progressAnim.setAnimation(R.raw.search_anim)
                binding.progressAnim.playAnimation()


                if (requireContext().isOnline()) {
                    presenter.getObservablePreviews(searchRequest)
                } else {
                    currentSearchRequest = "   "
                    showError()
                }
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
        when (sample.shopName) {
            Shop.DNS -> {
                DNSadapter.addElement(sample)
                binding.run {
                    dnsShopArea.visibility = View.VISIBLE
                    clickOnShopPreview(Shop.DNS)
                }
            }
            Shop.BELIY_VETER -> {
                BVAdapter.addElement(sample)
                binding.run {
                    bvShopArea.visibility = View.VISIBLE
                    clickOnShopPreview(Shop.BELIY_VETER)
                }
            }
            Shop.KASPI -> {
                kaspiAdapter.addElement(sample)
                binding.run {
                    kaspiShopArea.visibility = View.VISIBLE
                    clickOnShopPreview(Shop.KASPI)
                }
            }
            else -> error("Unknown shop name")
        }
    }

    override fun showError() {
        Toast.makeText(
            requireContext(), requireContext().resources.getText(R.string.error), Toast.LENGTH_LONG
        ).show()
        onComplete()
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
        val productList: View
        val spinnedView: View
        when (shop) {
            Shop.DNS -> {
                productList = binding.dnsPreviewList
                spinnedView = binding.dnsShopShow
            }
            Shop.BELIY_VETER -> {
                productList = binding.bvPreviewList
                spinnedView = binding.bvShopShow
            }
            Shop.KASPI -> {
                productList = binding.kaspiPreviewList
                spinnedView = binding.kaspiShopShow
            }
            else -> return
        }
        if (productList.visibility != View.VISIBLE) {
            spinnedView
                .animate().rotation(180f).apply {
                    duration = 300
                    interpolator = AccelerateInterpolator()
                    start()
                }
            productList.translationY = -productListTranslation
            productList.visibility = View.VISIBLE
            productList.animate().apply {
                duration = transitionDuration - 150
                translationY(0f)
                interpolator = LinearOutSlowInInterpolator()
            }.start()
        } else {
            spinnedView
                .animate().rotation(0f).apply {
                    duration = 300
                    interpolator = AccelerateInterpolator()
                    start()
                }
            productList.animate().apply {
                duration = transitionDuration
                this.translationY(-productListTranslation)
                productList.visibility = View.GONE
                interpolator = AccelerateInterpolator()
            }.start()
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