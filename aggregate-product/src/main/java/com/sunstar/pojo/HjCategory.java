package com.sunstar.pojo;

import java.util.Date;

public class HjCategory {

	private Integer clsId;

	private Integer parentId;

	private String clsName;

	private String sts;

	private Date stsTime;

	private String clsLevel;

	private Integer priority;
	
	private String url;

	public Integer getClsId() {
		return clsId;
	}

	public void setClsId(Integer clsId) {
		this.clsId = clsId;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public String getClsName() {
		return clsName;
	}

	public void setClsName(String clsName) {
		this.clsName = clsName;
	}

	public String getSts() {
		return sts;
	}

	public void setSts(String sts) {
		this.sts = sts;
	}

	public Date getStsTime() {
		return stsTime;
	}

	public void setStsTime(Date stsTime) {
		this.stsTime = stsTime;
	}

	public String getClsLevel() {
		return clsLevel;
	}

	public void setClsLevel(String clsLevel) {
		this.clsLevel = clsLevel;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "HjCategory [clsId=" + clsId + ", parentId=" + parentId + ", clsName=" + clsName + ", sts=" + sts + ", stsTime=" + stsTime + ", clsLevel=" + clsLevel + ", priority=" + priority + ", url=" + url + "]";
	}
}
