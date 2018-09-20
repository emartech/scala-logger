## Creating a release

This library is using [sbt-release-early] for releasing artifacts. Every push will be released to maven central, see the plugins documentation on the versioning schema.

### To cut a final release:

Choose the appropriate version number according to [semver] then create and push a tag with it, prefixed with `v`.
For example:

```
$ git tag -s v0.3.1
$ git push --tag
```

After pushing the tag, while it is not strictly necessary, please [draft a release on github] with this tag too.


[sbt-release-early]: https://github.com/scalacenter/sbt-release-early
[semver]: https://semver.org
[draft a release on github]: https://github.com/emartech/scala-logger/releases/new
