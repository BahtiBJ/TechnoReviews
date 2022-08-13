package com.bbj.technoreviews.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bbj.technoreviews.R
import com.bbj.technoreviews.view.fragments.PreviewsFragment
import com.bbj.technoreviews.view.fragments.ReviewsListFragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, PreviewsFragment())
            .addToBackStack("previews")
            .commit()
    }

    fun goToReviewsFragment(bundle: Bundle) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.enter_anim,
                R.anim.exit_anim,
                R.anim.pop_enter_anim,
                R.anim.pop_exit_anim
            )
            .add(R.id.main_fragment_container, ReviewsListFragment::class.java, bundle)
            .commit()
    }

    override fun onBackPressed() {
        supportFragmentManager.popBackStack("previews",0)
    }

}