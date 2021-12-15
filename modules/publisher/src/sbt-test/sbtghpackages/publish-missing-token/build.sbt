import sbtghpackages.{GitHubRepository, TokenSource}

name := "publish-missing-token-test"

githubPublishToRepository := GitHubRepository("owner", "repo", TokenSource.Property("UNKNOWN_PROPERTY"))
