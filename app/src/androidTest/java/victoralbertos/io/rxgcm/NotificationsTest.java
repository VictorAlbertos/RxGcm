package victoralbertos.io.rxgcm;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import victoralbertos.io.rxgcm.data.api.GcmServerService;
import victoralbertos.io.rxgcm.presentation.FragmentBase;
import victoralbertos.io.rxgcm.presentation.HostActivityIssues;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by victor on 08/02/16.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationsTest {
    private final static String TITLE = "A tittle", BODY = "A Body";
    @Rule public ActivityTestRule<HostActivityIssues> mActivityRule = new ActivityTestRule(HostActivityIssues.class);

    @Test public void _1_Send_And_Receive_Notification_On_Foreground() {
        //Conservative delay to wait for token in case app is not previously installed
        waitTime(7000);

        //Send issue
        onView(withId(R.id.et_title)).perform(click(), replaceText(TITLE), closeSoftKeyboard());
        onView(withId(R.id.et_body)).perform(click(), replaceText(BODY), closeSoftKeyboard());
        onView(withId(R.id.bt_send_issue)).perform(click());

        waitTime(3000);

        onView(withId(R.id.rv_notifications)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Send supply
        onView(withId(R.id.et_title)).perform(click(), replaceText(TITLE), closeSoftKeyboard());
        onView(withId(R.id.et_body)).perform(click(), replaceText(BODY), closeSoftKeyboard());
        onView(withId(R.id.bt_send_supply)).perform(click());

        waitTime(3000);

        onView(withId(R.id.tv_log)).check(matches(withText(FragmentBase.MISMATCH_TARGET_MESSAGE)));
        onView(withId(R.id.bt_go_to_other_screen)).perform(click());

        waitTime(1000);
        onView(withId(R.id.et_title)).perform(click(), replaceText(TITLE), closeSoftKeyboard());
        onView(withId(R.id.rv_notifications)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    @Test public void _2_Send_And_Receive_Notification_On_Background() {
        AppGcmReceiverUIBackground.initTestBackgroundMessage();
        mActivityRule.getActivity().finish();

        waitTime(1500);
        new GcmServerService().sendGcmNotificationRequestingIssue(TITLE, BODY).subscribe();
        waitTime(3000);
        assertNotNull(AppGcmReceiverUIBackground.getBackgroundMessage());
    }

    private  void waitTime(long time) {
        try {Thread.sleep(time);
        } catch (InterruptedException e) { e.printStackTrace();}
    }
}
