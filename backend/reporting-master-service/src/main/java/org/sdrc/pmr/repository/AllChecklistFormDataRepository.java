package org.sdrc.pmr.repository;

import java.util.Date;
import java.util.List;

import org.sdrc.pmr.collection.AllChecklistFormData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AllChecklistFormDataRepository extends MongoRepository<AllChecklistFormData, String> {

	AllChecklistFormData findById(String submissionId);

	List<AllChecklistFormData> findByFormId(Integer formId);

	List<AllChecklistFormData> findAllByFormIdAndSyncDateBetweenAndIsValidTrue(Integer formId, Date startDate, Date endDate);

	AllChecklistFormData findByIdAndFormId(String submissionId, Integer formId);

	List<AllChecklistFormData> findByFormIdAndUniqueIdAndRejectedFalse(Integer formId, String uniqueId);

	List<AllChecklistFormData> findByIdIn(List<String> rejectionList);

	List<AllChecklistFormData> findByFormIdAndIsValidTrueAndLatestTrue(Integer formId);

}
