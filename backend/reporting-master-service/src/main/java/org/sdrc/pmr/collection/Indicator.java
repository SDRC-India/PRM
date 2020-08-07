package org.sdrc.pmr.collection;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class Indicator {
	@Id
	private String _id;
	private Map<String, Object> indicatorDataMap;
}
