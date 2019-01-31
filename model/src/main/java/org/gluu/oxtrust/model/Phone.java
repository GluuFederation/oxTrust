package org.gluu.oxtrust.model;

public class Phone {
	private String addedOn;

	private String number;

    private String nickName;

    public String getAddedOn() {
		return addedOn;
	}

	public void setAddedOn(String addedOn) {
		this.addedOn = addedOn;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	@Override
	public String toString() {
		return "Phone [addedOn=" + addedOn + ", number=" + number + ", nickName=" + nickName + "]";
	}


}
