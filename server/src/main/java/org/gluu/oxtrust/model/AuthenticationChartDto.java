package org.gluu.oxtrust.model;

import java.io.Serializable;

public class AuthenticationChartDto implements Serializable{

	private static final long serialVersionUID = -6376070511032852935L;
	
	String []labels;
	Integer []success;
	Integer []failure;
	Integer totalRequest;
	Integer totalSuccess;
	Integer totalFailure;
	public AuthenticationChartDto(){
		totalRequest =0;
		totalSuccess=0;
		totalFailure=0;
		
	}
	
	public String[] getLabels() {
		return labels;
	}
	public void setLabels(String[] labels) {
		this.labels = labels;
	}
	public Integer[] getSuccess() {
		return success;
	}
	public void setSuccess(Integer[] success) {
		if(success!=null){
			for(Integer number:success){
				totalRequest  = totalRequest + number;
				totalSuccess  = totalSuccess + number;
			}
			
		}
		this.success = success;
	}
	public Integer[] getFailure() {
		
		return failure;
	}
	public void setFailure(Integer[] failure) {
		if(failure!=null){
			for(Integer number:failure){
				totalRequest  = totalRequest + number;
				totalFailure  = totalFailure + number;
			}
			
		}

		this.failure = failure;
	}
	public Integer getTotalRequest() {
		return totalRequest;
	}
	public void setTotalRequest(Integer totalRequest) {
		this.totalRequest = totalRequest;
	}
	public Integer getTotalSuccess() {
		return totalSuccess;
	}
	public void setTotalSuccess(Integer totalSuccess) {
		this.totalSuccess = totalSuccess;
	}
	public Integer getTotalFailure() {
		return totalFailure;
	}
	public void setTotalFailure(Integer totalFailure) {
		this.totalFailure = totalFailure;
	}
	
	
	
}
