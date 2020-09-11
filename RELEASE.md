## Creating a release

This library is using [sbt-ci-release] for releasing artifacts. Every push to master will end up as a snapshot release, see the plugins documentation on the versioning schema.

### To cut a final release:

Choose the appropriate version number according to [semver] then create and push a tag with it, prefixed with `v`.
For example:

```
$ git tag -s v0.3.1
$ git push --tag
```

After pushing the tag, while it is not strictly necessary, please [draft a release on github] with this tag too.


[sbt-ci-release]: https://github.com/olafurpg/sbt-ci-release
[semver]: https://semver.org
[draft a release on github]: https://github.com/emartech/scala-logger/releases/new
