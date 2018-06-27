# XW: Frontend

[![Build Status](https://travis-ci.com/hgiddens/xw.frontend.svg?branch=master)](https://travis-ci.com/hgiddens/xw.frontend)

This is intended to be the API frontend and web app of a collaborative
sketching service that allows crosswords to be completed collaboratively.

The intended feature set is modest (that's how it always starts):

1. Crosswords can be created – from an image of a crossword being
   uploaded. The set of existing crosswords should allow the usual CRUD
   operations.
2. Users can sketch on the crossword – the dream obviously being that I can do
   this with my Apple Pencil on my iPad.
3. Sketches are synced in real time across all clients.

Obviously the goal is to massively over-engineer everything because otherwise
it's not fun. Particular choices that I'm thinking of:

* Proper CQRS.
* Event sourcing.
* I'm pretty excited at using CRDT for the events.
* As much streaming as possible.
* ScalaJS front end using React.
* [AUI](https://docs.atlassian.com/aui/) interface.

The other thing is it'd be nice to have a reasonable playground to experiment
with all the cool new FOTM stuff.

## Development

[SBT](https://www.scala-sbt.org) is used as the build tool. To quickly spin up
a development server (listening on all interfaces on 8080), use:

    $ sbt
    > server/reStart

Running `server/reStart` again will rebuild everything necessary and restart
the server.

### Testing

This is done via SBT also; run tests via:

    $ sbt
    > test

## Deployment

This doesn't exist yet, as such, but there is a Dockerfile. One builds an
image via:

    $ docker image build -t xw.frontend:latest .

Notably the JVM in the container is GraalVM; I'm not using an fancy native
stuff at the moment though.

## Project structure

The project has a few modules.

### Common

This module contains code shared between the client and server, and as such is
compiled for both the JS and JVM backends; note the directory structure.

This is also where the custom predef we're using is – all the normal default
imports are disabled. This is done by shoving the stuff we want in the
`xw.frontend` package object (TLS Scala isn't supported with ScalaJS so we
can't use that here). So the definitions are visible without imports, the
convention is to use a multi-stage package declaration:

    package xw.frontend
    package whatever.else

### Static resources

This contains the static resources for the web application – the HTML
templates (which use [Twirl](https://github.com/playframework/twirl)) and, in
the future, probably the CSS too. In theory, this would be part of the server
module, but Twirl interacts poorly with some of the compiler flags we'd like
to use and as a consequence we sequester them off here.

### Client

This is the client code for the web application. It's written in
[ScalaJS](https://www.scala-js.org) and uses
[scalajs-react](https://github.com/japgolly/scalajs-react) because it's cool
and fun to not be able to find examples online. I very much imagine that
this'll end up using [Diode](https://github.com/suzaku-io/diode), which I
believe scratches many of the same itches as Redux.

### Server

This serves _both_ the API for the client and the client itself because at the
moment it would be massive drag otherwise. It's implemented with Akka HTTP not
because I think that's necessarily a good idea, but rather because I'd like to
explore it better because I am fucking confident I'm not using it to its
fullest.
