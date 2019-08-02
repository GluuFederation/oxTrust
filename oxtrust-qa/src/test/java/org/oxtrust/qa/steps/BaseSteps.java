package org.oxtrust.qa.steps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import atu.testrecorder.ATUTestRecorder;
import cucumber.api.Scenario;

public abstract class BaseSteps {
	public final DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd_HH-mm-ss");
	private ATUTestRecorder recorder;

	public void startRecorder(Scenario scenario) {
		// try {
		// String path = scenario.getName().replace("/", "").replace(" ",
		// "").toLowerCase()
		// + dateFormat.format(new Date());
		// recorder = new ATUTestRecorder(System.getProperty("user.dir") +
		// File.separator + "target/cucumber", path,
		// false);
		// recorder.start();
		// } catch (ATUTestRecorderException e) {
		// System.out.println("Error starting recorder");
		// }

	}

	public void stopRecorder() {
		// try {
		// recorder.stop();
		// } catch (ATUTestRecorderException e) {
		// System.out.println("Error stoping recorder");
		// }
	}

}
