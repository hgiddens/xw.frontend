# TODO

- when I run `server/test` it tries to run ScalaTest tests and complains none were found – wtf?
- favicon
- authentication
- digest digests too much – stop it eating source maps somehow?
- scalamu mutation testing(?)

## Distant future

- this is the web application and the API server – split it up!
- test for the dependencies of the client, when they exist?
- extract the sbt-digest compatibility into a separate library
- is Closure compilation idempotent i.e. do we actually get a cross-release
  benefit from content-based hashing? does this mean we want to isolate the
  client from the (mutable) parts of the buildinfo? I guess we still get
  intra-release benefit though.

## Things to remember

- when there is an API, it would like OpenAPI documentation.
