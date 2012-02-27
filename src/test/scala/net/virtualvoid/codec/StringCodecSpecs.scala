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
import org.scalacheck.{Gen, Arbitrary}

class StringCodecSpecs extends Specification with CodecSpecs {
  "StringCodecs" should {
    "ApplyCharset" in {
      val codec = Codecs.ApplyCharset("utf8")
      checkCodec(codec)(_.getBytes("utf8"))

    }
    "ToHexString" in {
      val codec = Codecs.ToHexString

      checkCodec(codec)(x => x.map(_.formatted("%02x")).mkString)
    }

    "ApplyCharset7Bit" in {
      val codec = Codecs.ApplyCharset7bit
      
      val arbitrary7bitString: Arbitrary[String] =
        Arbitrary(Gen.containerOf[Array,Byte](Gen.chooseNum(0, 127)).map(new String(_)))

      checkCodec(codec)(x => x.getBytes("ASCII"))(arbitrary7bitString)
    }
    "ApplyBase64" in {
      val codec = Codecs.ApplyBase64

      checkCodec(codec)(org.apache.commons.codec.binary.Base64.encodeBase64)
    }
  }
}
