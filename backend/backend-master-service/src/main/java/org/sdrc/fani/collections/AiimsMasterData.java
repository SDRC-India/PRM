package org.sdrc.fani.collections;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class AiimsMasterData {
	
	@Id
	private String admissionId;
	private Integer siNo;
	private String distName;
	private String blockName;
	private String subCenterName;
	private String villageName;
	private String currentAddress;
	private String sNCUName;
	private Date admissionDate;
	private Date dob;
	private String mothersName;
	private String fathersName;
	private Long contactNo;
	private String commWorkerName;
	private Long workerContactNo;
	private String outcome;
	private String	maturity;
	private Double weight;
	private Date discharge;
	private String indicationOfAdmission;
	

}
