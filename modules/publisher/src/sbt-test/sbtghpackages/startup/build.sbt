import sbtghpackages.{GitHubRepository, TokenSource}

name := "startup-test"

githubPublishToRepository := GitHubRepository("owner", "repo", TokenSource.Property("STARTUP_PROPERTY_TOKEN"))
