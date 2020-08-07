package org.sdrc.pmr.repository;

import java.util.Date;
import java.util.List;

import org.sdrc.pmr.collection.TimePeriod;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TimePeriodRepository extends MongoRepository<TimePeriod, String> {

	List<TimePeriod> findByPeriodicity(String i);

//	@Query("{'startDate':{$gte:?0},'endDate':{$lte:?0},'periodicity':?1}")
	@Query("{'startDate':{$lte:?0},'endDate':{$gte:?0},periodicity:?1}")
	TimePeriod getCurrentTimePeriod(Date createdDate, String periodicity);
	
	TimePeriod findByTimePeriodId(Integer timePeriodId);
	
	List<TimePeriod> findTop12ByPeriodicityOrderByTimePeriodIdDesc(String periodicity);

	TimePeriod findByFinancialYearAndTimePeriodAndPeriodicity(String string, String string2, String string3);

	List<TimePeriod> findTop6ByPeriodicityOrderByTimePeriodIdDesc(String string);
	
	TimePeriod findTop1ByPeriodicityOrderByTimePeriodIdDesc(String i);
}