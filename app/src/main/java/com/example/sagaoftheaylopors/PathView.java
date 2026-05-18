package com.example.sagaoftheaylopors;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom view for drawing animated paths between Points of Interest (POIs) on the map.
 * Supports smooth path animation with glow effects, dashed lines, and multiple paths.
 */
public class PathView extends View {
    // Path styling
    private static final int PATH_STROKE_WIDTH = 8;
    private static final int GLOW_STROKE_WIDTH = 16;
    private static final int PATH_COLOR_ORANGE = 0xFFFF9800; // Orange for new paths
    private static final int PATH_COLOR_GREEN = 0xFF4CAF50; // Green for completed paths
    private static final int GLOW_COLOR_ORANGE = 0x40FF9800; // Orange glow with transparency
    private static final int GLOW_COLOR_GREEN = 0x404CAF50; // Green glow with transparency
    private static final float[] DASH_PATTERN = new float[]{16, 8}; // Dash pattern: 16px dash, 8px gap
    
    // Single path (legacy support)
    private Paint pathPaint;
    private Paint glowPaint;
    private Path path;
    private PathMeasure pathMeasure;
    private float pathLength;
    private float animatedLength;
    private ValueAnimator pathAnimator;
    private boolean isAnimating = false;
    private boolean isPathVisible = false;
    
    // Multiple paths support
    public static class PathData {
        public Path path;
        public PathMeasure pathMeasure;
        public float pathLength;
        public float animatedLength;
        public Paint pathPaint;
        public Paint glowPaint;
        public boolean isCompleted; // true = green, false = orange
        public boolean isAnimating;
        public ValueAnimator animator;
        
        public PathData(Path path, boolean isCompleted) {
            this.path = path;
            this.isCompleted = isCompleted;
            this.pathMeasure = new PathMeasure(path, false);
            this.pathLength = pathMeasure.getLength();
            this.animatedLength = 0;
            this.isAnimating = false;
            
            // Create paints based on completion status
            this.pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.pathPaint.setStyle(Paint.Style.STROKE);
            this.pathPaint.setStrokeWidth(PATH_STROKE_WIDTH);
            this.pathPaint.setStrokeCap(Paint.Cap.ROUND);
            this.pathPaint.setStrokeJoin(Paint.Join.ROUND);
            
            this.glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.glowPaint.setStyle(Paint.Style.STROKE);
            this.glowPaint.setStrokeWidth(GLOW_STROKE_WIDTH);
            this.glowPaint.setStrokeCap(Paint.Cap.ROUND);
            this.glowPaint.setStrokeJoin(Paint.Join.ROUND);
            
            if (isCompleted) {
                // Completed path: solid green line
                this.pathPaint.setColor(PATH_COLOR_GREEN);
                this.glowPaint.setColor(GLOW_COLOR_GREEN);
                this.pathPaint.setPathEffect(null); // Solid line
            } else {
                // New path: dashed orange line
                this.pathPaint.setColor(PATH_COLOR_ORANGE);
                this.glowPaint.setColor(GLOW_COLOR_ORANGE);
                this.pathPaint.setPathEffect(new DashPathEffect(DASH_PATTERN, 0)); // Dashed line
            }
        }
    }
    
    private List<PathData> paths = new ArrayList<>();
    
    public PathView(Context context) {
        super(context);
        init();
    }
    
    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public PathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Legacy single path support
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(PATH_STROKE_WIDTH);
        pathPaint.setColor(PATH_COLOR_ORANGE);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setPathEffect(new DashPathEffect(DASH_PATTERN, 0)); // Dashed by default
        
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(GLOW_STROKE_WIDTH);
        glowPaint.setColor(GLOW_COLOR_ORANGE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setStrokeJoin(Paint.Join.ROUND);
        
        path = new Path();
        pathMeasure = new PathMeasure();
        animatedLength = 0;
    }
    
    /**
     * Add a path to be drawn (for multiple paths support).
     * @param path The path to draw
     * @param isCompleted true if this path represents a completed chapter (green), false if new (orange dashed)
     * @return the PathData for this path (can be used for animation)
     */
    public PathData addPath(Path path, boolean isCompleted) {
        PathData pathData = new PathData(path, isCompleted);
        paths.add(pathData);
        invalidate();
        return pathData;
    }
    
    /**
     * Clear all paths.
     */
    public void clearPaths() {
        // Cancel all animations
        for (PathData pathData : paths) {
            if (pathData.animator != null && pathData.animator.isRunning()) {
                pathData.animator.cancel();
            }
        }
        paths.clear();
        invalidate();
    }
    
