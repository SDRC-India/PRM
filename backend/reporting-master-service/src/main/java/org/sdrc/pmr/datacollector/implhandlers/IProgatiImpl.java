package org.sdrc.pmr.datacollector.implhandlers;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.util.IProgatiInterface;

/**
 * @author Sarita
 *
 */
@Service
public class IProgatiImpl implements IProgatiInterface {

	@Override
	public List<EnginesForm> getAssignesFormsForDataEntry(AccessType dataEntry) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EnginesForm> getAssignesFormsForDataEntryByCreatedDate(AccessType dataEntry, Date createdDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EnginesForm> getAssignesFormsForReview(AccessType review) {
		// TODO Auto-generated method stub
		return null;
	}}
