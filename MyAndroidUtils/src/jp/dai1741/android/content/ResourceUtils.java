package jp.dai1741.android.content;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

/**
 * リソースを扱うためのユーティリティクラス。
 * 
 */
public class ResourceUtils {
    public static final String rootNs = "http://schemas.android.com/apk/res/";
    public static final String thisNs = rootNs + "jp.dai1741.android";

    /**
     * 対応する属性のリソースIDを返す。
     * この関数はandroid sdk 12のライブラリプロジェクトのxml属性に関するバグを正しく処理し、
     * バグが修正された場合も正しく動作すると考えられる。
     * （現時点での最新版のsdk 14でもバグは修正されていない）
     * 外部プロジェクトに{@code attribute}と同名の属性名が使用されていた場合、
     * この関数は誤った値を返すことがある。
     * この関数はこのライブラリ内で定義した属性以外は正しく認識しない。
     * 
     * @param set
     * @param context
     *            外部プロジェクトのコンテキスト
     * @param attribute
     *            属性名
     * @return リソースID。存在しなければ0
     * @see {@link http://code.google.com/p/android/issues/detail?id=9656}
     * @see {@link http://ccl.c-lis.co.jp/modules/d3blog/details.php?bid=126}
     * @see AttributeSet#getAttributeResourceValue(String, String, int)
     */
    public static int getSpecificAttributeResourceValue(AttributeSet set,
            Context context, String attribute) {
        String contextNs = rootNs + context.getPackageName();
        int ret = set.getAttributeResourceValue(thisNs, attribute, 0);
        if (ret == 0) {
            ret = set.getAttributeResourceValue(contextNs, attribute, 0);
        }
        return ret;
    }

    /**
     * 属性値を返す。
     * 
     * @return 属性値を表す未処理の文字列。存在しなければnull
     * @see #getObtainedSpecificAttributeStringValue(AttributeSet, Context,
     *      String)
     * @see AttributeSet#getAttributeValue(String, String)
     */
    public static String getSpecificAttributeValue(AttributeSet set,
            Context context, String attribute) {
        String contextNs = rootNs + context.getPackageName();
        String ret = set.getAttributeValue(thisNs, attribute);
        if (ret == null) {
            ret = set.getAttributeValue(contextNs, attribute);
        }
        return ret;
    }

    /**
     * 解決済みの属性値を返す。
     * 
     * @return 属性値を表す文字列。存在しなければnull
     * @see #getSpecificAttributeResourceValue(AttributeSet, Context, String)
     * @see Resources.Theme#obtainStyledAttributes(AttributeSet, int[], int,
     *      int)
     */
    public static String getObtainedSpecificAttributeStringValue(
            AttributeSet set, Context context, String attribute) {
        int resId = getSpecificAttributeResourceValue(set, context, attribute);
        if (resId != 0) {
            return context.getResources().getString(resId);
        }
        return getSpecificAttributeValue(set, context, attribute);
    }


}
