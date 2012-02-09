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
package net.virtualvoid.codes

trait Codec[I, O] extends Encoder[I, O] with Decoder[I, O] {
  def name: String

  def <~>[O2](next: Codec[O, O2]): Codec[I, O2] =
    ConcatenatedCodec(this, next)
  
  def reversed: Codec[O, I] =
    ReversedCodec(this)
}

case class ConcatenatedCodec[I, O1, O2](first: Codec[I, O1], next: Codec[O1, O2]) extends Codec[I, O2] {
  val encodeFunc = chain[I, O1, O2](first, next)
  val decodeFunc = chain[O2, O1, I](next, first)
  
  def name = first.name+" <~> "+next.name
  def encode(i: I): OrError[O2] =
    encodeFunc(i)
  def decode(o2: O2): OrError[I] =
    decodeFunc(o2)
}

case class ReversedCodec[I, O](codec: Codec[I, O]) extends Codec[O, I] {
  def name = "Reversed "+codec.name
  def encode(i: O) = codec.decode(i)
  def decode(o: I) = codec.encode(o)
}
