package jp.dai1741.android.preference;

import jp.dai1741.android.content.ResourceUtils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 処理前に確認ダイアログを出す{@link DialogPreference}。
 * この{@code Preference}自体は何も設定値を保存しないので、
 * 継承しなければandroid:key属性を設定する必要はない。
 * 
 * @attr ref R.stylable#SimpleDialogPreference_onPositive
 * @attr ref R.stylable#SimpleDialogPreference_onNegative
 * @attr ref R.stylable#SimpleDialogPreference_onCancel
 */
public class SimpleDialogPreference extends DialogPreference {

    public SimpleDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SimpleDialogPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // TypedArray a = context.obtainStyledAttributes(attrs,
        // R.styleable.SimpleDialogPreference);

        // ライブラリプロジェクトでは、aaptのバグにより
        // TypedArrayを使う方法での固有属性取得は不可能(android sdkのバージョンは14)
        // @see http://code.google.com/p/android/issues/detail?id=9656

        // なのでAttributeSetを直接扱う。
        mOnPositiveMethodName = ResourceUtils.getObtainedSpecificAttributeStringValue(
                attrs, context, "onPositive");
        mOnNegativeMethodName = ResourceUtils.getObtainedSpecificAttributeStringValue(
                attrs, context, "onNegative");
        mOnCancelMethodName = ResourceUtils.getObtainedSpecificAttributeStringValue(
                attrs, context, "onCancel");

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        String methodName = which == DialogInterface.BUTTON_POSITIVE
                ? mOnPositiveMethodName
                : which == DialogInterface.BUTTON_NEGATIVE ? mOnNegativeMethodName : null;
        invoke(getContext(), methodName);
    }


    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {

            public void onCancel(DialogInterface dialog) {
                invoke(getContext(), mOnCancelMethodName);
            }
        });
    }

    /**
     * @param context
     *            which has the method to be invoked
     * @param methodName
     *            may be null
     * @throws RuntimeException
     */
    protected void invoke(Context context, String methodName) {
        if (methodName == null) {
            return;
        }
        try {
            Method m = context.getClass().getMethod(methodName,
                    SimpleDialogPreference.class);
            m.invoke(context, this);
        }
        catch (SecurityException e) {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException("no such public method:" + methodName, e);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException("method signature must be: 'public void "
                    + methodName + "(SimpleDialogPreference preference)'", e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException("no permission to invoke method:"
                    + methodName, e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }

    }

    protected String mOnPositiveMethodName;
    protected String mOnNegativeMethodName;
    protected String mOnCancelMethodName;

}
