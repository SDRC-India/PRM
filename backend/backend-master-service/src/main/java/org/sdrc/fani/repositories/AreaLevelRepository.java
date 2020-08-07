package org.sdrc.fani.repositories;

import org.sdrc.fani.collections.AreaLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaLevelRepository extends MongoRepository<AreaLevel, String>{

	AreaLevel findByAreaLevelId(Integer areaLevelId);

	AreaLevel findByAreaLevelName(String string);

}
