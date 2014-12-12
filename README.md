## Developing in Eclipse

You can use the [Maven Eclipse Plugin](https://maven.apache.org/plugins/maven-eclipse-plugin/) 
to set up the various subprojects.  Assuming 
`WORKSPACE` is your workspace, then you need to configure your workspace 
one time with:

```sh
mvn -Declipse.workspace="$WORKSPACE" eclipse:configure-workspace
```

Then for each module, use `eclipse:eclipse` and then import it as an 
existing project in Eclipse, e.g.:

```sh
cd sqltutor-common
mvn -Declipse.workspace="$WORKSPACE" eclipse:eclipse
```

For the `sqltutor-web` project, you should also include the flag
`-Dwtpversion=2.0`.

## Setting up the Database

We use [PostgreSQL](http://www.postgresql.org) as our database backend.
The web project assumes the following users exist, so create them first:

Role|Type|Members
----|----|-------
DB_Manager | login | &nbsp;
readonly_user | login | &nbsp;
readonly | group | readonly_user

Next, create the following database and restore them:

Database|Backup File
--------|-----------
sqltutor|sqltutor-web/database/sqltutor.backup
sqltutorschemas|sqltutor-web/database/sqltutorschemas.backup

## Configuring Tomcat

The `sqltutor-web` module builds a WAR file you can deploy to 
some Java servlet container.  We use and test with Tomcat 7.

You need to configure three data sources used by the application.  
One way is by placing the following in Tomcat's `context.xml` file:

```xml
  <Resource name="jdbc/sqltutorDB" auth="Container"
        type="javax.sql.DataSource"
        maxActive="100" maxIdle="30" maxWait="10000"
        username="DB_Manager"
        password="$DB_MANAGER_PASSWORD"
        driverClassName="org.postgresql.Driver"
        removeAbandoned="true"
        logAbandoned="true"
        url="jdbc:postgresql://localhost/sqltutor"/>

   <Resource name="jdbc/sqltutorUserDB" auth="Container"
        type="javax.sql.DataSource"
        maxActive="100" maxIdle="30" maxWait="10000"
        username="DB_Manager"
        password="$DB_MANAGER_PASSWORD"
        removeAbandoned="true"
        logAbandoned="true"
        driverClassName="org.postgresql.Driver"
        url="jdbc:postgresql://localhost/sqltutorschemas"/>
        
   <Resource name="jdbc/sqltutorUserDBRead" auth="Container"
        type="javax.sql.DataSource"
        maxActive="100" maxIdle="30" maxWait="10000"
        username="readonly_user"
        password="$READONLY_USER_PASSWORD"
        removeAbandoned="true"
        logAbandoned="true"
        driverClassName="org.postgresql.Driver"
        url="jdbc:postgresql://localhost/sqltutorschemas"/>
```

Substitute `$DB_MANAGER_PASSWORD` and `$READONLY_USER_PASSWORD` with the passwords 
you chose.
 
