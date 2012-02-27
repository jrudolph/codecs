/*
 * Copyright (c) 2012, Johannes Rudolph
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.virtualvoid.codec

/**
 * A collection of codecs concerning Strings.
 */
trait StringCodecs {

  /**
   * Encodes a string with a given charset.
   * @param charset
   */
  case class ApplyCharset(charset: String) extends ReversibleCodecBase[String, Bytes] {
    def name = "Encode with '%s'" format charset
    def doEncode(string: String) = string.getBytes(charset)
    def doDecode(bytes: Bytes) = new String(bytes, charset)
  }

  /**
   * Reinterprets a string of characters only in the 7-bit range [0, 127] into
   * an array of bytes.
   */
  case object ApplyCharset7Bit extends ReversibleCodecBase[String, Bytes] {
    def name = "Reinterpret 7-Bit String to byte array"
    def doEncode(string: String) = {
      string.map { char =>
          assert((char & 0x7f) == char, "Not a 7-bit character: "+char)
          char.toByte
        }.toArray
      }
    def doDecode(bytes: Bytes) = new String(bytes.map(_.toChar))
  }

  /**
   * Encodes an array of bytes into a string of double length where every two
   * characters of the result are the lower-case hexadecimal representation
   * of one byte of the array.
   */
  case object ToHexString extends ReversibleCodecBase[Bytes, String] {
    def name = "Convert bytes to hex string"
    def doEncode(bytes: Bytes) =
      bytes.map(_.formatted("%02x")).mkString
    def doDecode(string: String) =
      string.grouped(2).map(str => Integer.parseInt(str, 16).toByte).toArray
  }

  /**
   * Apply a base64 encoding on the input data. The result is an array of
   * bytes of characters in the 7-bit range. If you need a String output you
   * can combine this codec with `ApplyCharset7Bit` like this:
   *
   * {{{
   *     val ToBase64String = ApplyBase64 <~> ApplyCharset7Bit
   * }}}
   *
   * This relies on apache commons-codec, so you have to include that into
   * your build. In sbt you can use this line in your build definition to make it
   * available:
   *
   * {{{
   *   libraryDependencies += "commons-codec" % "commons-codec" % "1.5+"
   * }}}
   */
  case object ApplyBase64 extends ReversibleCodecBase[Bytes, Bytes] {
    def name = "Base64"

    def doEncode(bytes: Bytes) =
      org.apache.commons.codec.binary.Base64.encodeBase64(bytes)

    def doDecode(bytes: Bytes) =
      org.apache.commons.codec.binary.Base64.decodeBase64(bytes)
  }
}
