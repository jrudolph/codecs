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

import org.specs2.ScalaCheck
import org.scalacheck.{Prop, Arbitrary}
import org.specs2.mutable.Specification

trait CodecSpecs extends ScalaCheck { self: Specification =>
  def checkCodec[I: Arbitrary, O](codec: Codec[I, O])(checker: I => O): Prop =
    checkCodecWithChecker(codec)(Some(checker))

  def checkCodecWithChecker[I: Arbitrary, O, I2](codec: Codec[I, O])(maybeChecker: Option[I => O] = None, iChecker: I => I2 = (i: I) => identity[I](i)): Prop =
    check { (input: I) =>
      val code = codec.encode(input).right.get
      maybeChecker.foreach { checker =>
        code must be_==(checker(input))
      }
      iChecker(codec.decode(code).right.get) must be_==(iChecker(input))
    }
}
