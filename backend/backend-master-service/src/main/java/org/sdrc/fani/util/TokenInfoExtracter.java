
package org.sdrc.fani.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.sdrc.fani.collections.Area;
import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.repositories.AreaRepository;
import org.sdrc.fani.repositories.CustomAccountRepository;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

/**
 * @author Subham Ashish (subham@sdrc.co.in)
 *
 */
@Component
public class TokenInfoExtracter {

	@Autowired(required = false)
	private TokenStore tokenStore;

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private DesignationRepository designationRepository;
	
	@Autowired
	@Qualifier("customAccountRepository")
	private CustomAccountRepository customAccountRepository;
	
	@Autowired
	private MongoTemplate mongoTemplate;


	/*
	 * it retrieves the user-info from JWT token.
	 */
	public Map<String, Object> tokenInfo(OAuth2Authentication auth) {

		OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(details.getTokenValue());
		return accessToken.getAdditionalInformation();

	}

	/*
	 * Extracting the user info from JWT token and setting it to UserModel Object
	 */
	public UserModel getUserModelInfo(OAuth2Authentication auth) {

		Map<String, Object> tokenInfoMap = tokenInfo(auth);

		UserModel user = new UserModel();

		user.setEmailId(tokenInfoMap.get("emailId") != null ? tokenInfoMap.get("emailId").toString() : "");

		user.setUserId(tokenInfoMap.get("userId").toString());

		Set<String> idSet = new HashSet<>();
		Set<String> roleSet = new HashSet<>();

		Map<String, Object> sessionAreaMap = (Map<String, Object>) tokenInfoMap.get("sessionMap");

		StringTokenizer stId = new StringTokenizer(tokenInfoMap.get("designationIds").toString(), ",");

		while (stId.hasMoreTokens()) {
			String nextToken = stId.nextToken();
			nextToken = nextToken.replace("[", "").replace("]", "");

			idSet.add(nextToken);
		}

		StringTokenizer stName = new StringTokenizer(tokenInfoMap.get("designationNames").toString(), ",");

		while (stName.hasMoreTokens()) {
			String nextToken = stName.nextToken();
			nextToken = nextToken.replace("[", "").replace("]", "");
			roleSet.add(nextToken);
		}

		if (idSet.contains("") || roleSet.contains("")) {
			if (sessionAreaMap != null && sessionAreaMap.containsKey("desg")) {

				List<Object> desgList = (List<Object>) sessionAreaMap.get("desg");
				List<Designation> desgs = new ArrayList<>();
				Set<String> desgId = new HashSet<>();
				for (Object a : desgList) {
					desgId.add(a.toString());
				}
				user.setRoleIds(desgId);
				roleSet.clear();
				roleSet.add(
						designationRepository.findById(desgId.stream().collect(Collectors.toList()).get(0)).getName());
				user.setRoles(roleSet);
				tokenInfoMap.put("designationNames", roleSet.stream().collect(Collectors.toList()));
				tokenInfoMap.put("designationIds", desgId);
			}
		} else {
			user.setRoleIds(idSet);
			user.setRoles(roleSet);
		}

		if (sessionAreaMap != null && sessionAreaMap.containsKey("areaIds")) {

			List<Object> areaList = (List<Object>) sessionAreaMap.get("areaIds");
			List<Area> areas = new ArrayList<>();
			List<Integer> areaId = new ArrayList<>();
			for (Object a : areaList) {
				areaId.add((Integer) a);
			}
			user.setAreaIds(areaId);
			user.setAreas(areaRepository.findByAreaIdIn(areaId));
		}

		// extracting designation slug-id from tokenInfoMap
		List<Integer> desgSlugIds = (List<Integer>) (tokenInfoMap.get("desgSlugId"));
		user.setDesgSlugIds(desgSlugIds);
		user.setDesgnName(((List<String>) (tokenInfoMap.get("designationNames"))).get(0));

		// StringTokenizer partnerIdToken = new
		// StringTokenizer(sessionAreaMap.get("partnertId").toString(), ",");

		Set<String> partnerIdS = new HashSet<>();

		/*
		 * while (partnerIdToken.hasMoreTokens()) {
		 * 
		 * String nextToken = partnerIdToken.nextToken();
		 * nextToken=nextToken.replace("[","").replace("]","");
		 * partnerIdS.add(nextToken); }
		 */

		// user.setPartnerId(new ArrayList<>(partnerIdS));
		user.setAreaLevel("STATE");

		return user;
	}

