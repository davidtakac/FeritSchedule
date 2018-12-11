package os.dtakac.feritraspored.presenter.schedule;

import android.util.Log;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import os.dtakac.feritraspored.model.resources.ResourceManager;
import os.dtakac.feritraspored.model.repository.IRepository;
import os.dtakac.feritraspored.util.Constants;
import os.dtakac.feritraspored.util.JsUtil;

public class SchedulePresenter implements ScheduleContract.Presenter {

    private ScheduleContract.View view;

    private IRepository repo;
    private ResourceManager resManager;

    private LocalDate dateToDisplay;

    public SchedulePresenter(ScheduleContract.View view, IRepository repo, ResourceManager resManager) {
        this.view = view;
        this.repo = repo;
        this.resManager = resManager;

        this.dateToDisplay = generateDateBasedOnUserPrefs();
    }

    @Override
    public void loadCurrentDay() {
        String currentWeekUrl = buildScheduleUrl();
        String loadedUrl = view.getLoadedUrl();
        boolean settingsModified = repo.get(resManager.getSettingsModifiedKey(), false);

        if(settingsModified || loadedUrl == null || !loadedUrl.equals(currentWeekUrl)){
            dateToDisplay = generateDateBasedOnUserPrefs();
            view.loadUrl(currentWeekUrl);

            repo.add(resManager.getSettingsModifiedKey(), false);
        } else {
            scrollToCurrentDay();
        }
    }

    @Override
    public void hideElementsOtherThanSchedule() {
        view.injectJavascript(hideElementsScript());
        view.injectJavascript(removeElementsScript());
    }

    @Override
    public void scrollToCurrentDay() {
        view.injectJavascript(scrollIntoViewScript());
    }

    @Override
    public void highlightSelectedGroups() {
        Log.d(Constants.LOG_TAG, "highlighting groups");

        view.injectJavascript(highlightElementsScript());
    }

    private LocalDate generateDateBasedOnUserPrefs() {
        LocalDate date = new LocalDate();
        LocalTime time = new LocalTime();

        boolean skipSaturday = repo.get(resManager.getSkipSaturdayKey(), false);
        boolean skipToNextDay = repo.get(resManager.getSkipDayKey(), false);

        if(skipToNextDay) {
            date = skipToNextDay(date, time);
        }

        if(date.getDayOfWeek() == DateTimeConstants.SUNDAY) {
            date = date.plusDays(1);
        } else if(skipSaturday) {
            date = skipSaturday(date);
        }

        return date;
    }

    private LocalDate skipToNextDay(LocalDate date, LocalTime time){
        int hour = repo.get(resManager.getHourKey(), 20);
        int minute = repo.get(resManager.getMinuteKey(), 0);

        return date.plusDays(
                (time.getHourOfDay() >= hour && time.getMinuteOfHour() >= minute) ? 1 : 0
        );
    }

    private LocalDate skipSaturday(LocalDate date){
        return date.plusDays((date.getDayOfWeek() == DateTimeConstants.SATURDAY) ? 2 : 0);
    }

    private String buildScheduleUrl() {

        String defaultProgId = resManager.getUndergradProgrammeId(0);
        String defaultYearId = resManager.getUndergradYearId(0);

        return  resManager.getScheduleUrl()
                //load current week
                + dateToDisplay.withDayOfWeek(DateTimeConstants.MONDAY).toString()
                //load selected year
                + "/" + repo.get(resManager.getYearKey(), defaultYearId)
                //load selected programme
                + "-" + repo.get(resManager.getProgrammeKey(), defaultProgId)
                ;
    }

    private String hideElementsScript() {
        return "(" +
                JsUtil.toHideElementsWithIdFunction("header-top,header,gototopdiv,footer,sidebar") +
                "())";
    }

    private String removeElementsScript() {
        return "(" +
                JsUtil.toRemoveElementsWithIdFunction("izbor-studija") +
                "())";
    }

    private String scrollIntoViewScript() {
        return  "(" +
                JsUtil.toScrollIntoViewFunction(dateToDisplay.toString()) +
                "())";
    }

    private String highlightElementsScript() {
        String pContainsQuery = JsUtil.toPContains(
                repo.get(resManager.getGroupsKey(), "")
        );

        return "($(\"" +
                pContainsQuery +
                "\").css(\"text-transform\",\"uppercase\").css(\"color\",\"#EF271B\"))";
    }
}
