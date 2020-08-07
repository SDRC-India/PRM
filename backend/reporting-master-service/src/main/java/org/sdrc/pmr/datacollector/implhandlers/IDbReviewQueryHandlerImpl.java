package org.sdrc.pmr.datacollector.implhandlers;

import java.util.Map;

import org.springframework.stereotype.Component;

import in.co.sdrc.sdrcdatacollector.document.Question;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.handlers.IDbReviewQueryHandler;
import in.co.sdrc.sdrcdatacollector.models.DataModel;
import in.co.sdrc.sdrcdatacollector.models.DataObject;

/**
 * @author Sarita
 *
 */
@Component
public class IDbReviewQueryHandlerImpl implements IDbReviewQueryHandler {

	@Override
	public DataObject setReviewHeaders(DataObject dataObject, Question question,
			Map<Integer, TypeDetail> typeDetailsMap, DataModel submissionData, String type) {
		// TODO Auto-generated method stub
		return null;
	}}
