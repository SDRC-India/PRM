package org.sdrc.fani.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.co.sdrc.sdrcdatacollector.document.TypeDetail;


//@Repository("customTypeDetailRepository")
public interface CustomTypeDetailRepository extends MongoRepository<TypeDetail, String>{

	TypeDetail findById(String devPartner);

	List<TypeDetail> findByNameInAndTypeTypeName(List<String> names, String typeName);
	
	TypeDetail findByFormIdAndSlugId(Integer formId, Integer slugId);
	
}
