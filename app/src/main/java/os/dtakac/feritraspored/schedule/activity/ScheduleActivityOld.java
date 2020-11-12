/*
package os.dtakac.feritraspored.schedule.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import os.dtakac.feritraspored.common.constants.ConstantsKt;
import os.dtakac.feritraspored.common.preferences.PreferenceRepositoryImpl;
import os.dtakac.feritraspored.common.resources.ResourceRepositoryImpl;
import os.dtakac.feritraspored.common.scripts.ScriptProviderImpl;
import os.dtakac.feritraspored.settings.SettingsActivity;
import os.dtakac.feritraspored.views.debounce.DebouncedMenuItemClickListenerOld;
import os.dtakac.feritraspored.views.debounce.DebouncedOnClickListenerOld;
import os.dtakac.feritraspored.R;
import os.dtakac.feritraspored.views.dialog_info.InfoDialogFragment;
import os.dtakac.feritraspored.schedule.presenter.ScheduleContract;
import os.dtakac.feritraspored.schedule.presenter.SchedulePresenter;

public class ScheduleActivityOld extends AppCompatActivity implements ScheduleContract.View {

    @BindView(R.id.wvSchedule) WebView wvSchedule;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btn_navbar_current) ImageButton btnCurrent;
    @BindView(R.id.btn_navbar_next) ImageButton btnNext;
    @BindView(R.id.btn_navbar_previous) ImageButton btnPrevious;
    @BindView(R.id.cl_schedule_status) ConstraintLayout clStatus;
    @BindView(R.id.pbar_schedule_status) ProgressBar pbarStatus;
    @BindView(R.id.iv_schedule_error_status) ImageView ivError;
    @BindView(R.id.tv_schedule_status) TextView tvStatus;
    @BindView(R.id.btn_schedule_bug_report) Button btnBugReport;
    MenuItem btnRefresh;

    private ScheduleContract.Presenter presenter;
    private long debounceThreshold = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        ButterKnife.bind(this);
        initPresenter();
        initViews();
        presenter.onViewCreated();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onViewResumed(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }

    @Override
    protected void onPause() {
        presenter.onViewPaused();
        super.onPause();
    }

    @Override
    public void loadUrl(String url){ wvSchedule.loadUrl(url); }

    @Override
    public void injectJavascript(String script){
        wvSchedule.evaluateJavascript(script, s -> presenter.onJavascriptInjected());
    }

    @Override
    public void setWeekNumber(String script){
        wvSchedule.evaluateJavascript(script, s -> presenter.onWeekNumberReceived(s));
    }

    @Override
    public String getLoadedUrl() { return wvSchedule.getUrl(); }

    @Override
    public void refreshUi() { recreate(); }

    @Override
    public void reloadCurrentPage() { wvSchedule.reload(); }

    @Override
    public void setControlsEnabled(boolean enabled) {
        btnNext.setEnabled(enabled);
        btnPrevious.setEnabled(enabled);
        btnCurrent.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
    }

    @Override
    public void setLoading(boolean loading){
        ivError.setVisibility(View.GONE);
        pbarStatus.setVisibility(View.VISIBLE);
        btnBugReport.setVisibility(View.GONE);
        tvStatus.setText(null);
        clStatus.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showErrorMessage(String message){
        ivError.setVisibility(View.VISIBLE);
        pbarStatus.setVisibility(View.INVISIBLE);
        btnBugReport.setVisibility(View.VISIBLE);
        btnBugReport.setOnClickListener((v) -> sendBugReport(message));
        tvStatus.setText(message);
        clStatus.setVisibility(View.VISIBLE);
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setToolbarTitle(String title){ toolbar.setTitle(title); }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        wvSchedule.setWebViewClient(new ScheduleClient());
        wvSchedule.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    public void showChangelog(){
        InfoDialogFragment.Companion.newInstance(R.string.title_whats_new, R.string.content_whats_new, R.string.dismiss_whats_new)
                .show(getSupportFragmentManager(), ConstantsKt.DIALOG_WHATS_NEW);
    }

    @Override
    public boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void initViews(){
        initToolbar();
        initWebView();
        initNavbar();
    }

    private void initNavbar(){
        btnNext.setOnClickListener(new DebouncedOnClickListenerOld(debounceThreshold) {
            @Override public void onDebouncedClick() { presenter.onClickedNext(); }
        });
        btnCurrent.setOnClickListener(new DebouncedOnClickListenerOld(debounceThreshold) {
            @Override public void onDebouncedClick() { presenter.onClickedCurrent(); }
        });
        btnPrevious.setOnClickListener(new DebouncedOnClickListenerOld(debounceThreshold) {
            @Override public void onDebouncedClick() { presenter.onClickedPrevious(); }
        });
    }

    private void initPresenter(){
        presenter = new SchedulePresenter(
                this,
                new PreferenceRepositoryImpl(new ResourceRepositoryImpl(getResources()), PreferenceManager.getDefaultSharedPreferences(this)),
                new ResourceRepositoryImpl(getResources()),
                new ScriptProviderImpl(getAssets(), new ResourceRepositoryImpl(getResources()))
        );
    }

    private void initToolbar() {
        toolbar.inflateMenu(R.menu.menu);
        btnRefresh = toolbar.getMenu().findItem(R.id.item_menu_refresh);
        btnRefresh.setOnMenuItemClickListener(new DebouncedMenuItemClickListenerOld(debounceThreshold) {
            @Override
            public void onDebouncedClick() { presenter.onRefresh(); }
        });
        toolbar.getMenu().findItem(R.id.item_menu_settings).setOnMenuItemClickListener(item -> {
            startActivity(new Intent(ScheduleActivityOld.this, SettingsActivity.class));
            return true;
        });
        toolbar.getMenu().findItem(R.id.item_menu_openinbrowser).setOnMenuItemClickListener(item -> {
            openUrlInExternalBrowser(getLoadedUrl());
            return true;
        });
    }

    private void sendBugReport(String content){
        Resources r = getResources();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, r.getStringArray(R.array.email_addresses));
        intent.putExtra(Intent.EXTRA_SUBJECT, r.getString(R.string.subject_bug_report));
        intent.putExtra(Intent.EXTRA_TEXT, String.format(r.getString(R.string.template_bug_report), content));
        startActivity(Intent.createChooser(intent, r.getString(R.string.label_email_via)));
    }

    private void openUrlInExternalBrowser(String url){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void openUrlInCustomTabs(String url){
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.gray900));
        //launches url in custom tab
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(ScheduleActivityOld.this, Uri.parse(url));
    }

    private class ScheduleClient extends WebViewClient {

        private boolean errorReceived = false;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            openUrlInCustomTabs(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) { presenter.onPageStarted(); }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            errorReceived = true;
            presenter.onErrorReceived(errorCode, description, failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            presenter.onPageFinished(errorReceived);
            errorReceived = false;
        }
    }
}
*/