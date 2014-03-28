In order to prepare configuration for oxTrust it's needed to do next steps:
1. Copy folder ./profiles/default into ./profile/new_profile_name.
2. Update properties files inside ./profile/new_profile_name according to environment
3. Execute any maven tasks and specifiy new profile name. Example: mvn -D cfg=new_profile_name compile
4. Copy configuration files from ./target/conf/* into $TOMCAT_HOME/conf folder

Password in default profile is test.
