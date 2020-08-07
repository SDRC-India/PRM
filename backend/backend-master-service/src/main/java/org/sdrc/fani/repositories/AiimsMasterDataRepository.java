package org.sdrc.fani.repositories;

import org.sdrc.fani.collections.AiimsMasterData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiimsMasterDataRepository extends MongoRepository<AiimsMasterData, String> {

	AiimsMasterData findBySiNo(Integer slNo);

}
