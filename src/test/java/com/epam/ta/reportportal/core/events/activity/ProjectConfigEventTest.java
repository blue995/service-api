/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.ActivityDetails;
import com.epam.ta.reportportal.entity.HistoryField;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ProjectConfigEventTest {

	private static final Pair<String, String> ANALYZER_MODE = Pair.of("false", "true");
	private static final Pair<String, String> MIN_DOC_FREQ = Pair.of("7", "8");
	private static final Pair<String, String> MIN_TERM_FREQ = Pair.of("1", "2");
	private static final Pair<String, String> MIN_SHOULD_MATCH = Pair.of("80", "100");
	private static final Pair<String, String> NUMBER_OF_LOG_LINES = Pair.of("5", "10");
	private static final Pair<String, String> AUTO_ANALYZED_ENABLED = Pair.of("false", "true");

	private static final Pair<String, String> KEEP_LOGS = Pair.of("1 month", "3 month");
	private static final Pair<String, String> KEEP_SCREENSHOTS = Pair.of("2 weeks", "3 weeks");
	private static final Pair<String, String> KEEP_LAUNCHES = Pair.of("1", "2");

	@Test
	public void analyzerConfigUpdate() {
		final Activity actual = new ProjectAnalyzerConfigEvent(getProjectAttributes(getAnalyzerConfig(ANALYZER_MODE.getLeft(),
				MIN_DOC_FREQ.getLeft(),
				MIN_TERM_FREQ.getLeft(),
				MIN_SHOULD_MATCH.getLeft(),
				NUMBER_OF_LOG_LINES.getLeft(),
				AUTO_ANALYZED_ENABLED.getLeft()
		)), getProjectAttributes(getAnalyzerConfig(ANALYZER_MODE.getRight(),
				MIN_DOC_FREQ.getRight(),
				MIN_TERM_FREQ.getRight(),
				MIN_SHOULD_MATCH.getRight(),
				NUMBER_OF_LOG_LINES.getRight(),
				AUTO_ANALYZED_ENABLED.getRight()
		)), USER_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.UPDATE_ANALYZER);
		expected.getDetails()
				.setHistory(getAnalyzerConfigHistory(ANALYZER_MODE,
						MIN_DOC_FREQ,
						MIN_TERM_FREQ,
						MIN_SHOULD_MATCH,
						NUMBER_OF_LOG_LINES,
						AUTO_ANALYZED_ENABLED
				));
		assertActivity(expected, actual);
	}

	@Test
	public void projectConfigUpdate() {
		final Activity actual = new ProjectUpdatedEvent(
				getProjectAttributes(getProjectConfig(KEEP_LOGS.getLeft(), KEEP_SCREENSHOTS.getLeft(), KEEP_LAUNCHES.getLeft())),
				getProjectAttributes(getProjectConfig(KEEP_LOGS.getRight(), KEEP_SCREENSHOTS.getRight(), KEEP_LAUNCHES.getRight())),
				USER_ID
		).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.UPDATE_PROJECT);
		expected.getDetails().setHistory(getProjectConfigHistory(KEEP_LOGS, KEEP_SCREENSHOTS, KEEP_LAUNCHES));
		assertActivity(expected, actual);
	}

	private static ProjectAttributesActivityResource getProjectAttributes(Map<String, String> config) {
		ProjectAttributesActivityResource resource = new ProjectAttributesActivityResource();
		resource.setProjectName(PROJECT_NAME);
		resource.setProjectId(PROJECT_ID);
		resource.setConfig(config);
		return resource;
	}

	private static Map<String, String> getAnalyzerConfig(String analyzerMode, String minDocFreq, String minTermFreq, String minShouldMatch,
			String numberOfLogs, String autoAnalyzerEnabled) {
		HashMap<String, String> result = new HashMap<>();
		result.put(ProjectAttributeEnum.AUTO_ANALYZER_MODE.getAttribute(), analyzerMode);
		result.put(ProjectAttributeEnum.MIN_DOC_FREQ.getAttribute(), minDocFreq);
		result.put(ProjectAttributeEnum.MIN_TERM_FREQ.getAttribute(), minTermFreq);
		result.put(ProjectAttributeEnum.MIN_SHOULD_MATCH.getAttribute(), minShouldMatch);
		result.put(ProjectAttributeEnum.NUMBER_OF_LOG_LINES.getAttribute(), numberOfLogs);
		result.put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), autoAnalyzerEnabled);
		return result;
	}

	private static Map<String, String> getProjectConfig(String keepLogs, String keepScreenshots, String keepLaunches) {
		HashMap<String, String> result = new HashMap<>();
		result.put(ProjectAttributeEnum.KEEP_LOGS.getAttribute(), keepLogs);
		result.put(ProjectAttributeEnum.KEEP_SCREENSHOTS.getAttribute(), keepScreenshots);
		result.put(ProjectAttributeEnum.KEEP_LAUNCHES.getAttribute(), keepLaunches);
		return result;
	}

	private static Activity getExpectedActivity(ActivityAction action) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.PROJECT);
		activity.setUserId(USER_ID);
		activity.setProjectId(PROJECT_ID);
		activity.setObjectId(PROJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(PROJECT_NAME));
		return activity;
	}

	private static List<HistoryField> getAnalyzerConfigHistory(Pair<String, String> analyzerMode, Pair<String, String> minDocFreq,
			Pair<String, String> minTermFreq, Pair<String, String> minShouldMatch, Pair<String, String> numberOfLogsLines,
			Pair<String, String> autoAnalyzed) {
		return Lists.newArrayList(
				HistoryField.of(ProjectAttributeEnum.AUTO_ANALYZER_MODE.getAttribute(), analyzerMode.getLeft(), analyzerMode.getRight()),
				HistoryField.of(ProjectAttributeEnum.MIN_DOC_FREQ.getAttribute(), minDocFreq.getLeft(), minDocFreq.getRight()),
				HistoryField.of(ProjectAttributeEnum.MIN_TERM_FREQ.getAttribute(), minTermFreq.getLeft(), minTermFreq.getRight()),
				HistoryField.of(ProjectAttributeEnum.MIN_SHOULD_MATCH.getAttribute(), minShouldMatch.getLeft(), minShouldMatch.getRight()),
				HistoryField.of(ProjectAttributeEnum.NUMBER_OF_LOG_LINES.getAttribute(),
						numberOfLogsLines.getLeft(),
						numberOfLogsLines.getRight()
				),
				HistoryField.of(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), autoAnalyzed.getLeft(), autoAnalyzed.getRight())
		);
	}

	private static List<HistoryField> getProjectConfigHistory(Pair<String, String> keepLogs, Pair<String, String> keepScreenshots,
			Pair<String, String> keepLaunches) {
		return Lists.newArrayList(HistoryField.of(ProjectAttributeEnum.KEEP_LOGS.getAttribute(), keepLogs.getLeft(), keepLogs.getRight()),
				HistoryField.of(ProjectAttributeEnum.KEEP_SCREENSHOTS.getAttribute(),
						keepScreenshots.getLeft(),
						keepScreenshots.getRight()
				),
				HistoryField.of(ProjectAttributeEnum.KEEP_LAUNCHES.getAttribute(), keepLaunches.getLeft(), keepLaunches.getRight())
		);
	}

}