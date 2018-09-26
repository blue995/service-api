package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.util.ContentFieldMatcherUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.LATEST_OPTION;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants.COMBINED_CONTENT_FIELDS_REGEX;
import static java.util.Collections.singletonMap;

/**
 * @author Ivan Budaev
 */
@Service
public class ProductStatusFilterGroupedContentLoader implements ProductStatusContentLoader {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> fields, Map<Filter, Sort> filterSortMapping, Map<String, String> widgetOptions,
			int limit) {

		validateFilterSortMapping(filterSortMapping);

		validateWidgetOptions(widgetOptions);

		//in separate method for testing
		List<String> tags = fields.stream()
				.filter(f -> f.startsWith("tag"))
				.map(field -> field.split("\\$")[1])
				.collect(Collectors.toList());

		List<String> contentFields = fields.stream().filter(f -> !f.startsWith("tag")).collect(Collectors.toList());

		validateContentFields(contentFields);

		return singletonMap(RESULT,
				widgetContentRepository.productStatusGroupedByFilterStatistics(filterSortMapping,
						contentFields,
						tags,
						widgetOptions.containsKey(LATEST_OPTION),
						limit
				)
		);
	}

	/**
	 * Mapping should not be empty
	 *
	 * @param filterSortMapping Map of ${@link Filter} for query building as key and ${@link Sort} as value for each filter
	 */
	private void validateFilterSortMapping(Map<Filter, Sort> filterSortMapping) {
		BusinessRule.expect(MapUtils.isNotEmpty(filterSortMapping), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Filter-Sort mapping should not be empty");
	}

	/**
	 * Validate provided widget options.
	 *
	 * @param widgetOptions Map of stored widget options.
	 */
	private void validateWidgetOptions(Map<String, String> widgetOptions) {
		BusinessRule.expect(MapUtils.isNotEmpty(widgetOptions), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Widget options should not be null.");
	}

	/**
	 * Validate provided content fields.
	 * The value of content field should not be empty
	 * All content fields should match the pattern {@link com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants#COMBINED_CONTENT_FIELDS_REGEX}
	 *
	 * @param contentFields List of provided content.
	 */
	private void validateContentFields(List<String> contentFields) {
		BusinessRule.expect(CollectionUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
		BusinessRule.expect(ContentFieldMatcherUtil.match(COMBINED_CONTENT_FIELDS_REGEX, contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Bad content fields format");
	}
}
