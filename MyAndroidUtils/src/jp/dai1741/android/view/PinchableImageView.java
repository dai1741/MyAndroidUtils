package jp.dai1741.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


/**
 * ドラッグで画像を動かしたり、つまんで拡大縮小ができるビュー。
 * クラス名とは裏腹にandroid.widget.ImageViewを継承していない。
 * 
 * TODO: 色々動作がおかしいので要修正。
 * 
 * @author dai
 * 
 */
public class PinchableImageView extends View {
    protected Bitmap mBitMap;

    protected float mMapX;
    protected float mMapY;
    protected float mTouchCurrX;
    protected float mTouchCurrY;
    protected Paint mPaint = new Paint();
    protected int mAlphaChangeRate = 255;
    protected ZoomState mZoomState;

    public PinchableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(null);
    }

    public PinchableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }

    public PinchableImageView(Context context) {
        this(context, (Bitmap) null);
    }

    /**
     * @param context
     * @param bitmap
     *            　null可
     */
    public PinchableImageView(Context context, Bitmap bitmap) {
        this(context, bitmap, ZoomState.FIT_TO_SHORT_WIDTH);
    }

    public PinchableImageView(Context context, Bitmap bitmap, ZoomState initZoom) {
        super(context);
        init(bitmap, initZoom);
    }

    private void init(Bitmap bitmap) {
        init(bitmap, ZoomState.FIT_TO_SHORT_WIDTH);
    }

    private void init(Bitmap bitmap, ZoomState initZoom) {

        mZoomState = initZoom;

        setFocusable(true);
        setFocusableInTouchMode(true);
        mBitMap = bitmap != null ? bitmap
                : EMPTY_BITMAP; // ダミー
        mPaint.setFilterBitmap(true);
        mPaint.setAntiAlias(true);
        invalidate();

    }

    static final int HAMIDASHI_HOSEI = 6;
    static final float ZOOM_HOSEI = 1.05f;

    RectF mLazyShowngAroundRect;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initView();
    }

    private void initView() {
        fitView();
    }

    public void fitImageTo(ZoomState state) {
        mZoom = state.calculateZoomRate(this, mBitMap);

        makeImageInDisplay();
        invalidate();
    }

    private void makeImageInDisplay() {
        RectF bounds = getBounds();
        mMapX += bounds.left;
        mMapY += bounds.top;

        if (mLazyShowngAroundRect != null) {
            showAround(mLazyShowngAroundRect);
            mLazyShowngAroundRect = null;
        }

    }

    private void fitView() {

        mZoomCenterX = (float) getWidth() / 2;
        mZoomCenterY = (float) getHeight() / 2;

        fitImageTo(mZoomState);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.scale(mZoom, mZoom, mZoomCenterX, mZoomCenterY);
        canvas.translate(mMapX, mMapY);
        canvas.drawBitmap(mBitMap, 0, 0, mPaint);

        // if (mOverlays != null)
        // for (OverlayRect obj : mOverlays) {
        // obj.draw(canvas, mPaint);
        // }

        if (!mOnTouching) {
            if (fixOrverRun()) {
                invalidate();
            }
            int alpha = mPaint.getAlpha();
            if (alpha < 255) {
                mPaint.setAlpha(Math.min(255, alpha + mAlphaChangeRate));
                invalidate();
            }
        }
    }

    /**
     * はみ出した画像を滑らかに修繕する。
     * 
     * @return whether or not the clipped region is modified
     */
    private boolean fixOrverRun() {
        boolean modded = false;
        RectF bounds = getBounds();
        float hosei = 1; // これがないと拡大率によってはプルプルするよ！

        if ((bounds.left < -hosei)
                ^ (mBitMap.getWidth() < bounds.right - hosei)) {
            if (bounds.left < -hosei) {
                mMapX += Math.floor(bounds.left / HAMIDASHI_HOSEI);
            }
            else {
                mMapX += Math.ceil((bounds.right - mBitMap.getWidth())
                        / HAMIDASHI_HOSEI);
            }
            modded = true;
        }
        if ((bounds.top < -hosei)
                ^ (mBitMap.getHeight() < bounds.bottom - hosei)) {
            if (bounds.top < -hosei) {
                mMapY += Math.floor(bounds.top / HAMIDASHI_HOSEI);
            }
            else {
                mMapY += Math.ceil((bounds.bottom - mBitMap.getHeight())
                        / HAMIDASHI_HOSEI);
            }
            modded = true;
        }
        if (mZoom < 1 && getWidth() > mBitMap.getWidth() * mZoom
                && getHeight() > mBitMap.getHeight() * mZoom) {
            mZoom = Math.min(1, mZoom * ZOOM_HOSEI);
            modded = true;
        }

        return modded;

    }

    boolean mIsPinching;
    boolean mTouchOperationFinished;
    boolean mOnTouching;
    int mTouchMovedCount;
    float mZoom = 1;
    float mZoomCenterX;
    float mZoomCenterY;
    float mPinchStartDistance = 0;
    float mPinchStartZoom = 1;

    long mLastClickTime;
    long mTouchStartTime;
    static final int CLICK_TLERANCE = 3;
    static final int MAX_CLICK_TIME = 150;

    /**
     * {@inheritDoc} TODO: {@link GestureDetector}使う
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean willDraw = false;

        int pointerIndex = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
        // ACTION_POINTER_ID_SHIFT is actually a shift for index, so s
        // deprecated on api lv8

        switch (event.getAction() & ~MotionEvent.ACTION_POINTER_ID_MASK) {
        case MotionEvent.ACTION_DOWN:
            mTouchCurrX = x;
            mTouchCurrY = y;
            mTouchOperationFinished = false;
            mIsPinching = false;
            mTouchMovedCount = 0;
            mOnTouching = true;
            mTouchStartTime = System.currentTimeMillis();
            break;
        case MotionEvent.ACTION_POINTER_DOWN:
            if (!mTouchOperationFinished && pointerIndex == 1) {
                mIsPinching = true;
                mPinchStartDistance = (float) Math.hypot(x - event.getX(1),
                        y - event.getY(1));
                mPinchStartZoom = mZoom;
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (!mTouchOperationFinished) {
                if (!mIsPinching) {
                    mMapX += (x - mTouchCurrX) / (mZoom);
                    mMapY += (y - mTouchCurrY) / (mZoom);
                    mTouchCurrX = x;
                    mTouchCurrY = y;
                }
                else {
                    // assert both 0 and 1 are available for pointerIndex
                    float dist = (float) Math.hypot(x - event.getX(1),
                            y - event.getY(1));
                    mZoom = mPinchStartZoom
                            * (float) ((dist / mPinchStartDistance));
                    // zoomcenterは固定値に
                    // mZoomCenterX = (x + event.getX(1)) / 2; //
                    // 開始時の中点座標を記憶するべき？
                    // mZoomCenterY = (y + event.getY(1)) / 2;
                }
                willDraw = true;
            }
            mTouchMovedCount++;
            break;
        case MotionEvent.ACTION_UP:
            mIsPinching = false;
            mOnTouching = false;
            if (mTouchMovedCount < CLICK_TLERANCE
                    && System.currentTimeMillis() - mTouchStartTime < MAX_CLICK_TIME) {
                // クリックされた!!! (条件は変えるべき)
                if (!onClick()) {
                    performClick();
                }
            }
            willDraw = true;
            break;
        case MotionEvent.ACTION_POINTER_UP:
            if (pointerIndex == 0) mTouchOperationFinished = true;
            if (pointerIndex <= 1) mIsPinching = false;
            mTouchMovedCount++;
            break;
        }
        if (willDraw) {
            invalidate();
        }
        return true;
    }

    static final int DBLCLICK_TIME = 400;
    int mClickCount;

    /**
     * @return whether or not the click event is consumed
     */
    private boolean onClick() {
        boolean ret = false;
        long now = System.currentTimeMillis();
        if (now < mLastClickTime + DBLCLICK_TIME) {
            mClickCount++;
            if (mClickCount == 2) {
                // ダブルクリック!!!
                // concatZoom(1.3f);
                toggleZoomState(1);
                ret = true;
            }
        }
        else {
            mClickCount = 1;
        }
        mLastClickTime = now;
        return ret;
    }

    public ZoomState getZoomState() {
        return mZoomState;
    }

    public void setZoomState(ZoomState zoomState) {
        if (zoomState == null) throw new NullPointerException();
        updateZoomState(zoomState);
    }

    private void updateZoomState(ZoomState zoomState) {
        fitImageTo(mZoomState = zoomState);
    }

    /**
     * @param shift
     *            ずらす量
     */
    private void toggleZoomState(int shift) {
        ZoomState[] states = ZoomState.values();
        updateZoomState(states[(mZoomState.ordinal() + shift % states.length + states.length)
                % states.length]);
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint paint) {
        if (paint == null) throw new NullPointerException();
        mPaint = paint;
    }

    static final float RATIO_SHOWING_AROUND = 3f;

    /**
     * 
     * TODO: 仕様を定める
     * 
     * @param bound
     *            注目する対象の大きさ。この対象に加えていくらかの余白も表示する
     */
    public void showAround(RectF bound) {
        if (bound == null) {
            return;
        }
        if (getWidth() == 0) {
            mLazyShowngAroundRect = bound;
            return;
        }
        float viewRatio = (float) getWidth() / getHeight();

        float requiredWidth = (bound.right - bound.left)
                * RATIO_SHOWING_AROUND * viewRatio;
        float requiredHeight = (bound.bottom - bound.top)
                * RATIO_SHOWING_AROUND / viewRatio;
        // if (viewRatio < requiredWidth / requiredHeight) {
        // requiredWidth = Math.max(requiredHeight, requiredWidth
        // / RATIO_LONG_SHOWING_AROUND * RATIO_SHORT_SHOWING_AROUND);
        // //緩和措置
        // }
        // else {
        // requiredHeight = Math.max(requiredWidth, requiredHeight
        // / RATIO_LONG_SHOWING_AROUND * RATIO_SHORT_SHOWING_AROUND);
        // }
        if (viewRatio < requiredWidth / requiredHeight) {
            // 画面の比率よりも要求横幅の比率が大きいとき
            requiredHeight = requiredWidth / viewRatio;
        }
        else {
            requiredWidth = requiredHeight * viewRatio;
        }
        mZoom = getWidth() / requiredWidth;
        float rate = 1 - 1 / mZoom;
        float left = (bound.right + bound.left) / 2 - requiredWidth / 2;
        mMapX = -left + rate * mZoomCenterX;
        float top = (bound.bottom + bound.top) / 2 - requiredHeight / 2;
        mMapY = -top + rate * mZoomCenterY;

        invalidate();
    }

    /**
     * @return bitmap
     */
    public Bitmap getBitmap() {
        return mBitMap;
    }

    /**
     * setBitmap(bitmap, false)と等価
     * 
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        setBitmap(bitmap, false);
    }

    /**
     * @param bitmap
     *            non-null
     * @param recycleOld
     *            trueなら古いbitmapをリサイクル
     */
    public void setBitmap(Bitmap bitmap, boolean recycleOld) {
        if (bitmap == null) throw new NullPointerException();
        if (mBitMap == bitmap) return;
        if (recycleOld) mBitMap.recycle();
        mBitMap = bitmap;
        mPaint.setAlpha(0); // かなり疑わしい
        initView();

    }

    protected static final Bitmap EMPTY_BITMAP = Bitmap.createBitmap(Bitmap
            .createBitmap(1, 1, Bitmap.Config.ALPHA_8));

    public static Bitmap createEmptyBitmap() {
        return EMPTY_BITMAP.copy(Bitmap.Config.ALPHA_8, false);
    }

    public float getZoomRate() {
        return mZoom;
    }

    public int getAlphaChangeRate() {
        return mAlphaChangeRate;
    }

    /**
     * 画像が表示時にフェードインする速さを設定。
     * 1フレームごとに(alphaChangeRate/255)パーセントだけ画像の不透明率が上がる。
     * 
     * @param alphaChangeRate
     *            0以上255以下。255設定時は実質フェードインなし。範囲外の数値は矯正される
     */
    public void setAlphaChangeRate(int alphaChangeRate) {
        mAlphaChangeRate = Math.max(0, Math.min(alphaChangeRate, 255));
    }

    public PointF getLastTouchPointF() {
        return new PointF(mTouchCurrX, mTouchCurrY);
    }

    /**
     * 表示範囲を返す。
     * 
     * @return 表示範囲
     */
    public RectF getBounds() {
        float rate = 1 - 1 / mZoom;
        return new RectF(-mMapX + rate * mZoomCenterX,
                -mMapY + rate * mZoomCenterY,
                -mMapX + getWidth() / mZoom + rate * mZoomCenterX,
                -mMapY + getHeight() / mZoom + rate * mZoomCenterY);

    }


    public static enum ZoomState {
        /**
         * 画面内に収める
         */
        FIT_TO_WINDOW {
            @Override
            public float calculateZoomRate(View v, Bitmap b) {
                return Math.min((float) v.getWidth() / b.getWidth(),
                        (float) v.getHeight() / b.getHeight());
            }
        },
        // FIT_TO_WIDTH {
        // @Override
        // public float calculateZoomRate(View v, Bitmap b) {
        // return (float) v.getWidth() / b.getWidth();
        // }
        // },
        /**
         * 画面幅に合わせる
         */
        FIT_TO_SHORT_WIDTH {
            @Override
            public float calculateZoomRate(View v, Bitmap b) {
                return Math.max((float) v.getWidth() / b.getWidth(),
                        (float) v.getHeight() / b.getHeight());
            }
        },
        /**
         * 等倍
         */
        NO_ZOOM {
            @Override
            public float calculateZoomRate(View v, Bitmap b) {
                return 1;
            }
        };

        /**
         * {@link Bitmap}の{@link View}に対する表示サイズを返す。
         * 
         * @param v
         * @param b
         * @return 倍率
         */
        abstract public float calculateZoomRate(View v, Bitmap b);
    }

}
