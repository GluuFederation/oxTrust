package org.oxtrust.qa.pages.configuration;


import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class ViewLogFilePage  extends AbstractPage {

	WebElement listBody;
	List<WebElement> listItems;
	WebElement foundScope;

	public void assertLogFilesExist(String oxTrustLogs, String oxAuthLogs) {
		Assert.assertTrue(assertLogFilesExistInList(oxTrustLogs,oxAuthLogs));
	}
	
	private void getListItems() {
		WebElement form = webDriver.findElement(By.name("logViewForm"));
		listBody = form.findElement(By.className("rf-p-b"));
		listItems = listBody.findElements(By.tagName("div"));

		
	}

	private boolean assertLogFilesExistInList(String oxTrustLogs, String oxAuthLogs) {
		getListItems();
		boolean found = false,foundoxTrustLogs = false,foundoxAuthLogs = false;
		for (WebElement element : listItems) {
			if (element.getText().contains(oxTrustLogs)) {
				System.out.print(element.getText());
				foundoxTrustLogs = true;
			}
			if (element.getText().contains(oxAuthLogs)) {
				foundoxAuthLogs = true;
				System.out.print(element.getText());
				
			}
		}
		if(foundoxAuthLogs && foundoxTrustLogs){
			found= true;
		}
	
		return found;
	}

	
}