	public UserModel getUserModelWithOutRoleDesig(OAuth2Authentication auth) {

		Map<String, Object> tokenInfoMap = tokenInfo(auth);

		UserModel user = new UserModel();

		user.setEmailId(tokenInfoMap.get("emailId") != null ? tokenInfoMap.get("emailId").toString() : "");

		user.setUserId(tokenInfoMap.get("userId").toString());

		Set<String> idSet = new HashSet<>();
		Set<String> roleSet = new HashSet<>();

		Map<String, Object> sessionAreaMap = (Map<String, Object>) tokenInfoMap.get("sessionMap");

		StringTokenizer stId = new StringTokenizer(tokenInfoMap.get("designationIds").toString(), ",");

		while (stId.hasMoreTokens()) {
			String nextToken = stId.nextToken();
			nextToken = nextToken.replace("[", "").replace("]", "");

			idSet.add(nextToken);
		}

		StringTokenizer stName = new StringTokenizer(tokenInfoMap.get("designationNames").toString(), ",");

		while (stName.hasMoreTokens()) {
			String nextToken = stName.nextToken();
			nextToken = nextToken.replace("[", "").replace("]", "");
			roleSet.add(nextToken);
		}

		if (idSet.contains("") || roleSet.contains("")) {
			if (sessionAreaMap != null && sessionAreaMap.containsKey("desg")) {

				List<Object> desgList = (List<Object>) sessionAreaMap.get("desg");
				List<Designation> desgs = new ArrayList<>();
				Set<String> desgId = new HashSet<>();
				for (Object a : desgList) {
					desgId.add(a.toString());
				}
				user.setRoleIds(desgId);
//				roleSet.clear();
//				roleSet.add(
//						designationRepository.findById(desgId.stream().collect(Collectors.toList()).get(0)).getName());
//				user.setRoles(roleSet);
				tokenInfoMap.put("designationNames", roleSet.stream().collect(Collectors.toList()));
				tokenInfoMap.put("designationIds", desgId);
			}
		} else {
			user.setRoleIds(idSet);
//			user.setRoles(roleSet);
		}

		if (sessionAreaMap != null && sessionAreaMap.containsKey("areaIds")) {

			List<Object> areaList = (List<Object>) sessionAreaMap.get("areaIds");
			List<Area> areas = new ArrayList<>();
			List<Integer> areaId = new ArrayList<>();
			for (Object a : areaList) {
				areaId.add((Integer) a);
			}
			user.setAreaIds(areaId);
//			user.setAreas(areaRepository.findByAreaIdIn(areaId));
		}

		// extracting designation slug-id from tokenInfoMap
		List<Integer> desgSlugIds = (List<Integer>) (tokenInfoMap.get("desgSlugId"));
		user.setDesgSlugIds(desgSlugIds);
		user.setDesgnName(((List<String>) (tokenInfoMap.get("designationNames"))).get(0));

		// StringTokenizer partnerIdToken = new
		// StringTokenizer(sessionAreaMap.get("partnertId").toString(), ",");

		Set<String> partnerIdS = new HashSet<>();

		/*
		 * while (partnerIdToken.hasMoreTokens()) {
		 * 
		 * String nextToken = partnerIdToken.nextToken();
		 * nextToken=nextToken.replace("[","").replace("]","");
		 * partnerIdS.add(nextToken); }
		 */

		// user.setPartnerId(new ArrayList<>(partnerIdS));
		user.setAreaLevel("STATE");

		return user;
	}
	
	@Cacheable(value="userwisearea")
	public Map<String, Integer> userWiseArea() {
		System.out.println("userwisearea from method");
		List<Account> listOfAccount = customAccountRepository.findAll();
		Map<String, Integer> userMap = new HashMap<String, Integer>();
		for (Account account : listOfAccount) {
			if (account.getMappedAreaIds().size() > 1) {
				userMap.put(account.getUserName(), account.getMappedAreaIds().get(1));
			} else {
				userMap.put(account.getUserName(), account.getMappedAreaIds().get(0));
			}

		}

		return userMap;
	}

	@Cacheable(value="usercount",key="{ #desigName, #areaId }")
//	public Integer userCount(OAuth2Authentication auth) {
	public Integer userCount(String desigName, Integer areaId) {
		System.out.println("usercount from method");
		List<Account> listOfAccount = null;
//		UserModel principal = getUserModelWithOutRoleDesig(auth);
//		List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(principal.getRoleIds()));
		MatchOperation match = null;
		Aggregation resultQuery = null;
		if (desigName.equals("STATE LEVEL") || desigName.equals("ADMIN")) {
			match = Aggregation
					.match(Criteria.where("enabled").is(true).and("expired").is(false).and("locked").is(false));
			resultQuery = Aggregation.newAggregation(match);
			listOfAccount = mongoTemplate.aggregate(resultQuery, Account.class, Account.class).getMappedResults();
		} else {

//			List<Integer> districtAreaId = new ArrayList<>();
//			if (principal.getAreaIds().size() > 1) {
//				districtAreaId.add(principal.getAreaIds().get(1));
//			} else {
//				districtAreaId.add(principal.getAreaIds().get(0));
//			}
			match = Aggregation.match(Criteria.where("mappedAreaIds").in(areaId).and("enabled").is(true)
					.and("expired").is(false).and("locked").is(false));
			resultQuery = Aggregation.newAggregation(match);
			listOfAccount = mongoTemplate.aggregate(resultQuery, Account.class, Account.class).getMappedResults();
		}
		return listOfAccount.size();
	}
}
