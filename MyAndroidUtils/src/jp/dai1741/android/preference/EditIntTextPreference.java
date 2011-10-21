package jp.dai1741.android.preference;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

/**
 * intのみ受け付ける{@link EditTextPreference}。
 * 
 * @author dai
 * 
 */
public class EditIntTextPreference extends EditTextPreference {

    protected static final InputFilter INT_LENGTH_FILTER = new InputFilter.LengthFilter(
            11);

    // 11 == Math.ceil(Integer.SIZE * Math.log10(2)) + 1 == intの10進数表現の最大桁数+1

    public EditIntTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditIntTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditIntTextPreference(Context context) {
        super(context);
    }

    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        super.onAddEditTextToDialogView(dialogView, editText);
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        // このフィルタで入力は-100億超1000億未満の整数になる
        // その後closeの段階でintに処理する
        editText.setFilters(new InputFilter[] {
                DigitsKeyListener.getInstance(true, false), INT_LENGTH_FILTER });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            try {
                CharSequence origText = getEditText().getText();
                CharSequence text = origText;
                while (true) {
                    long data = Long.parseLong(text.toString());
                    if (Integer.MIN_VALUE <= data && data <= Integer.MAX_VALUE) {
                        break;
                    }
                    text = text.subSequence(0, text.length() - 1);
                }
                if (origText.length() != text.length()) {
                    getEditText().setText(text);
                }
                super.onDialogClosed(true);
            }
            catch (NumberFormatException e) {
                // 制約上発生しないはず
                throw new AssertionError("unexpected NumberFormatException has occured");
            }
        }
        super.onDialogClosed(false);
    }


}
