If you want to run tests using the Eclipse TestNG plugin, you'll need to add
these jars to the top of your TestNG classpath. Using the Run Dialog, select the
XML suite to run, and select these entries from the project tree:

/lib/test/jboss-embedded-all.jar
/lib/test/hibernate-all.jar
/lib/test/thirdparty-all.jar
/lib/jboss-embedded-api.jar
/lib/jboss-deployers-client-spi.jar
/lib/jboss-deployers-core-spi.jar

You also need to add the Embedded JBoss bootstrap folder, which you can do by
clicking on the Advanced... button.

/bootstrap

Seam uses JBoss Embedded in its unit and integration testing. This has an
additional requirement when using JDK 6. Add the following VM argument to the VM
args tab in the TestNG launch configuration for your suite.

-Dsun.lang.ClassLoader.allowArraySyntax=true 

Please be sure to use JDK 6 Update 4 or better (>= 1.6.0_04) if you are using
JDK 6. The Update 4 release upgraded to JAXB 2.1 which removes a problem with
earlier versions of Sun's JDK 6 which required overriding the JAXB libraries
using the endorsed directory. 

To add tests to your project create a TestNG xml descriptor called *Test.xml
(e.g. FooTest.xml) next to your test classes and run ant test.
