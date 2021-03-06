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
 * A collection of codecs concerning Scala data structures.
 */
trait ScalaCodecs {

  /**
   * Apply a codec only on the first element of a `Tuple2`.
   *
   * @param codec The codec to apply to the first element of the tuple.
   */
  case class OnFirst[I1, I2, O1](codec: Codec[I1, O1]) extends Codec[(I1, I2), (O1, I2)] {
    def name = "On first item of tuple do '%s'" format codec.name
    def encode(tuple: (I1, I2)) =
      for (o1 <- codec.encode(tuple._1).right)
        yield (o1, tuple._2)

    def decode(tuple: (O1, I2)) =
      for (i1 <- codec.decode(tuple._1).right)
        yield (i1, tuple._2)
  }

  /**
   * Apply a codec only on the second element of a `Tuple2`.
   *
   * @param codec The codec to apply to the second element of the tuple.
   */
  case class OnSecond[I1, I2, O2](codec: Codec[I2, O2]) extends Codec[(I1, I2), (I1, O2)] {
    def name = "On second item of tuple do '%s'" format codec.name
    def encode(tuple: (I1, I2)) =
      for (o2 <- codec.encode(tuple._2).right)
        yield (tuple._1, o2)
    def decode(tuple: (I1, O2)) =
      for (i2 <- codec.decode(tuple._2).right)
        yield (tuple._1, i2)
  }

  /**
   * A codec which swaps the elements of a `Tuple2`.
   */
  case class ReverseTuple[T1, T2]() extends ReversibleCodecBase[(T1, T2), (T2, T1)] {
    def name = "Reverse tuple"

    def doEncode(i: (T1, T2)) =
      (i._2, i._1)

    def doDecode(o: (T2, T1)) =
      (o._2, o._1)
  }

  /**
   * A codec which joins two arrays of elements of the same type by concatenation. To be able
   * to decode the resulting concatenated array the first element must have a constant size in
   * all cases.
   * @param firstBlockSize The size of the first data array
   */
  case class Join[T: ClassManifest](firstBlockSize: Int) extends ReversibleCodecBase[(Array[T], Array[T]), Array[T]] {
    def name = "Join two arrays"
    def doEncode(tuple: (Array[T], Array[T])) = {
      val (ar1, ar2) = tuple
      assert(ar1.size == firstBlockSize, "The size of the first array must be %d but is %s" format (firstBlockSize, ar1.size))
      ar1 ++ ar2
    }
    def doDecode(block: Array[T]) =
      (block.take(firstBlockSize), block.drop(firstBlockSize))
  }
}
