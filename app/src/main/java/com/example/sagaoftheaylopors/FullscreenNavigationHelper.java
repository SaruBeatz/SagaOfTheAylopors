package com.example.sagaoftheaylopors;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Helper class to manage fullscreen mode with custom navigation bar
 * that reveals on swipe-up gesture from bottom edge.
 */
public class FullscreenNavigationHelper {
    
    private static final int AUTO_HIDE_DELAY_MS = 2000; // 2 seconds
    private static final int SWIPE_THRESHOLD_DP = 20; // Minimum swipe distance
    private static final float SWIPE_VELOCITY_THRESHOLD = 500; // Minimum swipe velocity
    
    private final Activity activity;
    private final Window window;
    private LinearLayout customNavBar;
    private View bottomEdgeDetector;
    private Handler autoHideHandler;
    private Runnable autoHideRunnable;
    private boolean isNavBarVisible = false;
    private GestureDetector gestureDetector;
    private int swipeThresholdPx;
    
    public FullscreenNavigationHelper(Activity activity) {
        this.activity = activity;
        this.window = activity.getWindow();
        this.autoHideHandler = new Handler(Looper.getMainLooper());
        this.swipeThresholdPx = (int) (activity.getResources().getDisplayMetrics().density * SWIPE_THRESHOLD_DP);
        
        setupFullscreen();
        setupCustomNavigationBar();
        setupGestureDetection();
    }
    
    /**
     * Enable fullscreen mode and hide system navigation bar
     */
    private void setupFullscreen() {
        applyFullscreen();
    }
    
    /**
     * Apply fullscreen mode (can be called to re-apply after system bars appear)
     */
    public void applyFullscreen() {
        // Enable edge-to-edge
        View decorView = window.getDecorView();
        
        // Hide system bars
        WindowInsetsController insetsController = window.getInsetsController();
        if (insetsController != null) {
            insetsController.setSystemBarsBehavior(
                WindowInsetsController.BEHAVIOR_DEFAULT
            );
            insetsController.hide(WindowInsets.Type.navigationBars());
            insetsController.hide(WindowInsets.Type.statusBars());
        }
        
        // Legacy fullscreen flags for older Android versions
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }
    
    /**
     * Setup custom navigation bar view
     */
    private void setupCustomNavigationBar() {
        View rootView = window.getDecorView().findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            ViewGroup root = (ViewGroup) rootView;
            
            // Inflate custom navigation bar
            customNavBar = (LinearLayout) activity.getLayoutInflater()
                .inflate(R.layout.custom_navigation_bar, root, false);
            
            // Set layout params to position at bottom
            ViewGroup.LayoutParams navBarParams = customNavBar.getLayoutParams();
            if (navBarParams == null) {
                if (root instanceof android.widget.FrameLayout) {
                    navBarParams = new android.widget.FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.Gravity.BOTTOM
                    );
                } else {
                    navBarParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }
            } else if (root instanceof android.widget.FrameLayout && 
                      navBarParams instanceof android.widget.FrameLayout.LayoutParams) {
                ((android.widget.FrameLayout.LayoutParams) navBarParams).gravity = 
                    android.view.Gravity.BOTTOM;
            }
            customNavBar.setLayoutParams(navBarParams);
            
            // Add to root view
            root.addView(customNavBar);
            
            // Setup button listeners
            ImageButton backButton = customNavBar.findViewById(R.id.navBackButton);
            ImageButton homeButton = customNavBar.findViewById(R.id.navHomeButton);
            ImageButton recentButton = customNavBar.findViewById(R.id.navRecentButton);
            
            if (backButton != null) {
                backButton.setOnClickListener(v -> {
                    activity.onBackPressed();
                    hideNavigationBar();
                });
            }
            
            if (homeButton != null) {
                homeButton.setOnClickListener(v -> {
                    activity.moveTaskToBack(true);
                    hideNavigationBar();
                });
            }
            
            if (recentButton != null) {
                recentButton.setOnClickListener(v -> {
                    // Show recent apps - Android 11+ uses different approach
                    try {
                        // Try to show recent apps (this may not work on all devices)
                        activity.moveTaskToBack(false);
                    } catch (Exception e) {
                        // Fallback
                        activity.moveTaskToBack(true);
                    }
                    hideNavigationBar();
                });
            }
            
