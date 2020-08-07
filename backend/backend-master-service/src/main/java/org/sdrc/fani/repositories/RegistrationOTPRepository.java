package org.sdrc.fani.repositories;

import org.sdrc.fani.collections.RegistrationOTP;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RegistrationOTPRepository extends MongoRepository<RegistrationOTP, String> {

	RegistrationOTP findByEmailIdAndIsActiveTrue(String email);

	RegistrationOTP findByEmailIdAndVarificationCodeAndIsActiveTrue(String email, Integer varificationCode);

}
