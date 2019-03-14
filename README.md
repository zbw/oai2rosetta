# Dspace Subapp
## Prerequisites
* Java 1.8, for building you need the JDK
* a mysql DB for holding the data
## Installing
Building and installing is possible with sbt. There is a ./sbt launcher shipped with this application. sbt will download all dependencies.

* `./sbt run` will build and run the application. Be sure to have a database.
* `./sbt dist` will build a zip package in target/universal
* `./sbt universal:packageZipTarball` will build a gz package in target/universal
## Configuring
Configuring is done in the `conf/application.conf` file. Especially interesting is:
* `db.default.url`
* `importdirectory`
## Initial Run
If the database schema is not installed yet set `play.evolutions.enabled=true´ and follow the intstruction in the browser.

in `conf/initial-user.yml` is an initial user that is inserted in an empty db.
## Deployment
`./sbt universal:packageZipTarball` will create a tgz. Copy it into your target environment and unpack it. Execute bin/dspacesubapp and the application will run.

You can add a special config file into config. It will overwrite settings made in `application.conf`. To use it execute `bin/dspacesubapp -Dconfig.resource=prod.conf`

There is an example for system.d file in `dist/dspacesubapp.service`

    - copy dspacesubapp.service in /etc/systemd/system/
    - systemctl daemon-reload
    - systemctl start dspacesubapp
    - systemctl status dspacesubapp
    - systemctl stop dspacesubapp
    - systemctl enable dspacesubapp

## More Information
this application is built on [play 2.5](https://www.playframework.com/documentation/2.5.x/Home)