Scala Codecs
============

Scala codecs is a library to specify an encoding and the corresponding decoding step together
as a codec primitive. Codec primitives can then be combined into a pipeline in a type-safe way.

Documentation
-------------

When working with encoding and decoding it is desirable to specify the encoding and the decoding
step together. This library is a small framework which allows the definition of packages of
Encoder/Decoder into a ``Codec``. Several encoding-decoding steps can be combined using the
``<~>``-operator.

Scala codecs come with a set of predefined codecs for common tasks like working with strings,
Scala data structures, or encryption.

See the ScalaDocs_ for more information.

Example
-------

Let's assume we want to create a signed cookie containing a string of data which then
is base64 encoded. You could use this pipeline:

::

  import javax.crypto.spec.SecretKeySpec
  import net.virtualvoid.codec.Codecs._

  val cookiePipeline =
     ApplyCharset("utf8") <~>
     Sign("HmacSHA1", new SecretKeySpec("thepa55".getBytes, "HmacSHA1")) <~>
     Join(20) <~>
     ApplyBase64 <~>
     ApplyCharset7Bit.reversed

Let's see what the steps are:
  1. the input string is utf8 encoded into an array of bytes
  2. now a signature is calculated using an hmac algorithm, this results in a tuple
     `(signature, message)`
  3. signature and message are joined with the signature expected to have a constant length of 20 bytes
  4. the byte array is base64 encoded
  5. the resulting byte array is reinterpreted as a 7 bit ASCII string (``ApplyCharset7Bit`` is working
     in the wrong direction by default so we have to reverse it)

We can now use the pipeline like this:

::

  val code = cookiePipeline.encode("testdata").right.get // assuming there are no errors
  assert(code == "p8JfiVsTRYtkMOb6yfY3ekatMKN0ZXN0ZGF0YQ==")

to create a signed version of the original data which could be used as a cookie
value. To recover the original message we can now use this code:

::

  val original = cookiePipeline.decode("p8JfiVsTRYtkMOb6yfY3ekatMKN0ZXN0ZGF0YQ==")
  println(original.right.toOption.getOrElse("Decoding/Validation failed"))

Usage
-----

Use

::

  libraryDependency += "net.virtual-void" %% "codecs" % "0.4.0"

in your sbt build definition.

Questions / Feedback / Issues
-----------------------------

Please send me a mail_ or create an issue in the github issue tracker_.

TODO / Ideas
------------

 - case class extraction/creation
 - provide an iteratee API for streaming codes
 - more diagnostic tools

License
-------

Licensed under the 2-clause BSD license.

.. _ScalaDocs: http://jrudolph.github.com/scala-codecs/api/index.html
.. _mail: mailto:johannes.rudolph@gmail.com
.. _tracker: https://github.com/jrudolph/codec/issues