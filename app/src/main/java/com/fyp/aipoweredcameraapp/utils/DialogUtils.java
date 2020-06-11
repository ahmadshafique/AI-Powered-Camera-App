package com.fyp.aipoweredcameraapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

import com.fyp.aipoweredcameraapp.R;

public class DialogUtils {

    private Activity activity;

    public DialogUtils(Activity activity) {
        this.activity = activity;
    }

    private Dialog buildDialogView(@LayoutRes int layout) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        return dialog;
    }


    public Dialog buildDialogInfo(@StringRes int title, @StringRes int content, @StringRes int bt_text_pos, @DrawableRes int icon, final CallbackDialog2Buttons callback) {
        return buildDialogInfo(activity.getString(title), activity.getString(content), activity.getString(bt_text_pos), icon, callback);
    }

    // dialog info
    public Dialog buildDialogInfo(String title, String content, String bt_text_pos, @DrawableRes int icon, final CallbackDialog2Buttons callback) {
        final Dialog dialog = buildDialogView(R.layout.dialog_info);

        ((TextView) dialog.findViewById(R.id.title)).setText(title);
        ((TextView) dialog.findViewById(R.id.content)).setText(content);
        ((Button) dialog.findViewById(R.id.bt_positive)).setText(bt_text_pos);
        ((ImageView) dialog.findViewById(R.id.icon)).setImageResource(icon);

        ((Button) dialog.findViewById(R.id.bt_positive)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onPositiveClick(dialog);
            }
        });
        return dialog;
    }

    public Dialog buildDialogWarning(@StringRes int title, @StringRes int content, @StringRes int bt_text_pos, @StringRes int bt_text_neg, @DrawableRes int icon, final CallbackDialog2Buttons callback) {
        String _title = null;
        String _content = null;
        String _bt_text_neg = null;

        if (title != -1) _title = activity.getString(title);
        if (content != -1) _content = activity.getString(content);
        if (bt_text_neg != -1) _bt_text_neg = activity.getString(bt_text_neg);

        return buildDialogWarning(_title, _content, activity.getString(bt_text_pos), _bt_text_neg, icon, callback);
    }

    public Dialog buildDialogWarning(@StringRes int title, @StringRes int content, @StringRes int bt_text_pos, @DrawableRes int icon, final CallbackDialog2Buttons callback) {
        String _title = null;
        String _content = null;

        if (title != -1) _title = activity.getString(title);
        if (content != -1) _content = activity.getString(content);

        return buildDialogWarning(_title, _content, activity.getString(bt_text_pos), null, icon, callback);
    }

    // dialog warning
    public Dialog buildDialogWarning(String title, String content, String bt_text_pos, String bt_text_neg, @DrawableRes int icon, final CallbackDialog2Buttons callback) {
        final Dialog dialog = buildDialogView(R.layout.dialog_warning);

        // if id = -1 view will gone
        if (title != null) {
            ((TextView) dialog.findViewById(R.id.title)).setText(title);
        } else {
            ((TextView) dialog.findViewById(R.id.title)).setVisibility(View.GONE);
        }

        // if id = -1 view will gone
        if (content != null) {
            ((TextView) dialog.findViewById(R.id.content)).setText(content);
        } else {
            ((TextView) dialog.findViewById(R.id.content)).setVisibility(View.GONE);
        }
        ((Button) dialog.findViewById(R.id.bt_positive)).setText(bt_text_pos);
        if (bt_text_neg != null) {
            ((Button) dialog.findViewById(R.id.bt_negative)).setText(bt_text_neg);
        } else {
            ((Button) dialog.findViewById(R.id.bt_negative)).setVisibility(View.GONE);
        }
        ((ImageView) dialog.findViewById(R.id.icon)).setImageResource(icon);

        ((Button) dialog.findViewById(R.id.bt_positive)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onPositiveClick(dialog);
            }
        });

        ((Button) dialog.findViewById(R.id.bt_negative)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onNegativeClick(dialog);
            }
        });
        return dialog;
    }

    public Dialog buildDialogSelection(@StringRes int title, @StringRes int content, @StringRes int bt_option_1, @StringRes int bt_option_2, @StringRes int bt_close, @DrawableRes int icon, final CallbackDialog2Buttons callback) {
        String _title = null;
        String _content = null;
        String _bt_close = null;

        if (title != -1) _title = activity.getString(title);
        if (content != -1) _content = activity.getString(content);
        if (bt_close != -1) _bt_close = activity.getString(bt_close);

        return buildDialogSelection(_title, _content, activity.getString(bt_option_1), activity.getString(bt_option_2), _bt_close, icon, callback);
    }


    // dialog selection
    public Dialog buildDialogSelection(String title, String content, String bt_option_1, String bt_option_2, String bt_close, @DrawableRes int icon, final CallbackDialog2Buttons callback) {
        final Dialog dialog = buildDialogView(R.layout.dialog_selection);

        // if id = -1 view will gone
        if (title != null) {
            ((TextView) dialog.findViewById(R.id.title)).setText(title);
        } else {
            ((TextView) dialog.findViewById(R.id.title)).setVisibility(View.GONE);
        }

        // if id = -1 view will gone
        if (content != null) {
            ((TextView) dialog.findViewById(R.id.content)).setText(content);
        } else {
            ((TextView) dialog.findViewById(R.id.content)).setVisibility(View.GONE);
        }
        ((Button) dialog.findViewById(R.id.bt_opt_1)).setText(bt_option_1);
        ((Button) dialog.findViewById(R.id.bt_opt_2)).setText(bt_option_2);


        if (bt_close != null) {
            ((Button) dialog.findViewById(R.id.bt_close)).setText(bt_close);
        } else {
            ((Button) dialog.findViewById(R.id.bt_close)).setVisibility(View.GONE);
        }
        ((ImageView) dialog.findViewById(R.id.icon)).setImageResource(icon);

        ((Button) dialog.findViewById(R.id.bt_opt_1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onPositiveClick(dialog);
            }
        });

        ((Button) dialog.findViewById(R.id.bt_opt_2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onNegativeClick(dialog);
            }
        });

        ((Button) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //callback.onCloseClick(dialog);
            }
        });

        return dialog;
    }

    // dialog facial features options
    public Dialog buildDialogFacialFeatures(String title, String content, String bt_option_1, String bt_option_2, String bt_option_3, String bt_option_4, String bt_close, final CallbackDialog4Buttons callback) {
        final Dialog dialog = buildDialogView(R.layout.dialog_facial_features);

        // if id = -1 view will gone
        if (title != null) {
            ((TextView) dialog.findViewById(R.id.title)).setText(title);
        } else {
            ((TextView) dialog.findViewById(R.id.title)).setVisibility(View.GONE);
        }

        // if id = -1 view will gone
        if (content != null) {
            ((TextView) dialog.findViewById(R.id.content)).setText(content);
        } else {
            ((TextView) dialog.findViewById(R.id.content)).setVisibility(View.GONE);
        }
        ((Button) dialog.findViewById(R.id.bt_opt_1)).setText(bt_option_1);
        ((Button) dialog.findViewById(R.id.bt_opt_2)).setText(bt_option_2);
        ((Button) dialog.findViewById(R.id.bt_opt_3)).setText(bt_option_3);
        ((Button) dialog.findViewById(R.id.bt_opt_4)).setText(bt_option_4);

        if (bt_close != null) {
            ((Button) dialog.findViewById(R.id.bt_close)).setText(bt_close);
        } else {
            ((Button) dialog.findViewById(R.id.bt_close)).setVisibility(View.GONE);
        }

        ((Button) dialog.findViewById(R.id.bt_opt_1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onButton1Click(dialog);
            }
        });

        ((Button) dialog.findViewById(R.id.bt_opt_2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onButton2Click(dialog);
            }
        });

        ((Button) dialog.findViewById(R.id.bt_opt_3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onButton3Click(dialog);
            }
        });

        ((Button) dialog.findViewById(R.id.bt_opt_4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onButton4Click(dialog);
            }
        });

        ((Button) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //callback.onCloseClick(dialog);
            }
        });

        return dialog;
    }


    // dialog selfie manipulation parameters
    public Dialog buildDialogSelfieManipulation(String title, String content, String bt_option_1, String bt_option_2, final CallbackDialog2Buttons callback) {
        final Dialog dialog = buildDialogView(R.layout.dialog_selfie_manipulation);

        // if id = -1 view will gone
        if (title != null) {
            ((TextView) dialog.findViewById(R.id.title)).setText(title);
        } else {
            ((TextView) dialog.findViewById(R.id.title)).setVisibility(View.GONE);
        }

        // if id = -1 view will gone
        if (content != null) {
            ((TextView) dialog.findViewById(R.id.content)).setText(content);
        } else {
            ((TextView) dialog.findViewById(R.id.content)).setVisibility(View.GONE);
        }

        ((Button) dialog.findViewById(R.id.bt_positive)).setText(bt_option_1);
        ((Button) dialog.findViewById(R.id.bt_negative)).setText(bt_option_2);

        ((Button) dialog.findViewById(R.id.bt_positive)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onPositiveClick(dialog);
            }
        });

        ((Button) dialog.findViewById(R.id.bt_negative)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onNegativeClick(dialog);
            }
        });

        return dialog;
    }

}
