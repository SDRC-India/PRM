package org.sdrc.pmr.service;

import java.util.List;

import org.sdrc.pmr.model.AllChecklistFormDataDTO;

public interface ApiService {

	List<String> getAllUnAckInstances();

	AllChecklistFormDataDTO fetchSubmissionDetail(String submissionId);

}
