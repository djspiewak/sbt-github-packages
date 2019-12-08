# sbt-github-packages [![Build Status](https://travis-ci.com/djspiewak/sbt-github-packages.svg?branch=master)](https://travis-ci.com/djspiewak/sbt-github-packages)

Configures your project for publication to the [GitHub Package Registry](https://help.github.com/en/articles/about-github-package-registry) using its Apache Maven support. Note that you probably shouldn't use this with plugins, only libraries. Probably won't delete your source code, but no promises. Also provides some convenience functionality for *depending* upon artifacts which have been published to the Package Registry. **Has not yet been tested with private repos.**

## Usage

Add the following to your **project/plugins.sbt** file:

```sbt
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "<version>")
```

Published for sbt 1.x. No dependencies.

### Resolvers

If you're consuming packages that were published in the GitHub Package Registry, this plugin defines some convenience syntax for adding resolvers:

```sbt
resolvers += Resolver.githubPackagesRepo("OWNER", "REPOSITORY")
```

This works for both public and private repositories, though if you use it with a private repository, you will also need to ensure that either `credentials` or `githubTokenSource` are appropriately configured.

### Keys

The following setting keys are defined:

- `githubOwner : String` (*required*) Defines the organization or user name that owns the package registry to which this project will be published
- `githubRepository : String` (*required*) The repository which hosts this project under the organization/user defined in the other setting
- `githubUser : String` (*defaults to `git config github.user`*) *Your* GitHub username. This should almost never be specified in the build itself, but rather read from some external source. By default, it will read from the `git config` (by shelling out to the `git` command), but it's easy to override this to use an environment variable (e.g. `githubUser := sys.env("GITHUB_USER")`). To be extremely clear, this is the user who ran the `sbt` command, it is not *necessarily* the repository owner!
- `githubTokenSource : Option[TokenSource]` (*defaults to `None`*) Where the plugin should go to read the GitHub API token to use in authentication. `TokenSource` has two possible values: `Environment(variable: String)` and `GitConfig(key: String)`. This is mostly just a convenience. You're free to do whatever you want. Just don't, like, put it in your build. 

`homepage` and `scmInfo` will be configured for you based on the above.

Note that the token must have `read:packages` access if you want to *depend on* packages from private repositories, and must have `write:packages` if you wish to *publish* packages. At present there is no support for splitting these two tokens (mostly due to limitations in GitHub's API). If you define `githubTokenSource := None` (the default) and you don't want to use an environment variable or `git config` value to configure your token, you will need to set up the `credentials` in something roughly approximating the following:

```sbt
credentials += 
  Credentials(
    "GitHub Package Registry",
    "maven.pkg.github.com",
    githubUser.value,
    token)
```

For the love of all that is holy, do not put this into your **build.sbt**. The `token` is a password. Treat it as such. Best practice is generally to use an encrypted environment variable in your CI and a necessarily unencrypted environment variable locally. For example:

```sbt
githubTokenSource := Some(TokenSource.Environment("GITHUB_TOKEN"))
```

Then, if using Travis, you should be able to do something like:

```bash
# in your .profile file
$ export GITHUB_TOKEN="abcdef12345cafebabe"   # <-- your token here

# ...and when setting up your project
$ travis encrypt GITHUB_TOKEN=$GITHUB_TOKEN
```

Follow the instructions on how to add this to your `.travis.yml`.
