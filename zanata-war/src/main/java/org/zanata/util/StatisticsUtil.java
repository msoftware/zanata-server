package org.zanata.util;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.AbstractTranslationCount;
import org.zanata.common.ContentState;
import org.zanata.common.TransUnitWords;
import org.zanata.common.statistic.WordStatistic;
import org.zanata.rest.dto.stats.TranslationStatistics;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
//@Name("statisticsUtil")
//@Scope(ScopeType.APPLICATION)
public class StatisticsUtil implements Serializable {

    public static int calculateUntranslated(Long totalCount,
            AbstractTranslationCount translationCount) {
        return totalCount.intValue()
                - translationCount.get(ContentState.Translated)
                - translationCount.get(ContentState.NeedReview)
                - translationCount.get(ContentState.Approved)
                - translationCount.get(ContentState.Rejected);
    }

    public static double getRemainingHours(WordStatistic wordsStatistic) {
        return getRemainingHours(wordsStatistic.getTranslated(),
                wordsStatistic.getUntranslated(),
                wordsStatistic.getNeedReview() + wordsStatistic.getRejected());
    }

    public static double getRemainingHours(
            TranslationStatistics translationStatistics) {
        return getRemainingHours(translationStatistics.getTranslatedOnly(),
                translationStatistics.getUntranslated(),
                translationStatistics.getDraft());
    }

    public static double getRemainingHours(TransUnitWords transUnitWords) {
        return getRemainingHours(transUnitWords.getTranslated(),
                transUnitWords.getUntranslated(),
                transUnitWords.getNeedReview());
    }

    public static String formatPercentage(double percentage) {
        return String.valueOf(Math.floor(percentage));
    }

    public static String formatHours(double hours) {
        return String.valueOf(Math.ceil(hours * 100.0) / 100);
    }

    private static double getRemainingHours(double translated,
            double untranslated, double draft) {
        double untranslatedHours = untranslated / 250.0;
        double draftHours = draft / 500.0;
        double translatedHours = translated / 500.0;

        return translatedHours + untranslatedHours + draftHours;
    }

}
