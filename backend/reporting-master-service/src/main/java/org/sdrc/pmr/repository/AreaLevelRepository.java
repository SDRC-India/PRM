package org.sdrc.pmr.repository;

import org.sdrc.pmr.collection.AreaLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaLevelRepository extends MongoRepository<AreaLevel, String>{

	AreaLevel findByAreaLevelId(Integer areaLevelId);

	AreaLevel findByAreaLevelName(String string);

}
