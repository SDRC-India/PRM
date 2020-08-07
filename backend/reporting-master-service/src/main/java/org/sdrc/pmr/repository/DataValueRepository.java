package org.sdrc.pmr.repository;

import java.util.List;

import org.sdrc.pmr.collection.DataValue;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataValueRepository extends MongoRepository<DataValue, String> {

	List<DataValue> findByAreaId(Integer areaId);
	
	List<DataValue> findByInidAndAreaIdIn(Integer inid, List<Integer> districtIds);
	
	List<DataValue> findByAreaIdAndInidIn(Integer areaId, List<Integer> inids);
	
	DataValue findByInidAndAreaIdAndOriginStateId(Integer inid, Integer areaId, Integer originStateId);
	
	List<DataValue> findByInidAndOriginStateIdAndAreaIdIn(Integer inid, Integer originStateId, List<Integer> districtIds);
}
