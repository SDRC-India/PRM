package org.sdrc.fani.controller;
import java.util.Map;

import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.util.TokenInfoExtracter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sarita Panigrahi 
 */

@RestController
@RequestMapping("/me")
public class PrincipalResource {

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;
	
	@GetMapping
	public UserModel getPrincipal(OAuth2Authentication auth) {
		
		return tokenInfoExtracter.getUserModelWithOutRoleDesig(auth);
		
//		return (UserModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	@GetMapping(value="/userWiseArea")
	public Map<String, Integer> userWiseArea() {
	return tokenInfoExtracter.userWiseArea();
	}



	@GetMapping(value="/userCount")
	public Integer userWiseArea(OAuth2Authentication auth) {
	UserModel principal = getPrincipal(auth);
		
	return tokenInfoExtracter.userCount(principal.getDesgnName(), principal.getAreaIds().size() > 1 ? principal.getAreaIds().get(1) : principal.getAreaIds().get(0));
	}
}
