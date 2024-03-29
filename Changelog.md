## Changelog
### 3.5.116 (17th of May, 2022)
- `ADDED` - Added No data/ Not set Condition

### 3.5.109 (28th of Feb, 2022)
- `FIXED` - Removed special character bug
- `FIXED` - Fixed public report URL bug for Functional GUI

### 3.4.102 (17th of Dec, 2021)
- `FIXED` - Removed log4j and added java logs

### 3.4.* (upcoming)
- `FIXED` - broken UI when user has test with double quotes in test name

### 3.3.135 (27th of June, 2018)
- `ADDED` - workspace selection
- Updated plugin compatibility to the latest Bamboo version (6.6.0)

### 3.3.132 (4th of April, 2018)
- `ADDED` - link to BlazeMeter report 

_Limitations: If Build in progress state (does not finished or interrupted) 'BlazeMeter Report tab' will not display. Also this tab does not display for Job builds, that does not contains BlazeMeter Build Step_

### 3.2 (15th of March, 2018)
- `FIXED` - bzm-log file contains logs only from one Master
- `CHANGES` - update blazemeter-api-client dependency version from `1.2` to `1.8` ([changelog](https://github.com/Blazemeter/blazemeter-api-client/wiki/Changelog))

### 3.1 (15th of January, 2018)
* Migrating to [BlazeMeter Api Client](https://github.com/Blazemeter/blazemeter-api-client)
* Added log file for server part of plugin. It's located in ${CATALINA_HOME}/logs/bzm-log

##### Officially supported/verified setup:

Bamboo version 6.3.0
Server - Ubuntu 16.04.3; java version "1.8.0_131"
Agent - CentOS 7, java version "1.8.0_131"; atlassian-bamboo-agent-installer-6.3.0.jar

### 3.0 (26th of September, 2017)

##### Officially supported/verified setup:

Bamboo version 6.3.0
Server - Ubuntu 14.04.5; java version "1.8.0_131"
Agent - CentOS 7, java version "1.8.0_131"; atlassian-bamboo-agent-installer-6.3.0.jar

##### Features:

* Migrating to [Basic Authentication API keys](https://guide.blazemeter.com/hc/en-us/articles/115002213289-BlazeMeter-API-keys--BlazeMeter-API-keys)  
* [Remote agents support](https://confluence.atlassian.com/bamboo/bamboo-remote-agent-installation-guide-289276832.html);

* [OkHttp](square.github.io/okhttp/) migration;
* Junit/Jtl report downloading from BlazeMeter server;
* Pushing notes/jmeter properties to test session;


##### Known issues:

* If test is stopped/aborted on Bamboo side then certain logs will not show up in Bamboo




### 2.0.20 (13th of April, 2016)

* Supports single tests and tests collections;  
* Marks build status according to test result.  
* [Pipeline](https://jenkins.io/doc/book/pipeline/) support;  
