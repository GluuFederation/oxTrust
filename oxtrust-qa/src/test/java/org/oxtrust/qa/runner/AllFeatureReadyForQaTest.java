package org.oxtrust.qa.runner;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/java/org/oxtrust/qa/features" }, plugin = { "pretty",
		"json:target/cucumber/json/cucumber.json" },glue="", monochrome = true, tags = { "@gluuSE" })
public class AllFeatureReadyForQaTest {

}
