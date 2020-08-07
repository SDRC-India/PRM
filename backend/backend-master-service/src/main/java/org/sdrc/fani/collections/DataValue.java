package org.sdrc.fani.collections;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/*
 * @author Biswabhusan Pradhan
 * 
 */
@Document
@Data
public class DataValue implements Serializable {

	private static final long serialVersionUID = -1636923412521819247L;
	private String id;
	private Double dataValue;
	private Integer tp;
	private String _case;
	private Integer inid;
	private String numerator;
	private String denominator;
	private Integer datumId;
	private String datumtype;
}