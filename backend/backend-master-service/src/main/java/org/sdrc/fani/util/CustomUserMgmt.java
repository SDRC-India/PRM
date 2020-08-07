package org.sdrc.fani.util;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.sdrc.fani.repositories.CustomAccountRepository;
import org.sdrc.usermgmt.core.util.IUserManagementHandler;
import org.sdrc.usermgmt.exception.EmailMisMatchException;
import org.sdrc.usermgmt.exception.UserAlreadyExistException;
import org.sdrc.usermgmt.model.AuthorityControlType;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.AssignedDesignations;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.sdrc.usermgmt.service.MongoUserManagementServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
@Primary
public class CustomUserMgmt extends MongoUserManagementServiceImpl  {
	
	@Autowired(required = false)
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	@Qualifier("customAccountRepository")
	private CustomAccountRepository customAccountRepository;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired(required = false)
	@Qualifier("mongoDesignationRepository")
	private DesignationRepository designationRepository;
	
	@Autowired(required = false)
	private IUserManagementHandler iuserManagementHandler;
	
	@Caching(evict = {
		    @CacheEvict(value = "userwisearea", allEntries=true),
		    @CacheEvict(value = "usercount", allEntries=true)
		})
	public ResponseEntity<String> createUser(Map<String, Object> map, Principal p) {

		Gson gson = new Gson();

		if (map.get("userName") == null || map.get("userName").toString().isEmpty())
			throw new RuntimeException("key : userName not found in map");

		if (map.get("designationIds") == null || map.get("designationIds").toString().isEmpty())
			throw new RuntimeException("key : designationIds not found in map");

		if (map.get("password") == null || map.get("password").toString().isEmpty())
			throw new RuntimeException("key : password not found in map");

		Account user = customAccountRepository.findByUserName(map.get("userName").toString());

		if (user != null) {

			throw new UserAlreadyExistException(configurableEnvironment.getProperty("username.duplicate.error"));

		}

		Account account = new Account();

		account.setUserName(map.get("userName").toString());
		account.setPassword(bCryptPasswordEncoder.encode(map.get("password").toString()));
		
		
		/**
		 * Pending users: enabled = true; expired = false; Locked = true
		 * Approved users: enabled = true; expired = false; Locked = false
		 * Rejected users: enabled = true; expired = true; Locked = false
		 */

		if (map.get("email") != null && !map.get("email").toString().isEmpty()) {
			Account pendingacc = customAccountRepository.findByEmailAndExpiredFalseAndLockedTrue(map.get("email").toString());
			Account approveacc = customAccountRepository.findByEmailAndExpiredFalseAndLockedFalse(map.get("email").toString());
			if (pendingacc != null || approveacc!=null)
				throw new UserAlreadyExistException(configurableEnvironment.getProperty("email.duplicate.error"));
			account.setEmail(map.get("email").toString());
		}

		List<String> designationIds = (List<String>) map.get("designationIds");

		List<Designation> designations = designationRepository.findByIdIn(designationIds);

		// check whether the user wanted to create admin user, if yes than does
		// user set the property 'allow.admin.creation' = true
		if ((!configurableEnvironment.containsProperty("allow.admin.creation"))
				|| configurableEnvironment.getProperty("allow.admin.creation").equals("false")) {
			designations.forEach(desgs -> {
				if (desgs.getName().equals("ADMIN")) {
					throw new RuntimeException("you do not have permission to create admin user!");
				}
			});
		}

		// setting multiple AssignedDesignations in account
		List<AssignedDesignations> assDesgList = new ArrayList<>();
		designations.forEach(d -> {

			AssignedDesignations assignedDesignations = new AssignedDesignations();
			assignedDesignations.setDesignationIds(d.getId());
			assDesgList.add(assignedDesignations);
		});

		account.setAssignedDesignations(assDesgList);
		
		account.setAuthorityControlType(AuthorityControlType.DESIGNATION);
		iuserManagementHandler.saveAccountDetails(map, account);

		return new ResponseEntity<String>(gson.toJson(configurableEnvironment.getProperty("user.create.success")),
				HttpStatus.OK);
	}
	
	public ResponseEntity<String> updateUser(Map<String, Object> updateUserMap,Principal principal) {

		/**
		 * checking if account-id exist
		 */
		if (updateUserMap.get("id") == null || updateUserMap.get("id").toString().isEmpty()) {
			throw new RuntimeException("key : id not found in updateUserMap");
		}
		
		Account account = customAccountRepository.findOne(updateUserMap.get("id").toString());
		if(account == null) {
			throw new EntityNotFoundException("Cannot find such user with id:" + updateUserMap.get("id").toString()) ;
		}
		
		/**
		 * checking if email-id already exist
		 */
		if (updateUserMap.get("email") != null && !updateUserMap.get("email").toString().isEmpty()) {
			
			Account pendingacc = customAccountRepository.findByEmailAndExpiredFalseAndLockedTrue(updateUserMap.get("email").toString());
			Account approveacc = customAccountRepository.findByEmailAndExpiredFalseAndLockedFalse(updateUserMap.get("email").toString());
			
			if (pendingacc != null && !pendingacc.getId().equals(account.getId()))
				throw new EmailMisMatchException(configurableEnvironment.getProperty("email.duplicate.error"));
			
			if (approveacc != null && !approveacc.getId().equals(account.getId()))
				throw new EmailMisMatchException(configurableEnvironment.getProperty("email.duplicate.error"));
		}

		Gson gson = new Gson();
		
		try {
			iuserManagementHandler.updateAccountDetails(updateUserMap, account, principal);
			//log.info("Action : update-user :  user information updated successfully  for user : {}",account.getUserName());
			return new ResponseEntity<>(gson.toJson(configurableEnvironment.getProperty("user.update.success")),
					HttpStatus.OK);

		} catch (Exception e) {
			//log.error("Action : update user : error : ", e);
			throw new EmailMisMatchException(configurableEnvironment.getProperty("email.duplicate.error"));
		}

	}


}
