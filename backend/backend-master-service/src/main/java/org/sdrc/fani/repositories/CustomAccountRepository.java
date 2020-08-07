package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.AssignedDesignations;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository("customAccountRepository")
public interface CustomAccountRepository extends AccountRepository {

	List<Account> findByIdIn(List<String> ids);

	List<Account> findByExpiredFalse();

	Account findByEmailAndExpiredFalse(String email);

	@Query("{ $and: [ {'assignedDesignations' :{$in:?0}},{'userDetails.partnerId' : ?1}]}")
	List<Account> getAssignedDesignationsAndPartnerId(List<AssignedDesignations> assignedDesignations,
			String partnertId);

	@Query("{'userDetails.partnerId' :{$in:?0}}")
	List<Account> findByPartnerIdIn(List<String> partnerId);

	@Query("{ $and: [ {'assignedDesignations' :{$in:?0}}]}")
	List<Account> getAssignedDesignations(List<AssignedDesignations> asList);

	Account findByEmailAndExpiredFalseAndLockedTrue(String string);

	Account findByEmailAndExpiredFalseAndLockedFalse(String string);

}
