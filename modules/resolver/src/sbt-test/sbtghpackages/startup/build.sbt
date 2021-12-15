import sbtghpackages.{GitHubRepository, TokenSource}

name := "startup-test"

githubRepositories += GitHubRepository("owner", "repo", TokenSource.Property("STARTUP_PROPERTY_TOKEN"))
