/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.icu.extratest.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.testsharding.MainTestShard;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.ULocale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Locale;

/**
 * Test for Android patches in ICU's calendar.
 */
@MainTestShard
@RunWith(JUnit4.class)
public class CalendarTest {

    /**
     * Ensures that Gregorian is the default Calendar for all Locales in Android. This is the
     * historic behavior on Android; this test exists to avoid unintentional regressions.
     * http://b/80294184
     */
    @Test
    public void testAllDefaultCalendar_Gregorian() {
        for (ULocale locale : ULocale.getAvailableLocales()) {
            assertTrue("Default calendar should be Gregorian: " + locale,
                    Calendar.getInstance(locale) instanceof GregorianCalendar);
        }

        for (ULocale locale : ULocale.getAvailableLocales()) {
            // Classes, e.g. RelativeDateTimeFormatter, use a different property,
            // i.e. calendar/default, for calendar type. However, there is no direct way to obtain
            // the calendar type via the APIs. Verify the property value directly here.
            ICUResourceBundle bundle = (ICUResourceBundle) ICUResourceBundle.getBundleInstance(
                    ICUData.ICU_BASE_NAME, locale);
            String calType = bundle.getStringWithFallback("calendar/default");
            assertEquals("calendar/default should be Gregorian: " + locale, "gregorian", calType);
        }
    }

    /**
     * Test the consistency between {@link Calendar#getWeekData()} and
     * {@link Calendar#getWeekDataForRegion(String)}. It makes sure that we didn't regress
     * when switching to use the {@link Calendar#getWeekData()}.
     */
    @Test
    public void testGetWeekData_consistency() {
        for (Locale l : Locale.getAvailableLocales()) {
            Calendar calendar = Calendar.getInstance(l);
            Calendar.WeekData weekData1 = calendar.getWeekData();

            ULocale uLocale = ULocale.forLocale(l);
            String region = ULocale.getRegionForSupplementalData(uLocale, true);
            Calendar.WeekData weekData2 = Calendar.getWeekDataForRegion(region);

            assertEquals(weekData1, weekData2);
        }
    }
}

