organization := "net.virtual-void"

name := "codecs"

version := "0.4.0"

homepage := Some(url("http://github.com/jrudolph/codecs"))

licenses in GlobalScope += "BSD 2-Clause License" -> url("http://www.opensource.org/licenses/BSD-2-Clause")

description := "Codecs is a library of bidirectional encoding/decoding primitives which can easily be composed into a processing pipeline"

seq(lsSettings: _*)

(LsKeys.tags in LsKeys.lsync) := Seq("codec", "encode", "decode", "encrypt", "sign")

(LsKeys.docsUrl in LsKeys.lsync) <<= homepage
