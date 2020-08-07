package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.Indicator;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IndicatorRepository extends MongoRepository<Indicator, String> {
	@Query("{'indicatorDataMap.periodicity':?0}")
	List<Indicator> getIndicatorByPeriodicity(String periodcity);

	@Query("{'indicatorDataMap.periodicity':?0, 'indicatorDataMap.parentType':?1}")
	List<Indicator> getPercentageIndicators(String periodicity, String indicatorType);
	
	@Query("{'indicatorDataMap.sector' :{$in:?0 }}")
	List<Indicator> getIndicatorBySectors(List<String> sectors);
	

	@Query("{'indicatorDataMap.sectorId' :{$in:?0 }, 'indicatorDataMap.parentType':?1}")
	List<Indicator> getPercentageSectorWiseIndicators(List<String> sectorIds, String indicatorType);
	
	@Query("{'indicatorDataMap.formId':?0}")
	List<Indicator> getIndicatorByFormId(String formId);

	@Query("{'indicatorDataMap.indicatorNid' :{$in:?0 }}")
	List<Indicator> getIndicatiorsIn(List<String> indicatorIds);

	
	@Query("{'indicatorDataMap.sector' :{$in:?0 }, 'indicatorDataMap.formId' :{$in:?1 }}")
	List<Indicator> getIndicatorBySubSectorsAndFormIdIn(List<String> collect, List<String> formIds);
	
	@Query("{'indicatorDataMap.sector' :{$in:?0 }}")
	List<Indicator> getIndicatorBySubSectors(List<String> sectors);
}
