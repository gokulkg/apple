//resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

resolvers += "Sonatype OSS Snapshots" at "https://repository.sonatype.org/content/groups/forge/"

resolvers += "twitter-repo" at "http://maven.twttr.com"

resolvers += "websudos-repo" at "http://maven.websudos.co.uk/ext-release-local"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "twitter-repo" at "http://maven.twttr.com"

resolvers += "mvn-repo" at "http://mvnrepository.com/artifact"

resolvers += "central" at "http://repo1.maven.org/maven2/"

initialCommands := "import com.myweb.apple._"

net.virtualvoid.sbt.graph.Plugin.graphSettings
