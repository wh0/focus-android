/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.activity;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.web.internal.deps.guava.base.Strings;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.focus.R;
import org.mozilla.focus.activity.helpers.MainActivityFirstrunTestRule;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasChildCount;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.getText;
import static org.hamcrest.Matchers.containsString;
import static org.mozilla.focus.activity.TestHelper.webPageLoadwaitingTime;
import static org.mozilla.focus.activity.helpers.EspressoHelper.navigateToWebsite;

@RunWith(AndroidJUnit4.class)
public class PullDownToRefreshTest {
    private static final String MOZILLA_WEBSITE_SLOGAN_SELECTOR = ".content h2";
    private static final String MOZILLA_WEBSITE_SLOGAN_TEXT = "Internet for people,\nnot profit.";
    private static final String COUNTER = "counter";

    private MockWebServer webServer;
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new MainActivityFirstrunTestRule(false);


    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Before
    public void setUpWebServer() throws IOException {
        webServer = new MockWebServer();

        // Test page
        webServer.enqueue(new MockResponse().setBody(TestHelper.readTestAsset("counter.html")));
    }

    @After
    public void tearDownWebServer() {
        try {
            webServer.close();
            webServer.shutdown();
        } catch (IOException e) {
            throw new AssertionError("Could not stop web server", e);
        }
    }

    @Test
    public void pullDownToRefreshTest() throws InterruptedException, UiObjectNotFoundException, IOException {
        // Go to a website
        //navigateToWebsite(("file:///Users/irios/Downloads/counter.html"));

        //TestHelper.progressBar.waitForExists(webPageLoadwaitingTime);
        //Assert.assertTrue(TestHelper.progressBar.exists());
        //Assert.assertTrue(TestHelper.progressBar.waitUntilGone(webPageLoadwaitingTime));

        // Check that the webview is loaded
        //assertWebsiteUrlContains("counter.html");
        onView(withId(R.id.url_edit))
                .check(matches(isDisplayed()))
                .check(matches(hasFocus()))
                .perform(click(), replaceText(webServer.url("/").toString()), pressImeActionButton());

        onView(withId(R.id.display_url))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString(webServer.getHostName()))));

        onWebView()
                .withElement(findElement(Locator.ID, COUNTER));
                //.check(webMatches(getText(), containsString(MOZILLA_WEBSITE_SLOGAN_TEXT)));

        onView(withId(R.id.swipe_refresh))
                .check(matches(isDisplayed()));

        // Swipe down to refresh, spinner is shown (2nd child) and progress bar is shown
        onView(withId(R.id.swipe_refresh))
              .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(85)))
              .check(matches(hasChildCount(2)));

        Assert.assertTrue(TestHelper.progressBar.waitForExists(webPageLoadwaitingTime));

        // Check that the webview is loaded again and the progress bar dissapears
        //onWebView()
          //     .withElement(findElement(Locator.ID, COUNTER));
         //       .check(webMatches(getText(), containsString(MOZILLA_WEBSITE_SLOGAN_TEXT)));

       //Assert.assertFalse(TestHelper.progressBar.exists());
    }

    private void assertWebsiteUrlContains(String substring) {
        onView(withId(R.id.display_url))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString(substring))));
    }
    public static ViewAction withCustomConstraints(final ViewAction action, final Matcher<View> constraints) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return constraints;
            }

            @Override
            public String getDescription() {
                return action.getDescription();
            }

            @Override
            public void perform(UiController uiController, View view) {
                action.perform(uiController, view);
            }
        };
    }
}
