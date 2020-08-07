package org.sdrc.fani.models;

import java.util.List;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
public class UpdateUserModel {

	private Integer siNo;

	private String id;

	private String firstName;

	private String middleName;

	private String lastName;

	private String dob;

	private Gender gender;

	private String mobNo;

	private List<String> partnerId;

	private String subPartnertId;

	private List<String> roleId;

	private List<Integer> areaId;

	private List<String> areaName;

	private List<String> roleNames;

}
