package org.sdrc.pmr.collection;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
@CompoundIndexes({
    @CompoundIndex(name = "datavalue", def = "{'areaId' : 1, 'unit': 1, 'inid': 1}")
})
public class DataValue implements Serializable {
	
	private static final long serialVersionUID = -6106292915594868271L;
	
	private String id;
	private Integer areaId;
	private Integer originStateId;
	private Double dataValue;
	private String unit;
	private Integer inid;
	private String numerator;
	private String denominator;
	private Date createdDate;
	private String ntypeDetailId;
}