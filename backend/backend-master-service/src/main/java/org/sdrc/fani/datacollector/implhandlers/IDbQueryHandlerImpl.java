package org.sdrc.fani.datacollector.implhandlers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.servlet.http.HttpSession;

import org.sdrc.fani.collections.AiimsMasterData;
import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.AreaLevel;
import org.sdrc.fani.repositories.AiimsMasterDataRepository;
import org.sdrc.fani.repositories.AreaLevelRepository;
import org.sdrc.fani.repositories.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import in.co.sdrc.sdrcdatacollector.document.Question;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.handlers.IDbQueryHandler;
import in.co.sdrc.sdrcdatacollector.models.OptionModel;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;

/**
 * @author Debiprasad Parida (debiprasad@sdrc.co.in)
 * @author subham
 */
@Component
public class IDbQueryHandlerImpl implements IDbQueryHandler {

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private AreaLevelRepository areaLevelRepository;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	private AiimsMasterDataRepository aiimsMasterDataRepository;

	@Override
	public List<OptionModel> getOptions(QuestionModel questionModel, Map<Integer, TypeDetail> typeDetailsMap,
			Question question, String checkedValue, Object user1, Map<String, Object> paramKeyValueMap) {

		List<OptionModel> listOfOptions = new ArrayList<>();
		String tableName = questionModel.getTableName().split("\\$\\$")[0].trim();
		String areaLevel = "";
		if (tableName.equals("area"))
			areaLevel = questionModel.getTableName().split("\\$\\$")[1].trim().split("=")[1];
		List<Area> areas = null;

		switch (tableName) {

		case "area": {
			// listOfOptions = new ArrayList<>();
			switch (areaLevel) {

			case "state": {
				areas = areaRepository.findByAreaLevelAreaLevelIdOrderByAreaIdAsc(1);
			}
				break;

			case "district":

			{
				areas = areaRepository.findByAreaLevelAreaLevelIdOrderByAreaIdAsc(2);
			}

				break;
			case "tahasil": {
				areas = areaRepository.findByAreaLevelAreaLevelIdOrderByAreaIdAsc(3);
				areas.add(areaRepository.findByAreaId(600005));
			}
				break;

			case "block": {
				areas = areaRepository.findByAreaLevelAreaLevelIdOrderByAreaIdAsc(4);
				areas.add(areaRepository.findByAreaId(600001));
			}
				break;
			case "ulb": {
				areas = areaRepository.findByAreaLevelAreaLevelIdOrderByAreaIdAsc(5);
				areas.add(areaRepository.findByAreaId(600002));
			}

				break;

			case "shelter home": {
				//areas = areaRepository.findByAreaLevelAreaLevelIdOrderByAreaIdAsc(6);
				//areas.add(areaRepository.findByAreaId(16752));
			}

				break;

			}

			if (areas != null) {

				int order = 0;
				for (Area area : areas) {

					Map<String, Object> extraKeyMap = new HashMap<>();
					OptionModel optionModel = new OptionModel();
					/*
					 * //9, 10, 11, 12, 13, 14, 15, 16, 17, 57, 58, 59
					 */
				
						optionModel.setKey(area.getAreaId());
						optionModel.setValue(area.getAreaName());
						optionModel.setOrder(order++);
						if (area.getAreaLevel() != null) {
							optionModel.setParentId(area.getParentAreaId());
							optionModel.setLevel(area.getAreaLevel().getAreaLevelId());
						} else {
							optionModel.setParentId2(-2);
						}
						optionModel.setVisible(true);
						extraKeyMap.put("state_id", area.getStateId());
						extraKeyMap.put("district_id", area.getDistrictId());
						extraKeyMap.put("tahasil_id", area.getTahasilId());
						extraKeyMap.put("block_id", area.getBlockId());

						optionModel.setExtraKeyMap(extraKeyMap);
						listOfOptions.add(optionModel);
					
					questionModel.setOptions(listOfOptions);
				}

			}

		}
			break;
		case "areaLevel": {
			List<AreaLevel> areaLevels = areaLevelRepository.findAll();
			areaLevels.removeIf(findFacilityLevel(configurableEnvironment.getProperty("facility.area.level.name"))); // remove

			for (AreaLevel areaLvl : areaLevels) {
				OptionModel optionModel = new OptionModel();
				optionModel.setKey(areaLvl.getAreaLevelId());
				optionModel.setValue(areaLvl.getAreaLevelName());
				listOfOptions.add(optionModel);
			}
			questionModel.setOptions(listOfOptions);
		}
			break;

		case "aiimsMasterData": {
			List<AiimsMasterData> aiimsMasterDatas = aiimsMasterDataRepository.findAll();
			int order = 0;
			for (AiimsMasterData data : aiimsMasterDatas) {
				Map<String, Object> extraKeyMap = new LinkedHashMap<>();
				OptionModel optionModel = new OptionModel();
				optionModel.setKey(data.getSiNo());
				optionModel.setValue(data.getAdmissionId());
				optionModel.setOrder(order++);
//				extraKeyMap.put(data.getAdmissionId(), data);
				if (questionModel.getFormId() == 43) {
					Map<String, String> qmap = new LinkedHashMap<>();
					qmap.put("ColumnName", "Fq01");
					qmap.put("Value", data.getDistName());
					extraKeyMap.put("District", qmap);

					Map<String, String> qmap1 = new LinkedHashMap<>();
					qmap1.put("ColumnName", "Fq02");
					qmap1.put("Value", data.getBlockName());
					extraKeyMap.put("Block", qmap1);

					Map<String, String> qmap2 = new LinkedHashMap<>();
					qmap2.put("ColumnName", "Fq03");
					qmap2.put("Value", data.getSubCenterName());
					extraKeyMap.put("Sub Center", qmap2);

					Map<String, String> qmap3 = new LinkedHashMap<>();
					qmap3.put("ColumnName", "Fq04");
					qmap3.put("Value", data.getVillageName());
					extraKeyMap.put("Village", qmap3);

					Map<String, String> qmap4 = new LinkedHashMap<>();
					qmap4.put("ColumnName", "Fq05");
					qmap4.put("Value", data.getCurrentAddress());
					extraKeyMap.put("Current Address", qmap4);

					Map<String, String> qmap5 = new LinkedHashMap<>();
					qmap5.put("ColumnName", "Fq06");
					qmap5.put("Value", data.getSNCUName());
					extraKeyMap.put("SNCU Name", qmap5);

					String value;
					value = (data.getAdmissionDate() != null && !data.getAdmissionDate().equals("")
							? (new SimpleDateFormat("dd/MM/yyyy").format(data.getAdmissionDate()))
							: null);
					Map<String, String> qmap6 = new LinkedHashMap<>();
					qmap6.put("ColumnName", "Fq07");
					qmap6.put("Value", value);
					extraKeyMap.put("Admission  Date", qmap6);

					value = (data.getDob() != null && !data.getDob().equals("")
							? (new SimpleDateFormat("dd/MM/yyyy").format(data.getDob()))
							: null);
					Map<String, String> qmap7 = new LinkedHashMap<>();
					qmap7.put("ColumnName", "Fq08");
					qmap7.put("Value", value);
					extraKeyMap.put("Date of Birth", qmap7);

					Map<String, String> qmap8 = new LinkedHashMap<>();
					qmap8.put("ColumnName", "Fq09");
					qmap8.put("Value", data.getMothersName());
					extraKeyMap.put("Mother's Name", qmap8);

					Map<String, String> qmap9 = new LinkedHashMap<>();
					qmap9.put("ColumnName", "Fq10");
					qmap9.put("Value", data.getFathersName());
					extraKeyMap.put("Father's Name", qmap9);

					Map<String, String> qmap10 = new LinkedHashMap<>();
					qmap10.put("ColumnName", "Fq11");
					qmap10.put("Value", data.getContactNo().toString());
					extraKeyMap.put("Contact Number", qmap10);

					Map<String, String> qmap11 = new LinkedHashMap<>();
					qmap11.put("ColumnName", "Fq12");
					qmap11.put("Value", data.getCommWorkerName());
					extraKeyMap.put("Community Worker Name", qmap11);

					Map<String, String> qmap12 = new LinkedHashMap<>();
					qmap12.put("ColumnName", "Fq13");
					qmap12.put("Value", data.getWorkerContactNo().toString());
					extraKeyMap.put("Worker Contact Number", qmap12);

					Map<String, String> qmap13 = new LinkedHashMap<>();
					qmap13.put("ColumnName", "Fq14");
					qmap13.put("Value", data.getOutcome());
					extraKeyMap.put("Outcome", qmap13);

					Map<String, String> qmap14 = new LinkedHashMap<>();
					qmap14.put("ColumnName", "Fq15");
					qmap14.put("Value", data.getMaturity());
					extraKeyMap.put("Maturity", qmap14);

					Map<String, String> qmap15 = new LinkedHashMap<>();
					qmap15.put("ColumnName", "Fq16");
					qmap15.put("Value", data.getWeight().toString());
					extraKeyMap.put("Weight (in Kg)", qmap15);

					Map<String, String> qmap16 = new LinkedHashMap<>();
					qmap16.put("ColumnName", "Fq17");
					value = (data.getDischarge() != null && !data.getDischarge().equals("")
							? (new SimpleDateFormat("dd/MM/yyyy").format(data.getDischarge()))
							: null);
					qmap16.put("Value", value);
					extraKeyMap.put("Discharge", qmap16);

					Map<String, String> qmap17 = new LinkedHashMap<>();
					qmap17.put("ColumnName", "Fq18");
					qmap17.put("Value", data.getIndicationOfAdmission());
					extraKeyMap.put("Indication of Admission", qmap17);
					optionModel.setExtraKeyMap(extraKeyMap);
					listOfOptions.add(optionModel);
				}
				if (questionModel.getFormId() == 44) {

					Map<String, String> qmap = new LinkedHashMap<>();
					qmap.put("ColumnName", "Fqad01");
					qmap.put("Value", data.getDistName());
					extraKeyMap.put("District", qmap);

					Map<String, String> qmap1 = new LinkedHashMap<>();
					qmap1.put("ColumnName", "Fqad02");
					qmap1.put("Value", data.getBlockName());
					extraKeyMap.put("Block", qmap1);

					Map<String, String> qmap2 = new LinkedHashMap<>();
					qmap2.put("ColumnName", "Fqad03");
					qmap2.put("Value", data.getSubCenterName());
					extraKeyMap.put("Sub Center", qmap2);

					Map<String, String> qmap3 = new LinkedHashMap<>();
					qmap3.put("ColumnName", "Fqad04");
					qmap3.put("Value", data.getVillageName());
					extraKeyMap.put("Village", qmap3);

					Map<String, String> qmap4 = new LinkedHashMap<>();
					qmap4.put("ColumnName", "Fqad05");
					qmap4.put("Value", data.getCurrentAddress());
					extraKeyMap.put("Current Address", qmap4);

					Map<String, String> qmap5 = new LinkedHashMap<>();
					qmap5.put("ColumnName", "Fqad06");
					qmap5.put("Value", data.getSNCUName());
					extraKeyMap.put("SNCU Name", qmap5);

					String value;
					value = (data.getAdmissionDate() != null && !data.getAdmissionDate().equals("")
							? (new SimpleDateFormat("dd/MM/yyyy").format(data.getAdmissionDate()))
							: null);
					Map<String, String> qmap6 = new LinkedHashMap<>();
					qmap6.put("ColumnName", "Fqad07");
					qmap6.put("Value", value);
					extraKeyMap.put("Admission  Date", qmap6);

					value = (data.getDob() != null && !data.getDob().equals("")
							? (new SimpleDateFormat("dd/MM/yyyy").format(data.getDob()))
							: null);
					Map<String, String> qmap7 = new LinkedHashMap<>();
					qmap7.put("ColumnName", "Fqad08");
					qmap7.put("Value", value);
					extraKeyMap.put("Date of Birth", qmap7);

					Map<String, String> qmap8 = new LinkedHashMap<>();
					qmap8.put("ColumnName", "Fqad09");
					qmap8.put("Value", data.getMothersName());
					extraKeyMap.put("Mother's Name", qmap8);

					Map<String, String> qmap9 = new LinkedHashMap<>();
					qmap9.put("ColumnName", "Fqad10");
					qmap9.put("Value", data.getFathersName());
					extraKeyMap.put("Father's Name", qmap9);

					Map<String, String> qmap10 = new LinkedHashMap<>();
					qmap10.put("ColumnName", "Fqad11");
					qmap10.put("Value", data.getContactNo().toString());
					extraKeyMap.put("Contact Number", qmap10);

					Map<String, String> qmap11 = new LinkedHashMap<>();
					qmap11.put("ColumnName", "Fqad12");
					qmap11.put("Value", data.getCommWorkerName());
					extraKeyMap.put("Community Worker Name", qmap11);

					Map<String, String> qmap12 = new LinkedHashMap<>();
					qmap12.put("ColumnName", "Fqad13");
					qmap12.put("Value", data.getWorkerContactNo().toString());
					extraKeyMap.put("Worker Contact Number", qmap12);

					Map<String, String> qmap13 = new LinkedHashMap<>();
					qmap13.put("ColumnName", "Fqad14");
					qmap13.put("Value", data.getOutcome());
					extraKeyMap.put("Outcome", qmap13);

					Map<String, String> qmap14 = new LinkedHashMap<>();
					qmap14.put("ColumnName", "Fqad15");
					qmap14.put("Value", data.getMaturity());
					extraKeyMap.put("Maturity", qmap14);

					Map<String, String> qmap15 = new LinkedHashMap<>();
					qmap15.put("ColumnName", "Fqad16");
					qmap15.put("Value", data.getWeight().toString());
					extraKeyMap.put("Weight (in Kg)", qmap15);

					Map<String, String> qmap16 = new LinkedHashMap<>();
					qmap16.put("ColumnName", "Fqad17");
					value = (data.getDischarge() != null && !data.getDischarge().equals("")
							? (new SimpleDateFormat("dd/MM/yyyy").format(data.getDischarge()))
							: null);
					qmap16.put("Value", value);
					extraKeyMap.put("Discharge", qmap16);

					Map<String, String> qmap17 = new LinkedHashMap<>();
					qmap17.put("ColumnName", "Fqad18");
					qmap17.put("Value", data.getIndicationOfAdmission());
					extraKeyMap.put("Indication of Admission", qmap17);
					optionModel.setExtraKeyMap(extraKeyMap);
					listOfOptions.add(optionModel);

				}
			}
			questionModel.setOptions(listOfOptions);

		}
		}

		return listOfOptions;

	}

