package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.Arrays;

public class AuthenticationChartDto implements Serializable {

	private static final long serialVersionUID = -6376070511032852935L;

	String[] labels;
	Long[] success;
	Long[] failure;
	Long totalRequest;
	Long yearlyRequest;
	Long totalSuccess;
	Long totalFailure;

	public AuthenticationChartDto() {
		totalRequest = 0L;
		totalSuccess = 0L;
		totalFailure = 0L;
		yearlyRequest = 0L;
	}

	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	public Long[] getSuccess() {
		return success;
	}

	public void setSuccess(Long[] success) {
		if (success != null) {
			for (Long number : success) {
				totalRequest = totalRequest + number;
				totalSuccess = totalSuccess + number;
			}

		}
		this.success = success;
	}

	public Long[] getFailure() {

		return failure;
	}

	public void setFailure(Long[] failure) {
		
		if (failure != null) {
			for (Long number : failure) {
				totalRequest = totalRequest + number;
				totalFailure = totalFailure + number;
			}

		}

		this.failure = failure;
	}

	public Long getTotalRequest() {
		return totalRequest;
	}

	public void setTotalRequest(Long totalRequest) {
		this.totalRequest = totalRequest;
	}

	public Long getYearlyRequest() {
		return yearlyRequest;
	}

	public void setYearlyRequest(Long yearlyRequest) {
		this.yearlyRequest = yearlyRequest;
	}

	public Long getTotalSuccess() {
		return totalSuccess;
	}

	public void setTotalSuccess(Long totalSuccess) {
		this.totalSuccess = totalSuccess;
	}

	public Long getTotalFailure() {
		return totalFailure;
	}

	public void setTotalFailure(Long totalFailure) {
		this.totalFailure = totalFailure;
	}

    @Override
    public String toString() {
        return "AuthenticationChartDto [labels=" + Arrays.toString(labels) + ", success=" + Arrays.toString(success)
                + ", failure=" + Arrays.toString(failure) + ", totalRequest=" + totalRequest + ", yearlyRequest="
                + yearlyRequest + ", totalSuccess=" + totalSuccess + ", totalFailure=" + totalFailure + "]";
    }
	

}
