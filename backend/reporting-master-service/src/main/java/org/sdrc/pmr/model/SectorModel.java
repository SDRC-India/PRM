package org.sdrc.pmr.model;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
@Data
public class SectorModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2468543712385920492L;
	private String sectorName;
	private Integer sectorId;
	private String timePeriod;
	private List<SubSectorModel> subSectors;
}
