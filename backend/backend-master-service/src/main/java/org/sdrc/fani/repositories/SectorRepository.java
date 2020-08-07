package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.Sector;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SectorRepository extends MongoRepository<Sector, String> {

	Sector findTopByOrderByIdDesc();

	List<Sector> findAllByOrderByOrderAsc();

	List<Sector> findByFormIdIn(List<Integer> formId);

	List<Sector> findBySectorNameIn(List<String> sectorName);

	List<Sector> findByFormIdInAndSectorName(List<Integer> formId, String sectorNames);

}
