lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "ua.mackenzy",
      scalaVersion    := "2.12.8"
    )),
    name := "milichov",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"   %% "akka-http"            % "10.1.7",
      "com.typesafe.akka"   %% "akka-http-spray-json" % "10.1.7",
      "com.typesafe.akka"   %% "akka-http-xml"        % "10.1.7",
      "com.typesafe.akka"   %% "akka-stream"          % "2.5.21",
      "com.google.inject"   %  "guice"                % "4.2.2",
      "com.typesafe.slick"  %% "slick"                % "3.3.0",
      "com.typesafe.slick"  %% "slick-hikaricp"       % "3.3.0",
      "org.xerial"          %  "sqlite-jdbc"          % "3.8.11.2",
      "de.heikoseeberger"   %% "akka-http-circe"      % "1.25.2",
      "io.circe"            %% "circe-core"           % "0.11.1",
      "io.circe"            %% "circe-generic"        % "0.11.1",
      "ch.qos.logback"      %  "logback-classic"      % "1.1.3",

      "com.typesafe.akka"   %% "akka-http-testkit"    % "10.1.7"      % Test,
      "com.typesafe.akka"   %% "akka-testkit"         % "2.5.21"      % Test,
      "com.typesafe.akka"   %% "akka-stream-testkit"  % "2.5.21"      % Test,
      "org.scalatest"       %% "scalatest"            % "3.0.5"       % Test,
      "com.h2database"      %  "h2"                   % "1.3.148"     % Test
    )
  )