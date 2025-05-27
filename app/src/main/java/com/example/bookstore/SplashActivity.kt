package com.example.bookstore

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var splashLogo: ImageView
    private lateinit var appName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize views
        splashLogo = findViewById(R.id.splash_logo)
        appName = findViewById(R.id.app_name)

        // Start animation
        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        // Set initial positions and scales
        setupInitialState()

        // Create and start the animation sequence
        createAnimationSequence().start()
    }

    private fun setupInitialState() {
        // Move logo to the left (off-screen)
        splashLogo.translationX = -1000f
        splashLogo.scaleX = 0.3f
        splashLogo.scaleY = 0.3f
        splashLogo.alpha = 0f

        // Move text to the right (off-screen)
        appName.translationX = 1000f
        appName.scaleX = 0.3f
        appName.scaleY = 0.3f
        appName.alpha = 0f
    }

    private fun createAnimationSequence(): AnimatorSet {
        val animatorSet = AnimatorSet()

        // Phase 1: Slide in animations (logo from left, text from right)
        val logoSlideIn = createSlideInAnimation(splashLogo, -1000f, 0f)
        val textSlideIn = createSlideInAnimation(appName, 1000f, 0f)

        // Phase 2: Scale up and focus animations
        val logoScaleUp = createScaleUpAnimation(splashLogo)
        val textScaleUp = createScaleUpAnimation(appName)

        // Phase 3: Final focus effect (subtle bounce)
        val logoFocus = createFocusAnimation(splashLogo)
        val textFocus = createFocusAnimation(appName)

        // Sequence the animations
        animatorSet.apply {
            // Play slide-in animations together
            playTogether(logoSlideIn, textSlideIn)

            // Then play scale-up animations together
            play(logoScaleUp).after(logoSlideIn)
            play(textScaleUp).after(textSlideIn)

            // Finally play focus animations together
            play(logoFocus).after(logoScaleUp)
            play(textFocus).after(textScaleUp)

            // Navigate to main activity after animation completes
            doOnEnd {
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToMainActivity()
                }, 500) // Small delay after animation
            }
        }

        return animatorSet
    }

    private fun createSlideInAnimation(view: android.view.View, startX: Float, endX: Float): AnimatorSet {
        val slideAnimator = ObjectAnimator.ofFloat(view, "translationX", startX, endX).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        val alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        return AnimatorSet().apply {
            playTogether(slideAnimator, alphaAnimator)
        }
    }

    private fun createScaleUpAnimation(view: android.view.View): AnimatorSet {
        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0.3f, 1.0f).apply {
            duration = 600
            interpolator = OvershootInterpolator(1.2f)
        }

        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0.3f, 1.0f).apply {
            duration = 600
            interpolator = OvershootInterpolator(1.2f)
        }

        return AnimatorSet().apply {
            playTogether(scaleXAnimator, scaleYAnimator)
        }
    }

    private fun createFocusAnimation(view: android.view.View): AnimatorSet {
        // Create a subtle pulse effect for focus
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1.0f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1.0f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

        val focusAnimatorSet = AnimatorSet()
        focusAnimatorSet.play(scaleUpX).with(scaleUpY)
        focusAnimatorSet.play(scaleDownX).after(scaleUpX)
        focusAnimatorSet.play(scaleDownY).after(scaleUpY)

        return focusAnimatorSet
    }

    private fun navigateToMainActivity() {
        // Replace MainActivity::class.java with your actual main activity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()

        // Optional: Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    // Prevent back button during splash
//    override fun onBackPressed() {
//        // Do nothing or handle as needed
//    }
}

// Extension function for easier animation chaining (optional)
private fun AnimatorSet.playSequentially(vararg animators: android.animation.Animator): AnimatorSet {
    this.playSequentially(*animators)
    return this
}