package org.sdrc.pmr.datacollector.implhandlers;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import in.co.sdrc.sdrcdatacollector.document.Question;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.handlers.IDbQueryHandler;
import in.co.sdrc.sdrcdatacollector.models.OptionModel;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;

/**
 * @author Sarita
 */
@Component
public class IDbQueryHandlerImpl implements IDbQueryHandler {

	@Override
	public List<OptionModel> getOptions(QuestionModel questionModel, Map<Integer, TypeDetail> typeDetailsMap,
			Question question, String checkedValue, Object user, Map<String, Object> paramKeyValueMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDropDownValueForRawData(String tableName, Integer dropdownId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QuestionModel setValueForTextBoxFromExternal(QuestionModel qModel, Question question,
			Map<String, Object> paramKeyValMap, HttpSession session, Object user) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
