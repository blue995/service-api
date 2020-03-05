package com.epam.ta.reportportal.job.service.impl;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.job.service.AttachmentCleanerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class AttachmentCleanerServiceImpl implements AttachmentCleanerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentCleanerServiceImpl.class);

	private final AttachmentRepository attachmentRepository;

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final DataStoreService dataStoreService;

	public AttachmentCleanerServiceImpl(AttachmentRepository attachmentRepository, LaunchRepository launchRepository,
			TestItemRepository testItemRepository, @Qualifier("attachmentDataStoreService") DataStoreService dataStoreService) {
		this.attachmentRepository = attachmentRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.dataStoreService = dataStoreService;
	}

	@Override
	public void removeOutdatedItemsAttachments(Collection<Long> itemIds, LocalDateTime before, AtomicLong attachmentsCount,
			AtomicLong thumbnailsCount) {
		List<Attachment> attachments = attachmentRepository.findByItemIdsModifiedBefore(itemIds, before);
		removeAttachments(attachments, attachmentsCount, thumbnailsCount);
	}

	@Override
	public void removeOutdatedLaunchesAttachments(Collection<Long> launchIds, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		List<Attachment> attachments = attachmentRepository.findAllByLaunchIdIn(launchIds);
		removeAttachments(attachments, attachmentsCount, thumbnailsCount);
	}

	@Override
	@Transactional
	public void removeProjectAttachments(Project project, LocalDateTime before, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		try (Stream<Long> launchIds = launchRepository.streamIdsModifiedBefore(project.getId(), before)) {
			launchIds.forEach(id -> {
				try (Stream<Long> ids = testItemRepository.streamTestItemIdsByLaunchId(id)) {
					List<Attachment> attachments = attachmentRepository.findByItemIdsModifiedBefore(ids.collect(toList()), before);
					removeAttachments(attachments, attachmentsCount, thumbnailsCount);
				} catch (Exception e) {
					//do nothing
					LOGGER.error("Error during cleaning project attachments", e);
				}
			});
		} catch (Exception e) {
			//do nothing
			LOGGER.error("Error during cleaning project attachments", e);
		}
	}

	private void removeAttachments(Collection<Attachment> attachments, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		List<Long> attachmentIds = new ArrayList<>();
		attachments.forEach(it -> {
			try {
				ofNullable(it).ifPresent(attachment -> {
					ofNullable(attachment.getFileId()).ifPresent(fileId -> {
						dataStoreService.delete(fileId);
						attachmentsCount.addAndGet(1L);
					});
					ofNullable(attachment.getThumbnailId()).ifPresent(thumbnailId -> {
						dataStoreService.delete(thumbnailId);
						thumbnailsCount.addAndGet(1L);
					});
					attachmentIds.add(attachment.getId());
				});
			} catch (Exception ex) {
				LOGGER.debug("Error has occurred during the attachments removing", ex);
				//do nothing, because error that has occurred during the removing of current attachment shouldn't affect others
			}
		});
		attachmentRepository.deleteAllByIds(attachmentIds);
	}
}
