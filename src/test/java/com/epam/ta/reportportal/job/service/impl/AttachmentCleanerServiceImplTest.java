/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.job.service.impl;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.project.Project;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class AttachmentCleanerServiceImplTest {

	@Mock
	private AttachmentRepository attachmentRepository;

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private DataStoreService dataStoreService;

	@InjectMocks
	private AttachmentCleanerServiceImpl attachmentCleanerService;

	@Test
	void removeOutdatedItemsAttachmentsPositiveTest() {
		ArrayList<Long> itemIds = Lists.newArrayList(1L, 2L, 3L);
		LocalDateTime before = LocalDateTime.now().minus(ChronoUnit.WEEKS.getDuration());
		AtomicLong attachmentCount = new AtomicLong();
		AtomicLong thumbnailCount = new AtomicLong();

		String fileId = "fileId";
		String thumbnailId = "thumbnailId";
		Attachment attachment = testAttachment(fileId, thumbnailId);

		when(attachmentRepository.findByItemIdsModifiedBefore(itemIds, before)).thenReturn(Collections.singletonList(attachment));

		attachmentCleanerService.removeOutdatedItemsAttachments(itemIds, before, attachmentCount, thumbnailCount);

		assertEquals(1L, attachmentCount.get());
		assertEquals(1L, thumbnailCount.get());
		verify(dataStoreService, times(1)).delete(fileId);
		verify(dataStoreService, times(1)).delete(thumbnailId);
		verify(attachmentRepository, times(1)).deleteAllByIds(Collections.singletonList(attachment.getId()));
	}

	@Test
	void removeOutdatedLaunchesAttachmentsPositive() {
		ArrayList<Long> launchIds = Lists.newArrayList(1L, 2L, 3L);
		AtomicLong attachmentCount = new AtomicLong();
		AtomicLong thumbnailCount = new AtomicLong();

		String fileId = "fileId";
		String thumbnailId = "thumbnailId";
		Attachment attachment = testAttachment(fileId, thumbnailId);

		when(attachmentRepository.findAllByLaunchIdIn(launchIds)).thenReturn(Collections.singletonList(attachment));

		attachmentCleanerService.removeOutdatedLaunchesAttachments(launchIds, attachmentCount, thumbnailCount);

		assertEquals(1L, attachmentCount.get());
		assertEquals(1L, thumbnailCount.get());
		verify(dataStoreService, times(1)).delete(fileId);
		verify(dataStoreService, times(1)).delete(thumbnailId);
		verify(attachmentRepository, times(1)).deleteAllByIds(Collections.singletonList(attachment.getId()));
	}

	@Test
	void removeProjectAttachmentsPositive() {
		Project project = new Project();
		project.setId(111L);
		LocalDateTime before = LocalDateTime.now().minus(ChronoUnit.WEEKS.getDuration());
		AtomicLong attachmentCount = new AtomicLong();
		AtomicLong thumbnailCount = new AtomicLong();

		when(launchRepository.streamIdsModifiedBefore(project.getId(), before)).thenReturn(Stream.of(1L, 2L, 3L));
		when(testItemRepository.streamTestItemIdsByLaunchId(1L)).thenReturn(Stream.of(11L));
		when(testItemRepository.streamTestItemIdsByLaunchId(2L)).thenReturn(Stream.of(22L));
		when(testItemRepository.streamTestItemIdsByLaunchId(3L)).thenReturn(Stream.of(33L));
		when(attachmentRepository.findByItemIdsModifiedBefore(Collections.singletonList(11L), before)).thenReturn(Collections.singletonList(
				testAttachment("one", "two")));
		when(attachmentRepository.findByItemIdsModifiedBefore(Collections.singletonList(22L), before)).thenReturn(Collections.singletonList(
				testAttachment("three", "four")));
		when(attachmentRepository.findByItemIdsModifiedBefore(Collections.singletonList(33L), before)).thenReturn(Collections.singletonList(
				testAttachment("five", null)));

		attachmentCleanerService.removeProjectAttachments(project, before, attachmentCount, thumbnailCount);

		assertEquals(3L, attachmentCount.get());
		assertEquals(2L, thumbnailCount.get());
		verify(dataStoreService, times(5)).delete(anyString());
		verify(attachmentRepository, times(3)).deleteAllByIds(anyCollection());
	}

	private static Attachment testAttachment(String fileId, String thumbnailId) {
		Attachment attachment = new Attachment();
		attachment.setId(100L);
		attachment.setFileId(fileId);
		attachment.setThumbnailId(thumbnailId);
		return attachment;
	}
}