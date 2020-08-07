package org.sdrc.fani;

import org.sdrc.usermgmt.core.annotations.EnableUserManagementWithJWTMongoSecurityConfiguration;
import org.sdrc.usermgmt.core.util.UgmtClientCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Debiprasad Parida (debiprasad@sdrc.co.in) This class enables user-management
 *         with JWT security configuration
 */
@Component
@EnableUserManagementWithJWTMongoSecurityConfiguration
public class Loader {

	@Bean
	public UgmtClientCredentials ugmtClientCredentials() {
		return new UgmtClientCredentials("rmncha", "rmncha@123#!");
	}

}
