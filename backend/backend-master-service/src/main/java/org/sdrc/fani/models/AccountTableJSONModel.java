package org.sdrc.fani.models;

import java.util.List;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
public class AccountTableJSONModel {

	private List<String> tableColumn;

	private Object tableData;
}
