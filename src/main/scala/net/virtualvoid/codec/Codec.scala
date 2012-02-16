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

object Codec {
  def checkPipeline[A, B, C](pipeline: ConcatenatedCodec[A, B, C])(start: A) {
    pipeline.first match {
      case x: ConcatenatedCodec[_, _, _] => checkPipeline(x)(start)
      case _ =>
    }
    val startValue = pipeline.first.encode(start).right.get
    val endValue = pipeline.next.encode(startValue).right.get
    val checkValue = pipeline.next.decode(endValue).right.get

    def isEqual(i1: Any, i2: Any): Boolean = (i1, i2) match {
      case (a: Array[Byte], b: Array[Byte]) => a.toSeq == b.toSeq
      case ((a1, a2), (b1, b2)) => isEqual(a1, b1) && isEqual(a2, b2)
      case _ => i1 == i2
    }

    if (isEqual(startValue, checkValue))
      println("Stage '%s' working correctly" format  pipeline.next.name)
    else
      println("Stage '%s' of pipeline broken: '%s' != '%s'" format (pipeline.next.name, startValue, checkValue))
  }

  def noisyEncode[I, O](codec: Codec[I, O])(start: I): (O, Seq[(Codec[_, _], String)]) = codec match {
    case ConcatenatedCodec(first, second) =>
      val (end1, msgs1) = noisyEncode(first)(start)
      val (end2, msgs2) = noisyEncode(second)(end1)

      (end2, msgs1 ++ msgs2)
    case ReversedCodec(inner) =>
      noisyDecode(inner)(start)
    case _ =>
      val value = codec.encode(start).right.get
      (value, Seq((codec, print(value))))
  }
  // TODO: this shares almost all code with noisyEncode, but because of
  //       ReversedCodec we've to supply it right now. Check how to unify
  def noisyDecode[I, O](codec: Codec[I, O])(start: O): (I, Seq[(Codec[_, _], String)]) = codec match {
    case ConcatenatedCodec(first, second) =>
      val (end2, msgs2) = noisyDecode(second)(start)
      val (end1, msgs1) = noisyDecode(first)(end2)

      (end1, msgs2 ++ msgs1)
    case ReversedCodec(inner) =>
      noisyEncode(inner)(start)
    case _ =>
      val value = codec.decode(start).right.get
      (value, Seq((codec, print(value))))
  }

  def print(value: Any): String = value match {
    case a: Bytes => "%d Bytes: [%s]" format (a.size, a.map(_ formatted "%02X").mkString(" "))
    case a: Array[_] => a.toSeq.toString
    case (a, b) => (print(a), print(b)).toString
    case s: String => "\"%s\"" format s
    case _ => value.toString
  }
}