	public Predicate<AreaLevel> findFacilityLevel(String facility) {
		return f -> f.getAreaLevelName().equalsIgnoreCase(facility);
	}

	@Override
	public String getDropDownValueForRawData(String tableName, Integer dropdownId) {
		// TODO Auto-generated method stub
		return null;
	}

	public QuestionModel setValueForTextBoxFromExternal(QuestionModel questionModel, Question question,
			Map<String, Object> paramKeyValMap, HttpSession session, Object user) {
		return questionModel;
		//
		// String featureName = questionModel.getFeatures();
		// UserModel userModel = (UserModel) user;
		// if (featureName != null &&
		// featureName.contains("fetch_from_external")) {
		// for (String feature : featureName.split("@AND")) {
		// switch (feature.split(":")[0]) {
		// case "fetch_from_external": {
		// switch (feature.split(":")[1]) {
		// case "supervisor_name":
		// questionModel.setValue(userModel.getFirstName()+"
		// "+userModel.getLastName());
		// break;
		// case "organization":
		// questionModel.setValue(userModel.getOrgName());
		// break;
		// case "designation":
		// questionModel.setValue(userModel.getDesgnName());
		// break;
		// case "level":
		// questionModel.setValue(userModel.getAreaLevel());
		// break;
		// case "N/A":
		// questionModel.setValue("N/A");
		// }
		//
		// }
		// break;
		//
		// }
		// }
		// }
		//
		// return questionModel;
		//
	}

}
