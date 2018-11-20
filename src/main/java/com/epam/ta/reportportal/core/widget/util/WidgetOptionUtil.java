package com.epam.ta.reportportal.core.widget.util;

import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class WidgetOptionUtil {

	private WidgetOptionUtil() {
		//static only
	}

	public static String getValueByKey(String key, WidgetOptions widgetOptions) {
		expect(widgetOptions, notNull()).verify(ErrorType.BAD_REQUEST_ERROR, "Widget options should be not null.");
		expect(MapUtils.isNotEmpty(widgetOptions.getOptions()), equalTo(true)).verify(ErrorType.BAD_REQUEST_ERROR,
				"Widget options should be not empty."
		);
		Object value = widgetOptions.getOptions().get(key);
		expect(value, v -> v instanceof String).verify(ErrorType.BAD_REQUEST_ERROR, "Wrong widget option value type. String expected.");
		return (String) value;
	}

	public static Map<String, List<String>> getMapByKey(String key, WidgetOptions widgetOptions) {
		expect(widgetOptions, notNull()).verify(ErrorType.BAD_REQUEST_ERROR, "Widget options should be not null.");
		expect(MapUtils.isNotEmpty(widgetOptions.getOptions()), equalTo(true)).verify(ErrorType.BAD_REQUEST_ERROR,
				"Widget options should be not empty."
		);

		Object value = widgetOptions.getOptions().get(key);
		expect(value, v -> v instanceof Map).verify(ErrorType.BAD_REQUEST_ERROR, "Wrong widget option value type. Map expected.");

		return (Map<String, List<String>>) value;
	}

	public static boolean containsKey(String key, WidgetOptions widgetOptions) {
		expect(widgetOptions, notNull()).verify(ErrorType.BAD_REQUEST_ERROR, "Widget options should be not null.");
		expect(MapUtils.isNotEmpty(widgetOptions.getOptions()), equalTo(true)).verify(ErrorType.BAD_REQUEST_ERROR,
				"Widget options should be not empty."
		);

		return widgetOptions.getOptions().containsKey(key);
	}
}
