package org.sdrc.fani.repositories;

import java.util.Date;

import org.sdrc.fani.collections.CollectionAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CollectionAuditRepository extends MongoRepository<CollectionAudit, String> {

	CollectionAudit findByCollectionName(String string);

	//@Query("{{'collectionName':?0},'updateDate':{ $gte:?1}}")
	/*@Query(value = "{ $and: [{'collectionName' :{$in:?0 }},{'updatedDate':{ $gte:?1}}]}")
	CollectionAudit getByCollectionNameAndLastUpdatedDate(String collectionName, Date date);*/

}
