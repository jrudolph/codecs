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

import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import javax.crypto.{Mac, Cipher}

trait CryptCodecs {
  case class EncryptCBC(cipherName: String, key: SecretKeySpec) extends CodecBase[Bytes, (Bytes, Bytes)] {
    def name = "Encrypt with "+cipherName

    def doEncode(message: Bytes) = {
      val cipher = Cipher.getInstance(cipherName)
      cipher.init(Cipher.ENCRYPT_MODE,
                  key)

      val code = cipher.doFinal(message)
      val iv = cipher.getIV
      (iv, code)
    }
    def doDecode(code: (Bytes, Bytes)) = {
      val (iv, msg) = code
      val cipher = Cipher.getInstance(cipherName)
      cipher.init(Cipher.DECRYPT_MODE,
                  key,
                  new IvParameterSpec(iv))
      cipher.doFinal(msg)
    }
  }

  case class Sign(macName: String, key: SecretKeySpec) extends CodecBase[Bytes, (Bytes, Bytes)] {
    def name = "Sign with "+macName
    def doEncode(msg: Bytes) = {
      val mac = Mac.getInstance(macName)
      mac.init(key)

      val signature = mac.doFinal(msg)
      (signature, msg)
    }
    def doDecode(info: (Bytes, Bytes)) = {
      val (signature, msg) = info

      val mac = Mac.getInstance(macName)
      mac.init(key)
      val check = mac.doFinal(msg)
      if (!constantTimeIsEqual(check, signature))
        throw new RuntimeException("Validation failed. Signature '%s' wasn't equal to '%s'" format (check.toSeq, signature.toSeq))

      msg
    }
  }

  // non-short-circuiting isEqual for byte arrays to avoid timing attacks
  // see e.g. http://codahale.com/a-lesson-in-timing-attacks/
  private def constantTimeIsEqual(a : Bytes, b : Bytes) : Boolean = {
    assert(a.length == b.length && a.length > 0)

    (a, b).zipped.map(_ == _).reduceLeftOption(_ && _).getOrElse(true)
  }
}
