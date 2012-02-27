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
 * Represents a decoding function. In contrast to an encoding function a decoding
 * function must be referentially transparent to establish the codec invariant that
 * a round-trip through a codec always reproduces the original input value.
 *
 * @tparam I
 * @tparam O
 */
trait Decoder[+I, -O] { self =>
  def decode(o: O): OrError[I]

  def <~[O2](next: Decoder[O, O2]): Decoder[I, O2] =
    new Decoder[I, O2] {
      val func = chain(next, self)

      def decode(o2: O2) = func(o2)
    }
}

object Decoder {
  implicit def decoderIsF1[I, O](decoder: Decoder[I, O]): O => I =
    o => decoder.decode(o).right.get

  implicit def decoderIsF1WithError[I, O](decoder: Decoder[I, O]): O => OrError[I] =
    o => decoder.decode(o)
}