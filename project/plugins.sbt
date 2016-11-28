logLevel := Level.Warn

resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("com.sksamuel.avro4s" % "sbt-avro4s" % "1.0.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")

addSbtPlugin("com.eed3si9n"      % "sbt-assembly"    % "0.14.3")

addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "1.6.0")

//addSbtPlugin("org.wartremover" % "sbt-wartremover" % "1.2.1") // Code linting