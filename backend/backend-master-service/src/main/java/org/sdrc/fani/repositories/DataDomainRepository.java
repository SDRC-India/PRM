package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.DataValue;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataDomainRepository extends MongoRepository<DataValue, String> {
	
	List<DataValue> findByDatumIdInAndTpAndInid(List<Integer> areaIds, Integer tpId, Integer indicatorId);

	List<DataValue> findByDatumIdInAndTpAndInidIs(List<Integer> areaId, Integer tpId, Integer indicatorId);

	List<DataValue> findByDatumIdInAndTpAndInidIn(List<Integer> areaId, Integer tpId, List<Integer> indicatorIds);
	

}
