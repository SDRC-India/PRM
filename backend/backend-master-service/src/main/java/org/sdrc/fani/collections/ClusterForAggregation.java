package org.sdrc.fani.collections;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class ClusterForAggregation  implements Serializable {
	
	private static final long serialVersionUID = 6921206181126815146L;
	
	@Id
	private String id;
	
	private Integer district;
	
	private Integer block;
	
	private Integer village;
	
	private Integer clusterNumber;

}
