package com.meiya.alarm.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 */

public class PreFilterRules {

	private String id;

	private String exp;

	private String expReplace;
	
	private long exportCount;

	private Integer addUserId;

	public Integer getAddUserId() {
		return addUserId;
	}

	public void setAddUserId(Integer addUserId) {
		this.addUserId = addUserId;
	}

	// 存放关键字与变量名对照表: key:关键字 value:变量名
	private Map<String, String> varName = new HashMap<>();


	// 默认参数值,value均为false
	private Map<String, Object> defValue = new HashMap<>();

	private String keyExp;

	private String keyExpReplace;

	// 存放关键字与变量名对照表: key:关键字 value:变量名
	private Map<String, String> keyVarName = new HashMap<>();

	// 默认参数值,value均为false
	private Map<String, Object> keyDefValue = new HashMap<>();
	
	private String nodeid;
	
	private String opid;
	
	private String taskid;
	
	private String addTime;
	
	private String expireTime;
	
	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	public String getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(String expireTime) {
		this.expireTime = expireTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	private String updateTime;

	public String getTaskid() {
		return taskid;
	}

	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}

	public String getNodeid() {
		return nodeid;
	}

	public void setNodeid(String nodeid) {
		this.nodeid = nodeid;
	}

	public String getOpid() {
		return opid;
	}

	public void setOpid(String opid) {
		this.opid = opid;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public String getExpReplace() {
		return expReplace;
	}

	public void setExpReplace(String expReplace) {
		this.expReplace = expReplace;
	}

	public Map<String, String> getVarName() {
		return varName;
	}

	public void setVarName(Map<String, String> varName) {
		this.varName = varName;
	}

	public Map<String, Object> getDefValue() {
		return defValue;
	}

	public void setDefValue(Map<String, Object> defValue) {
		this.defValue = defValue;
	}

	public String getKeyExp() {
		return keyExp;
	}

	public void setKeyExp(String keyExp) {
		this.keyExp = keyExp;
	}

	public String getKeyExpReplace() {
		return keyExpReplace;
	}

	public void setKeyExpReplace(String keyExpReplace) {
		this.keyExpReplace = keyExpReplace;
	}

	public Map<String, String> getKeyVarName() {
		return keyVarName;
	}

	public void setKeyVarName(Map<String, String> keyVarName) {
		this.keyVarName = keyVarName;
	}

	public Map<String, Object> getKeyDefValue() {
		return keyDefValue;
	}

	public void setKeyDefValue(Map<String, Object> keyDefValue) {
		this.keyDefValue = keyDefValue;
	}
	
	public long getExportCount() {
		return exportCount;
	}

	public void setExportCount(long exportCount) {
		this.exportCount = exportCount;
	}
	
	@Override
	public String toString() {
		return getId() + "~~" + getExp() + "~~" + getKeyExp() + "~~" + nodeid + opid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
