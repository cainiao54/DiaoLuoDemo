package yaoyuan.diaoluodemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;

/**
 * Created by 20150924 on 2016/3/18.
 */
public class LoveLayout extends RelativeLayout {
    private Drawable a;
    private Drawable b;
    private Drawable c;
    private Drawable d;
    private Drawable[] drawables;
    private Interpolator[] interpolators = {new AccelerateDecelerateInterpolator()//加速减速
            , new AccelerateInterpolator()//加速
            , new LinearInterpolator()
            , new DecelerateInterpolator()//减速
    };

    private int dHeigth;
    private int dWidth;

    private int mHeight;
    private int mWidth;
    private LayoutParams mParams;
    private Random mRandom = new Random();
    private ValueAnimator animator;

    private int pointX;
    private int pointY;

    public LoveLayout(Context context) {
        this(context, null);
    }

    public LoveLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoveLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        a = getResources().getDrawable(R.drawable.a);
        b = getResources().getDrawable(R.drawable.b);
        c = getResources().getDrawable(R.drawable.c);
        d = getResources().getDrawable(R.drawable.d);
        drawables = new Drawable[4];
        drawables[0] = a;
        drawables[1] = b;
        drawables[2] = c;
        drawables[3] = d;

        //得到drawable的宽高
        dHeigth = a.getIntrinsicHeight();
        dWidth = a.getIntrinsicWidth();
        //初始化Params
        mParams = new LayoutParams(dWidth, dHeigth);
//        mParams.addRule(CENTER_HORIZONTAL, TRUE);//父容器水平居中
        mParams.addRule(ALIGN_PARENT_TOP, TRUE);//
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
    }

    public void addLove() {
        final ImageView iv = new ImageView(getContext());
        iv.setImageDrawable(drawables[mRandom.nextInt(drawables.length)]);
        iv.setLayoutParams(mParams);
        addView(iv);

        //属性动画控制坐标
        final AnimatorSet set = getAnimation(iv);
        //为了性能优化，我们应该在动画消失不见后将ImageView进行回收
        //由于我们只关心动画结束后的状态，所以只重写OnAnimationEnd()方法
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                removeView(iv);
            }
        });
        set.start();
        iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final ImageView iv1 = new ImageView(getContext());
                iv1.setImageDrawable(iv.getDrawable());
                iv1.setLayoutParams(mParams);
                addView(iv1);
                pointX = (int) iv.getX();
                pointY = (int) iv.getY();
                final AnimatorSet set = getAnimation3(iv1);
                //为了性能优化，我们应该在动画消失不见后将ImageView进行回收
                //由于我们只关心动画结束后的状态，所以只重写OnAnimationEnd()方法
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        removeView(iv1);
                    }
                });
                set.start();
                removeView(iv);
            }
        });
    }

    private AnimatorSet getAnimation3(ImageView iv) {
        //缩放动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(iv, "scaleX", 1f, 0.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(iv, "scaleY", 1f, 0.2f);

        ValueAnimator anim = getTransAnimator(iv);
        //添加起始动画集合
        AnimatorSet allAnim = new AnimatorSet();
        allAnim.playTogether(anim, scaleX, scaleY);
        allAnim.setTarget(iv);
        return allAnim;
    }

    private ValueAnimator getTransAnimator(final ImageView iv) {
        Point point1 = new Point(pointX, pointY);
        Point point2 = new Point(mWidth - 100, mHeight - 150);
        ValueAnimator anim = ValueAnimator.ofObject(new PointEvaluator(), point1, point2);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final Point pointf = (Point) animation.getAnimatedValue();
                iv.setX(pointf.x);
                iv.setY(pointf.y);
                iv.setAlpha(1 - animation.getAnimatedFraction());//getAnimatedFraction返回动画进行的百分比，api12以上支持
            }
        });
        anim.setTarget(iv);
        anim.setDuration(1000);
        anim.setInterpolator(interpolators[mRandom.nextInt(interpolators.length)]);
        return anim;
    }

    private AnimatorSet getAnimation(ImageView iv) {
        //贝塞尔曲线动画(不断的修改ImageView的坐标---PointF)
        ValueAnimator bezierValueAnimator = getBezierValueAnimator(iv);
        AnimatorSet bezierSet = new AnimatorSet();
        bezierSet.setTarget(iv);
//        bezierSet.playSequentially(startAnimatorSet, bezierValueAnimator);//将所有动画添加到一起：起始动画---->贝塞尔曲线动画
        bezierSet.playSequentially(bezierValueAnimator);//将所有动画添加到一起：起始动画---->贝塞尔曲线动画
        //需要注意的是当时用AnimatorSet时，尽量不要设置Duration，API会根据所包含的动画集自动计算持续时间
//        bezierSet.setDuration(1000);
        return bezierSet;
    }

    private ValueAnimator getBezierValueAnimator(final ImageView iv) {
        PointF pointf0 = new PointF(mWidth / 2 - dWidth / 2, mHeight - dHeigth);
        PointF pointf1 = new PointF(mRandom.nextInt(mWidth), mRandom.nextInt(mHeight / 2) + mHeight / 2);
        PointF pointf2 = new PointF(mRandom.nextInt(mWidth), mRandom.nextInt(mHeight / 2));
        PointF pointf3 = new PointF(mRandom.nextInt(mWidth), 0);
        //通过贝塞尔曲线公式，自定义估值器
        final BezierEvaluator evaluator = new BezierEvaluator(pointf1, pointf2);
//        final BezierEvaluator evaluator = new BezierEvaluator(pointf2, pointf1);
        //将估值器传入属性动画，不断的修改控件的坐标
//        ValueAnimator animator = ValueAnimator.ofObject(evaluator, pointf0, pointf3);
        animator = ValueAnimator.ofObject(evaluator, pointf3, pointf0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final PointF pointf = (PointF) animation.getAnimatedValue();
                iv.setX(pointf.x);
                iv.setY(pointf.y);
            }
        });
        animator.setTarget(iv);
        animator.setDuration(3000);
        //同样，为了美观我们还可以添加加速度,减速度，弹射等效果(插值器)
//        animator.setInterpolator(interpolators[mRandom.nextInt(interpolators.length)]);

        return animator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public class Point {

        private float x;

        private float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

    }

    public class PointEvaluator implements TypeEvaluator {

        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Point startPoint = (Point) startValue;
            Point endPoint = (Point) endValue;
            float x = startPoint.getX() + fraction * (endPoint.getX() - startPoint.getX());
            float y = startPoint.getY() + fraction * (endPoint.getY() - startPoint.getY());
            Point point = new Point(x, y);
            return point;
        }

    }
}