    /**
     * Animate a specific path.
     * @param pathData The path data to animate
     * @param duration Animation duration in milliseconds
     */
    public void animatePath(PathData pathData, int duration) {
        if (pathData.pathLength <= 0) {
            return;
        }
        
        if (pathData.animator != null && pathData.animator.isRunning()) {
            pathData.animator.cancel();
        }
        
        pathData.isAnimating = true;
        pathData.animatedLength = 0;
        
        pathData.animator = ValueAnimator.ofFloat(0, pathData.pathLength);
        pathData.animator.setDuration(duration);
        pathData.animator.setInterpolator(new AccelerateDecelerateInterpolator());
        pathData.animator.addUpdateListener(animation -> {
            pathData.animatedLength = (float) animation.getAnimatedValue();
            invalidate();
        });
        
        pathData.animator.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
                pathData.isAnimating = true;
            }
            
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                pathData.isAnimating = false;
                pathData.animatedLength = pathData.pathLength;
                invalidate();
            }
            
            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                pathData.isAnimating = false;
            }
            
            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {
            }
        });
        
        pathData.animator.start();
    }
    
    /**
     * Show a specific path immediately without animation.
     */
    public void showPath(PathData pathData) {
        if (pathData.animator != null && pathData.animator.isRunning()) {
            pathData.animator.cancel();
        }
        pathData.isAnimating = false;
        pathData.animatedLength = pathData.pathLength;
        invalidate();
    }
    
    /**
     * Set path between two points with optional curve control points.
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     * @param controlX1 Optional control point 1 X (for curve)
     * @param controlY1 Optional control point 1 Y (for curve)
     * @param controlX2 Optional control point 2 X (for curve)
     * @param controlY2 Optional control point 2 Y (for curve)
     */
    public void setPath(float startX, float startY, float endX, float endY,
                       Float controlX1, Float controlY1, Float controlX2, Float controlY2) {
        path.reset();
        path.moveTo(startX, startY);
        
        if (controlX1 != null && controlY1 != null && controlX2 != null && controlY2 != null) {
            // Cubic bezier curve
            path.cubicTo(controlX1, controlY1, controlX2, controlY2, endX, endY);
        } else if (controlX1 != null && controlY1 != null) {
            // Quadratic bezier curve
            path.quadTo(controlX1, controlY1, endX, endY);
        } else {
            // Straight line
            path.lineTo(endX, endY);
        }
        
        pathMeasure.setPath(path, false);
        pathLength = pathMeasure.getLength();
        animatedLength = 0;
        
        // Use dashed line for legacy single path
        pathPaint.setPathEffect(new DashPathEffect(DASH_PATTERN, 0));
        
        invalidate();
    }
    
    /**
     * Create a Path between two points with optional curve control points.
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     * @param controlX1 Optional control point 1 X (for curve)
     * @param controlY1 Optional control point 1 Y (for curve)
     * @param controlX2 Optional control point 2 X (for curve)
     * @param controlY2 Optional control point 2 Y (for curve)
     * @return A Path object
     */
    public static Path createPath(float startX, float startY, float endX, float endY,
                                  Float controlX1, Float controlY1, Float controlX2, Float controlY2) {
        Path newPath = new Path();
        newPath.moveTo(startX, startY);
        
        if (controlX1 != null && controlY1 != null && controlX2 != null && controlY2 != null) {
            // Cubic bezier curve
            newPath.cubicTo(controlX1, controlY1, controlX2, controlY2, endX, endY);
        } else if (controlX1 != null && controlY1 != null) {
            // Quadratic bezier curve
            newPath.quadTo(controlX1, controlY1, endX, endY);
        } else {
            // Straight line
            newPath.lineTo(endX, endY);
        }
        
        return newPath;
    }
    
    /**
     * Animate path drawing from start to end.
     * @param duration Animation duration in milliseconds
     */
    public void animatePath(int duration) {
        if (pathLength <= 0) {
            return;
        }
        
        if (pathAnimator != null && pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        
        isAnimating = true;
        isPathVisible = true;
        animatedLength = 0;
        
        pathAnimator = ValueAnimator.ofFloat(0, pathLength);
        pathAnimator.setDuration(duration);
        pathAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pathAnimator.addUpdateListener(animation -> {
            animatedLength = (float) animation.getAnimatedValue();
            invalidate();
        });
        
        pathAnimator.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
                isAnimating = true;
            }
            
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isAnimating = false;
                // Keep path visible after animation
                animatedLength = pathLength;
                invalidate();
            }
            
            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                isAnimating = false;
            }
            
            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {
            }
        });
        
        pathAnimator.start();
    }
    
    /**
     * Show path immediately without animation.
     */
    public void showPath() {
        if (pathAnimator != null && pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        isAnimating = false;
        isPathVisible = true;
        animatedLength = pathLength;
        invalidate();
    }
    
    /**
     * Hide path.
     */
    public void hidePath() {
        if (pathAnimator != null && pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        isAnimating = false;
        isPathVisible = false;
        animatedLength = 0;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw all paths (new multi-path support)
        for (PathData pathData : paths) {
            if (pathData.animatedLength > 0) {
                // Draw glow effect
                Path drawPath = new Path();
                pathData.pathMeasure.getSegment(0, pathData.animatedLength, drawPath, true);
                canvas.drawPath(drawPath, pathData.glowPaint);
                
                // Draw main path
                drawPath = new Path();
                pathData.pathMeasure.getSegment(0, pathData.animatedLength, drawPath, true);
                canvas.drawPath(drawPath, pathData.pathPaint);
            }
        }
        
        // Draw legacy single path (for backwards compatibility)
        if (isPathVisible && pathLength > 0 && animatedLength > 0) {
            // Draw glow effect
            Path drawPath = new Path();
            pathMeasure.getSegment(0, animatedLength, drawPath, true);
            canvas.drawPath(drawPath, glowPaint);
            
            // Draw main path
            drawPath = new Path();
            pathMeasure.getSegment(0, animatedLength, drawPath, true);
            canvas.drawPath(drawPath, pathPaint);
        }
    }
    
    public boolean isAnimating() {
        return isAnimating;
    }
    
    public boolean isPathVisible() {
        return isPathVisible;
    }
}
