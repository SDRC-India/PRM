package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.AreaLevel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends MongoRepository<Area, String> {

	Area findByAreaNameAndAreaLevel(String areaName, AreaLevel areaLevel);

	List<Area> findByParentAreaIdOrderByAreaName(Integer parentAreaId);

	List<Area> findByAreaIdOrderByAreaName(Integer parentAreaId);

	List<Area> findByAreaLevel(AreaLevel areaLevel);

	@Cacheable
	@Query(value = "{}", fields = "{areaId : 1, areaName : 1}")
	List<Area> findAreaIdAndAreaName();

	List<Area> findByAreaLevelAreaLevelIdAndBlockId(int i, Integer areaId);

	List<Area> findByAreaLevelAreaLevelIdAndDistrictId(int i, Integer areaId);

	Area findByAreaCode(String parentAreaId);

	Area findByAreaNameAndAreaLevelAreaLevelIdAndParentAreaId(String trim, int i, Integer areaId);

	Area findByAreaId(Integer areaId);

	List<Area> findByAreaLevelAreaLevelIdAndAreaId(int i, Integer areaId);

	List<Area> findByAreaIdIn(List<Integer> mappedAreaIds);

	List<Area> findAllByAreaLevelAreaLevelIdInOrderByAreaIdAsc(List<Integer> asList);

	List<Area> findByParentAreaId(Integer areaLevelId);

	List<Area> findByAreaLevelAreaLevelId(Integer levelId);

	List<Area> findByStateId(Integer areaId);

	List<Area> findAllByOrderByAreaNameAsc();

	List<Area> findByAreaNameIn(List<String> mappedAreaName);

	List<Area> findAllByAreaLevelAreaLevelIdInOrderByAreaNameAsc(List<Integer> asList);

	List<Area> findByStateIdAndAreaLevelAreaLevelIdOrderByAreaIdAsc(Integer stateId, int levelId);

	List<Area> findByDistrictIdAndAreaLevelAreaLevelIdOrderByAreaIdAsc(Integer districtId, int levelId);

	List<Area> findByBlockIdAndAreaLevelAreaLevelIdOrderByAreaIdAsc(Integer blockId, int levelId);

	List<Area> findByAreaIdAndAreaLevelAreaLevelIdOrderByAreaIdAsc(Integer blockId, int levelId);

	List<Area> findByAreaLevelAreaLevelIdAndStateId(int i, Integer areaId);

	List<Area> findAllByAreaLevelAreaLevelIdIn(List<Integer> asList);

	List<Area> findByAreaLevelAreaLevelIdOrderByAreaIdAsc(int i);
	
	List<Area> findByParentAreaIdInAndAreaLevelAreaLevelIdOrderByAreaNameAsc(List<Integer> parentIds, int levelId);

	List<Area> findAllByAreaLevelAreaLevelIdInAndParentAreaIdOrderByAreaNameAsc(List<Integer> asList, int parentAreaId);
}
