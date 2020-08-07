package org.sdrc.fani.datacollector.implhandlers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sdrc.fani.collections.DesignationPartnerFormMapping;
import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.repositories.DesignationPartnerFormMappingRepository;
import org.sdrc.fani.repositories.EnginesFormRepository;
import org.sdrc.fani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.util.IProgatiInterface;

/**
 * @author subham
 *
 */
@Service
public class IProgatiImpl implements IProgatiInterface {

	@Autowired
	private EnginesFormRepository enginesFormRepository;

	@Autowired
	private DesignationPartnerFormMappingRepository designationPartnerFormMappingRepository;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	private DesignationRepository designationRepository;

	@Override
	public List<EnginesForm> getAssignesFormsForDataEntry(AccessType accessType) {

		List<EnginesForm> formsz = new ArrayList<>();

		OAuth2Authentication oauth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();

		UserModel userModel = tokenInfoExtracter.getUserModelInfo(oauth);

		String designation = userModel.getRoleIds().iterator().next();

		/**
		 * if loggedin user is admin than get all forms
		 */
		Designation desgs = designationRepository.findBySlugIdIn(userModel.getDesgSlugIds()).get(0);

		if (desgs.getCode().equals("ADMIN")) {
			List<EnginesForm> forms = enginesFormRepository.findAll();
			formsz.addAll(forms);
		} else {
			List<DesignationPartnerFormMapping> designationPartnerFormMapping = designationPartnerFormMappingRepository
					.findBypartnerIdInAndAccessTypeAndDesignation(userModel.getPartnerId(), accessType,
							designationRepository.findById(designation));

			for (DesignationPartnerFormMapping dpfm : designationPartnerFormMapping) {
				List<Integer> formIds = dpfm.getFormId();

				List<EnginesForm> forms = enginesFormRepository.findByFormIdIn(formIds);
				formsz.addAll(forms);
			}
		}

		return formsz;

	}

	@Override
	public List<EnginesForm> getAssignesFormsForReview(AccessType arg0) {
		return enginesFormRepository.findAll();
	}

	@Override
	public List<EnginesForm> getAssignesFormsForDataEntryByCreatedDate(AccessType accessType, Date createdDate) {
		List<EnginesForm> formsz = new ArrayList<>();

		OAuth2Authentication oauth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();

		UserModel userModel = tokenInfoExtracter.getUserModelInfo(oauth);

		String designation = userModel.getRoleIds().iterator().next();

		/**
		 * if loggedin user is admin than get all forms
		 */
		/*Designation desgs = designationRepository.findBySlugIdIn(userModel.getDesgSlugIds()).get(0);

		if (desgs.getCode().equals("ADMIN")) {
			List<EnginesForm> forms = enginesFormRepository.findAllByUpdatedDate(createdDate);
			formsz.addAll(forms);
		} else {
			List<DesignationPartnerFormMapping> designationPartnerFormMapping = designationPartnerFormMappingRepository
					.findBypartnerIdInAndAccessTypeAndDesignation(userModel.getPartnerId(), accessType,
							designationRepository.findById(designation));

			for (DesignationPartnerFormMapping dpfm : designationPartnerFormMapping) {
				List<Integer> formIds = dpfm.getFormId();

				List<EnginesForm> forms = enginesFormRepository.findByFormIdInAndUpdatedDate(formIds,createdDate);
				formsz.addAll(forms);
			}
		}*/
		
		formsz = enginesFormRepository.findAllByUpdatedDate(createdDate);

		return formsz;
		}
}
