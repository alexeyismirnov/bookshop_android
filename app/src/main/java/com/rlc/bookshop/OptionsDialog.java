package com.rlc.bookshop;


import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class OptionsDialog extends Dialog
        implements android.view.View.OnClickListener {

    private MainActivity c;
    private Button okButton;
    private RadioButton radioCN, radioHK, radioEN, radioRU;
    private SharedPreferences settings;

    public OptionsDialog(MainActivity a, SharedPreferences settings) {
        super(a);

        this.c = a;
        this.settings = settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.options_dialog);

        ((Toolbar) findViewById(R.id.settings_toolbar)).setTitle(Translate.s("Language"));

        ((TextView) findViewById(R.id.textEN)).setText("English");
        ((TextView) findViewById(R.id.textRU)).setText("Русский");
        ((TextView) findViewById(R.id.textCN)).setText("简体中文");
        ((TextView) findViewById(R.id.textHK)).setText("繁體中文");


        radioEN = (RadioButton) findViewById(R.id.radioEN);
        radioRU = (RadioButton) findViewById(R.id.radioRU);
        radioCN = (RadioButton) findViewById(R.id.radioCN);
        radioHK = (RadioButton) findViewById(R.id.radioHK);
        okButton = (Button) findViewById(R.id.okButton);

        String lang = settings.getString("language", "en");

        if (lang.equals("en"))
            radioEN.setChecked(true);

        else if (lang.equals("zh_cn"))
            radioCN.setChecked(true);

        else if (lang.equals("zh_hk"))
            radioHK.setChecked(true);

        else
            radioRU.setChecked(true);

        radioEN.setOnClickListener(this);
        radioRU.setOnClickListener(this);
        radioCN.setOnClickListener(this);
        radioHK.setOnClickListener(this);
        okButton.setOnClickListener(this);
    }

    private void uncheckRadioButtons() {
        radioEN.setChecked(false);
        radioRU.setChecked(false);
        radioHK.setChecked(false);
        radioCN.setChecked(false);
    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor = settings.edit();

        uncheckRadioButtons();

        switch (v.getId()) {
            case R.id.okButton:
               //  c.updateView();
                dismiss();
                break;

            case R.id.radioEN:
                editor.putString("language", "en");
                radioEN.setChecked(true);
                break;

            case R.id.radioRU:
                editor.putString("language", "ru");
                radioRU.setChecked(true);

                break;

            case R.id.radioCN:
                editor.putString("language", "zh_cn");
                radioCN.setChecked(true);

                break;

            case R.id.radioHK:
                editor.putString("language", "zh_hk");
                radioHK.setChecked(true);
                break;

            default:
                break;
        }

        editor.commit();

        Translate.setLanguage(settings.getString("language", "en"));

    }

}
