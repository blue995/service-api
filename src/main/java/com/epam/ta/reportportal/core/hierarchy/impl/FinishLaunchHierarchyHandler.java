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

package com.epam.ta.reportportal.core.hierarchy.impl;

import com.epam.ta.reportportal.core.hierarchy.AbstractFinishHierarchyHandler;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.ItemAttributePojo;
import com.epam.ta.reportportal.entity.item.issue.IssueEntityPojo;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotSupportedException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("finishLaunchHierarchyHandler")
public class FinishLaunchHierarchyHandler extends AbstractFinishHierarchyHandler<Launch> {

	@Autowired
	public FinishLaunchHierarchyHandler(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			ItemAttributeRepository itemAttributeRepository, IssueTypeHandler issueTypeHandler,
			IssueEntityRepository issueEntityRepository) {
		super(launchRepository, testItemRepository, itemAttributeRepository, issueEntityRepository, issueTypeHandler);
	}

	@Override
	protected void updateDescendantsWithoutChildren(Long projectId, Launch launch, StatusEnum status, LocalDateTime endTime,
			boolean isIssueRequired, List<ItemAttributePojo> itemAttributes, List<IssueEntityPojo> issueEntities) {

		Optional<IssueType> issueType = getIssueType(isIssueRequired, projectId, TO_INVESTIGATE.getLocator());

		testItemRepository.streamIdsByNotHasChildrenAndLaunchIdAndStatus(launch.getId(), StatusEnum.IN_PROGRESS)
				.forEach(itemId -> updateDescendantWithoutChildren(itemId.longValue(),
						status,
						endTime,
						issueType,
						itemAttributes,
						issueEntities
				));

		if (!itemAttributes.isEmpty()) {
			itemAttributeRepository.saveMultiple(itemAttributes);
		}
		if (!issueEntities.isEmpty()) {
			issueEntityRepository.saveMultiple(issueEntities);
		}

	}

	@Override
	protected void updateDescendantsWithChildren(Launch launch, LocalDateTime endTime, List<ItemAttributePojo> itemAttributes) {
		testItemRepository.streamIdsByHasChildrenAndLaunchIdAndStatusOrderedByPathLevel(launch.getId(), StatusEnum.IN_PROGRESS)
				.forEach(itemId -> updateDescendantWithChildren(itemId.longValue(), endTime, itemAttributes));
		if (!itemAttributes.isEmpty()) {
			itemAttributeRepository.saveMultiple(itemAttributes);
		}
	}

	@Override
	protected boolean isIssueRequired(StatusEnum status, Launch launch) {
		return FAILED.equals(status) || evaluateSkippedAttributeValue(status, launch.getId());
	}

	@Override
	public void setAncestorsStatus(Launch entity, StatusEnum status, Date endDate) {
		throw new NotSupportedException("Launch has not ancestors");
	}
}
