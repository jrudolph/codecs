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

import org.specs2.mutable.Specification
import javax.crypto.spec.SecretKeySpec

class CryptCodecSpecs extends Specification with CodecSpecs {
  "CryptCodecs" should {
    "EncryptCBC" in {
      val codec = Codecs.EncryptCBC("AES/CBC/PKCS5Padding", new SecretKeySpec("thepa55w0rd12345".getBytes, "AES"))

      checkCodecWithChecker(codec)(None)
    }
    "Sign" in {
      val codec = Codecs.Sign("HmacSHA1", new SecretKeySpec("hmackey".getBytes, "HmacSHA1"))

      "signature correct" in {
        checkCodecWithChecker(codec)(None)
      }
      "signature broken" in {
        object TransmissionError extends CodecBase[(Array[Byte], Array[Byte]), (Array[Byte], Array[Byte])] {
          def name = "Produce a transmission error"
          def doEncode(i: (Array[Byte], Array[Byte])) = {
            val x = i._2.toSeq.toArray
            x(5) = 42
            (i._1, x)
          }
          def doDecode(o: (Array[Byte], Array[Byte])) = o
        }
        val codec2 = codec <~> TransmissionError
        
        check { (input: (Array[Byte])) =>
          (input.size > 5) ==> {
            val code = codec2.encode(input).right.get

            val decoded = codec2.decode(code)
            decoded.left.toOption.exists(_.getMessage.startsWith("Validation failed.")) must beTrue
          }
        }
      }
    }
  }
}
