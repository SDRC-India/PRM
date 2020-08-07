package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.ClusterDataValue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterDataValueRepository extends MongoRepository<ClusterDataValue, String>{

	List<ClusterDataValue> findTop12ByAreaIdAndInidInOrderByTpDesc(Integer areaId, List<Integer> trendGroupIndicatorIds);

	List<ClusterDataValue> findByAreaIdAndTpAndInidIn(Integer areaId, Integer tpId, List<Integer> indicatorIds);

	List<ClusterDataValue> findByAreaIdInAndTpAndInidIn(List<Integer> areaIds, Integer tpId, List<Integer> asList);

	List<ClusterDataValue> findByAreaIdIsAndTpAndInidIn(Integer areaId, Integer tpId, List<Integer> asList);

}
