/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.util;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class TfsIntegrationService extends BasicIntegrationServiceImpl {

	private BtsIntegrationService btsIntegrationService;

	@Autowired
	public TfsIntegrationService(IntegrationRepository integrationRepository, PluginBox pluginBox, BtsIntegrationService btsIntegrationService) {
		super(integrationRepository, pluginBox);
		this.btsIntegrationService = btsIntegrationService;
	}

	@Override
	public Map<String, Object> retrieveCreateParams(String integrationType, Map<String, Object> integrationParams) {
		expect(integrationParams, MapUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR, "No integration params provided");
		
		Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(BtsProperties.values().length);

		resultParams.put(BtsProperties.PROJECT.getName(),
				BtsProperties.PROJECT.getParam(integrationParams)
						.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "TFS project is not specified."))
		);
		resultParams.put(BtsProperties.URL.getName(),
				BtsProperties.URL.getParam(integrationParams)
						.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "TFS url is not specified."))
		);

		resultParams.put(BtsProperties.ATTACHMENT_URL.getName(),
				BtsProperties.ATTACHMENT_URL.getParam(integrationParams)
						.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "TFS attachment server url is not specified."))
		);

		return resultParams;
	}

	@Override
	public Map<String, Object> retrieveUpdatedParams(String integrationType, Map<String, Object> integrationParams) {
		Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(BtsProperties.values().length);

		BtsProperties.PROJECT.getParam(integrationParams)
				.ifPresent(btsProject -> resultParams.put(BtsProperties.PROJECT.getName(), btsProject));
		BtsProperties.URL.getParam(integrationParams).ifPresent(btsProject -> resultParams.put(BtsProperties.URL.getName(), btsProject));

		Optional.ofNullable(integrationParams.get("defectFormFields"))
				.ifPresent(defectFormFields -> resultParams.put("defectFormFields", defectFormFields));

		BtsProperties.ATTACHMENT_URL.getParam(integrationParams)
				.ifPresent(attachmentUrl -> resultParams.put(BtsProperties.ATTACHMENT_URL.getName(), attachmentUrl));
		
		BtsProperties.USER_NAME.getParam(integrationParams)
				.ifPresent(userName -> resultParams.put(BtsProperties.USER_NAME.getName(), userName));

		return resultParams;
	}
}
