## gitlog-maven-plugin


Available Parameters:

| Parameter Name | default value   | type  |
| ---------------|-----------------| -----|
| fileName       | gitlog-embedded | String |
| commitsSince   | null            |   Date |
| commitExtent   | null            |    Integer |
|reportTitle|GIT RELEASE NOTES|String|
|reportName|Release Notes|String|
|reportDescription|Generate changelog from git SCM|String|


>**Note:** If both 'commitsSince' and 'commitExtent' are provided then 'commitsSince' will be ignored.

>'project.issueManagement.system' and 'project.issueManagement.url' should be present for JIRA integration(Only JIRA integration is present at the moment).
#### Usage:
In plugin management:
```XML
<pluginManagement>
    <plugins>
        <plugin>
            <groupId>uk.co.hermes</groupId>
            <artifactId>gitlog-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
        </plugin>
    </plugins>
</pluginManagement>
```
In project/reporting
```XML
<reporting>
    <plugins>
        <plugin>
            <groupId>uk.co.hermes</groupId>
            <artifactId>gitlog-plugin</artifactId>
            <configuration>
                 <commitExtent>365</commitExtent>
            </configuration>
        </plugin>
    </plugins>
</reporting>
```

Build project with _maven site_

Generated report avaialble at _../target/site/gitlog-embedded.html_