            // Initially hidden
            customNavBar.setVisibility(View.GONE);
        }
    }
    
    /**
     * Setup gesture detection for swipe-up from bottom edge
     */
    private void setupGestureDetection() {
        View rootView = window.getDecorView();
        View contentView = rootView.findViewById(android.R.id.content);
        
        // Setup gesture detector
        gestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null) {
                    float deltaY = e1.getY() - e2.getY();
                    float deltaX = Math.abs(e1.getX() - e2.getX());
                    
                    // Check if swipe is upward from bottom edge
                    float screenHeight = rootView.getHeight();
                    float bottomEdgeThreshold = screenHeight - 
                        (activity.getResources().getDisplayMetrics().density * 80); // Bottom 80dp
                    
                    boolean isUpwardSwipe = deltaY > swipeThresholdPx;
                    boolean isVerticalSwipe = deltaX < Math.abs(deltaY);
                    boolean isFastEnough = Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD;
                    boolean isFromBottom = e1.getY() >= bottomEdgeThreshold;
                    
                    if (isUpwardSwipe && isVerticalSwipe && isFastEnough && isFromBottom) {
                        showNavigationBar();
                        return true;
                    }
                }
                return false;
            }
            
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
        
        // Create a simple touch listener for the content view
        if (contentView != null) {
            contentView.setOnTouchListener((v, event) -> {
                float screenHeight = v.getHeight();
                float bottomEdgeThreshold = screenHeight - 
                    (activity.getResources().getDisplayMetrics().density * 80);
                
                // Check if touch is in bottom edge area
                if (event.getY() >= bottomEdgeThreshold) {
                    // Try gesture detector first
                    if (gestureDetector.onTouchEvent(event)) {
                        return true;
                    }
                    
                    // Also handle simple upward swipe
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Object tag = v.getTag();
                        if (tag instanceof MotionEvent) {
                            MotionEvent downEvent = (MotionEvent) tag;
                            float deltaY = downEvent.getY() - event.getY();
                            
                            if (deltaY > swipeThresholdPx && downEvent.getY() >= bottomEdgeThreshold) {
                                showNavigationBar();
                                return true;
                            }
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        // Store down event
                        MotionEvent downEvent = MotionEvent.obtain(event);
                        v.setTag(downEvent);
                    }
                }
                
                return false;
            });
        }
    }
    
    /**
     * Show custom navigation bar
     */
    public void showNavigationBar() {
        if (customNavBar != null && !isNavBarVisible) {
            isNavBarVisible = true;
            customNavBar.setVisibility(View.VISIBLE);
            
            // Cancel any pending auto-hide
            cancelAutoHide();
            
            // Schedule auto-hide
            scheduleAutoHide();
        }
    }
    
    /**
     * Hide custom navigation bar
     */
    public void hideNavigationBar() {
        if (customNavBar != null && isNavBarVisible) {
            isNavBarVisible = false;
            customNavBar.setVisibility(View.GONE);
            cancelAutoHide();
        }
    }
    
    /**
     * Schedule automatic hiding of navigation bar
     */
    private void scheduleAutoHide() {
        cancelAutoHide();
        autoHideRunnable = () -> hideNavigationBar();
        autoHideHandler.postDelayed(autoHideRunnable, AUTO_HIDE_DELAY_MS);
    }
    
    /**
     * Cancel automatic hiding
     */
    private void cancelAutoHide() {
        if (autoHideRunnable != null) {
            autoHideHandler.removeCallbacks(autoHideRunnable);
            autoHideRunnable = null;
        }
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        cancelAutoHide();
        if (customNavBar != null && customNavBar.getParent() != null) {
            ((ViewGroup) customNavBar.getParent()).removeView(customNavBar);
        }
        if (bottomEdgeDetector != null && bottomEdgeDetector.getParent() != null) {
            ((ViewGroup) bottomEdgeDetector.getParent()).removeView(bottomEdgeDetector);
        }
        
        // Clean up touch listeners
        View contentView = window.getDecorView().findViewById(android.R.id.content);
        if (contentView != null) {
            contentView.setOnTouchListener(null);
            contentView.setTag(null);
        }
    }
}
