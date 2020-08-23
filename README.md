# playn
Various [PlayN](http://playn.io) demos that were written for a section of CSCI 490 taught at Drury University in the Spring of 2016.
PlayN is essentially [LWJGL](https://www.lwjgl.org/).

Click on a folder above to see a short description of that folder's demo.

If you enjoy the demos in this repo, you might also like a [version of Asteroids](https://github.com/sj-simmons/asteroids)
that Simmons wrote (in Python) for a different class.

#### Running a demo

To run, say, the vertlet demo at the command-line on Debian-like systems you can simply:

```console
user@host:~$ sudo apt install maven
user@host:~$ git clone https://github.com/sj-simmons/playn
user@host:~$ cd playn/vertletDemo
user@host:~$ mvn test -Pjava
```

after which you may want to:

```console
user@host:~$ mvn clean
```

#### Forking and extending a demo

To get started with [PlayN](http://playn.io), see [Setup](http://playn.io/docs/setup.html) which covers building for
Java, Android, iOS, and HTML5 backends.

See [CygwinMavenSetUp.pdf](CygwinMavenSetUp.pdf) to set up a PlayN development environment using the Maven build system,
by hand, on Windows in Cygwin.

**Note (2020)**: Instead of using Cygwin for this (if you are running Windows), you
might wish to develop (by hand) in the [Window Subsystem for Linux](https://docs.microsoft.com/en-us/windows/wsl/).
Or, obviously, you could just use an IDE (on Windows, with Java installed in Windows).

For the vertlet demo, for instance, **main** is found here:
[vertletDemo/java/src/main/java/vertletDemo/java/VertletDemoJava.java](https://github.com/sj-simmons/playn/blob/master/vertletDemo/java/src/main/java/vertletDemo/java/VertletDemoJava.java);  
and the source code is here:
[vertletDemo/core/src/main/java/vertletDemo/core/](https://github.com/sj-simmons/playn/tree/master/vertletDemo/core/src/main/java/vertletDemo/core).
