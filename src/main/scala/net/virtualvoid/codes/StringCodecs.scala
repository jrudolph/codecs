/* Copyright (c) 2012, Johannes Rudolph
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the NA nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.virtualvoid.codes

trait StringCodecs {
  case class Charset(charset: String) extends CodecBase[String, Bytes] {
    def name = "Encode with '%s'" format charset
    def doEncode(string: String) = string.getBytes(charset)
    def doDecode(bytes: Bytes) = new String(bytes, charset)
  }

  case object Charset7bit extends CodecBase[String, Bytes] {
    def name = "Reinterpret 7-Bit String to byte array"
    def doEncode(string: String) = {
      string.map { char =>
          assert((char & 0x7f) == char, "Not a 7-bit character: "+char)
          char.toByte
        }.toArray
      }
    def doDecode(bytes: Bytes) = new String(bytes.map(_.toChar))
  }

  case object HexString extends CodecBase[Bytes, String] {
    def name = "Convert bytes to hex string"
    def doEncode(bytes: Bytes) =
      bytes.map(_.formatted("%02x")).mkString
    def doDecode(string: String) =
      string.grouped(2).map(str => Integer.parseInt(str, 16).toByte).toArray
  }
}
