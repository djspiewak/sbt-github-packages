import sbtghpackages.{GitHubRepository, TokenSource}

name := "resolve-missing-token-test"

githubRepositories += GitHubRepository("owner", "repo", TokenSource.Property("UNKNOWN_PROPERTY"))
