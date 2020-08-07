package org.sdrc.fani.models;

import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Data
public class FileModel {

	private String base64;
	
	private String columnName;
	
	private String fileName;
	
	private Long fileSize; 
	
	private String fileType;
	
}
