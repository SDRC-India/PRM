package org.sdrc.fani.usermgmt.handler;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.UserDetails;
import org.sdrc.fani.models.Gender;
import org.sdrc.fani.models.UserStatus;
import org.sdrc.fani.repositories.AreaRepository;
import org.sdrc.usermgmt.core.util.IUserManagementHandler;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.AccountAudit;
import org.sdrc.usermgmt.mongodb.domain.AssignedDesignations;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author subham
 *
 */
@Slf4j
@Service
public class SessionMapInitializer implements IUserManagementHandler {

	@Autowired
	private AreaRepository areaRepositoy;

	@Autowired
	@Qualifier("mongoAccountRepository")
	private AccountRepository accountRepository;

	@Autowired
	@Qualifier("mongoDesignationRepository")
	private DesignationRepository designationRepository;
	
	private ConfigurableEnvironment configurableEnvironment;
	
	/**
	 * Pending users: enabled = true; expired = false; Locked = true
	 * Approved users: enabled = true; expired = false; Locked = false
	 * Rejected users: enabled = true; expired = true; Locked = false
	 */
	@Override
	public boolean saveAccountDetails(Map<String, Object> map, Object account) {
		try {

			Account acc = (Account) account;

			if (map.get("firstName") == null || map.get("firstName").toString().isEmpty())
				throw new RuntimeException("key : firstName not found in map");

			if (map.get("lastName") == null || map.get("lastName").toString().isEmpty())
				throw new RuntimeException("key : lastName not found in map");

			if (map.get("gender") == null || map.get("gender").toString().isEmpty())
				throw new RuntimeException("key : gender not found in map");

			if (map.get("mobNo") == null || map.get("mobNo").toString().isEmpty())
				throw new RuntimeException("key : mobNo not found in map");

			if (map.get("areaId") == null || map.get("areaId").toString().isEmpty())
				throw new RuntimeException("key : areaId not found in map");

			UserDetails userDetails = new UserDetails();
			userDetails.setFirstName(map.get("firstName").toString());
			userDetails
					.setGender(map.get("gender").toString().equalsIgnoreCase(("MALE")) ? Gender.MALE : Gender.FEMALE);
			userDetails.setLastName(map.get("lastName").toString());
//			userDetails.setDob(map.get("dob").toString());

			if (map.get("middleName") != null) {
				userDetails.setMiddleName(map.get("middleName").toString());
			}

			userDetails.setMobNo(map.get("mobNo").toString());

			/**
			 * check whether rolec is PARTNER_ADMIN, if yes than check same PARTNER_ADMIN
			 * for exact partner exist in the system or not if exist throw exception saying
			 * duplicate PARTNER_ADMIN.
			 */

			List<AssignedDesignations> assignedDesignations = acc.getAssignedDesignations();

			List<String> desgIds = assignedDesignations.stream().map(c -> c.getDesignationIds())
					.collect(Collectors.toList());

			List<Designation> desgList = designationRepository.findByIdIn(desgIds);

			/**
			 * set account status to pending
			 */
			acc.setEnabled(true);
			acc.setExpired(false);
			acc.setLocked(true);

			userDetails.setUserStatus(UserStatus.PENDING);

			acc.setUserDetails(userDetails);

			List<Integer> areaIds = (List<Integer>) map.get("areaId");
			// verify whether areaId provided is exist or not
			List<Area> arIds = areaRepositoy.findByAreaIdIn(areaIds);
			if (!arIds.isEmpty() && arIds.size() == areaIds.size()) {

				userDetails.setAreaId(areaIds);
				acc.setMappedAreaIds(areaIds);
				accountRepository.save(acc);
				return true;
			} else {
				throw new RuntimeException("Key : areaId is invalid");
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Action : While create user with payload {},{}", map, e);
			throw new RuntimeException(e);
		}

	}
	@Override
	public Map<String, Object> sessionMap(Object account) {

		Account acc = (Account) account;

		Map<String, Object> sessionMap = new HashMap<>();

		List<Area> areas = areaRepositoy.findByAreaIdIn(acc.getMappedAreaIds());

		List<Integer> areaIds = areas.stream().map(a->a.getAreaId()).collect(Collectors.toList());
		sessionMap.put("areaIds", acc.getMappedAreaIds());
		sessionMap.put("areaIds", areaIds);
		
		UserDetails userDetails = (UserDetails) acc.getUserDetails();
		
		
		
		List<AssignedDesignations> assignedDesignations = acc.getAssignedDesignations();
		assignedDesignations.stream().filter(v->v.getEnable());
		
		List<String> assDesg = assignedDesignations.stream().map(d->d.getDesignationIds()).collect(Collectors.toList());
		
		sessionMap.put("desg", assDesg);
		
//		sessionMap.put("authorityIds", acc.getAuthorityIds());

		return sessionMap;
	}

	@Override
	public boolean updateAccountDetails(Map<String, Object> map, Object account, Principal p) {

		try {

			Account acc = (Account) account;

			UserDetails userDetails = (UserDetails) acc.getUserDetails();

			userDetails.setFirstName(map.get("firstName").toString());
			userDetails.setGender(map.get("gender").toString().equals("MALE") ? Gender.MALE : Gender.FEMALE);
			userDetails.setLastName(map.get("lastName").toString());
			userDetails.setMobNo(map.get("mobNo").toString());
//			userDetails.setDob(map.get("dob").toString());
			if (map.get("middleName") != null) {
				userDetails.setMiddleName(map.get("middleName").toString());
			}else {
				userDetails.setMiddleName(null);
			}

			// set userDetails to account
			
			/**
			 * set account status to pending
			 */
			acc.setEnabled(true);
			acc.setExpired(false);
			acc.setLocked(false);
			acc.setEmail(map.get("email").toString());
			userDetails.setUserStatus(UserStatus.APPROVED);
			
			acc.setUserDetails(userDetails);
			
			if (map.get("areaId") != null && !map.get("areaId").toString().isEmpty()) {

				acc.setMappedAreaIds(null);

				List<Integer> areaIds = (List<Integer>) map.get("areaId");

				// verify whether areaId provided is exist or not
				List<Area> arIds = areaRepositoy.findByAreaIdIn(areaIds);
				if (!arIds.isEmpty() && arIds.size() == areaIds.size()) {
					acc.setMappedAreaIds(areaIds);
				} else {
					throw new RuntimeException("Key : areaId is invalid");
				}
			}

			if (map.get("designationIds") != null && !map.get("designationIds").toString().isEmpty()) {

				// set assigned designation to null and updated new one
				acc.setAssignedDesignations(null);

				List<String> designationIds = (List<String>) map.get("designationIds");

				List<Designation> designations = designationRepository.findByIdIn(designationIds);

				// check whether the user wanted to create admin user, if yes
				// than does
				// user set the property 'allow.admin.creation' = true
			/*	if ((!configurableEnvironment.containsProperty("allow.admin.creation"))
						|| configurableEnvironment.getProperty("allow.admin.creation").equals("false")) {*/
					designations.forEach(desgs -> {
						if (desgs.getName().equals("ADMIN")) {
							throw new RuntimeException("you do not have permission to update admin user!");
						}
					});
				//}

				// setting multiple AssignedDesignations in account
				List<AssignedDesignations> assDesgList = new ArrayList<>();
				designations.forEach(d -> {

					AssignedDesignations assignedDesignations = new AssignedDesignations();
					assignedDesignations.setDesignationIds(d.getId());
					assDesgList.add(assignedDesignations);
				});
				acc.setAssignedDesignations(assDesgList);
			}

			/*
			 * Audit
			 */
			List<AccountAudit> audits = acc.getChangeHistory();
			List<AccountAudit> accAuditList = new ArrayList<>();

			AccountAudit audit = new AccountAudit();
			ObjectMapper mapper = new ObjectMapper();
			
			acc.setChangeHistory(accAuditList);
			audit.setAccount(mapper.writeValueAsString(acc));
			audit.setAuditBy(p.getName());
			audit.setAuditDate(new Date());

			if (audits != null) {
				audits.add(audit);
			} else {
				audits = new ArrayList<>();
				audits.add(audit);
			}

			acc.setChangeHistory(audits);

			accountRepository.save(acc);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Action : While updating user with payload {},{}", map, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<?> getAllAuthorities() {

		return null;
	}

}
