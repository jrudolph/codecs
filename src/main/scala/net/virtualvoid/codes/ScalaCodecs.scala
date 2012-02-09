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

trait ScalaCodecs {

  case class OnFirst[I1, I2, O1](inner: Codec[I1, O1]) extends CodecBase[(I1, I2), (O1, I2)] {
    def name = inner.name+" on first item of tuple"
    def doEncode(tuple: (I1, I2)) =
      (inner.encode(tuple._1).right.get, tuple._2)
    def doDecode(tuple: (O1, I2)) =
      (inner.decode(tuple._1).right.get, tuple._2)
  }
  case class OnSecond[I1, I2, O2](inner: Codec[I2, O2]) extends CodecBase[(I1, I2), (I1, O2)] {
    def name = inner.name+" on first item of tuple"
    def doEncode(tuple: (I1, I2)) =
      (tuple._1, inner.encode(tuple._2).right.get)
    def doDecode(tuple: (I1, O2)) =
      (tuple._1, inner.decode(tuple._2).right.get)
  }
  case class ReverseTuple[T1, T2]() extends CodecBase[(T1, T2), (T2, T1)] {
    def name = "Reverse tuple"

    def doEncode(i: (T1, T2)) =
      (i._2, i._1)

    def doDecode(o: (T2, T1)) =
      (o._2, o._1)
  }

  case class Join[T: ClassManifest](firstBlockSize: Int) extends CodecBase[(Array[T], Array[T]), Array[T]] {
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
