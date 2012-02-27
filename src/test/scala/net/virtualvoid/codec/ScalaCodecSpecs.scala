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

class ScalaCodecSpecs extends Specification with CodecSpecs {
  object IntStringifier extends CodecBase[Int, String] {
    def name = "Convert int to string"
    def doEncode(i: Int) = i.toString
    def doDecode(o: String) = o.toInt
  }

  "ScalaCodecs" should {
    "OnFirst" in {
      val tupleC = Codecs.OnFirst[Int, Int, String](IntStringifier)

      checkCodec(tupleC)(t => (t._1.toString, t._2))
    }
    "OnSecond" in {
      val tupleC = Codecs.OnSecond[Int, Int, String](IntStringifier)

      checkCodec(tupleC)(t => (t._1, t._2.toString))
    }
    "ReverseTuple" in {
      checkCodec(Codecs.ReverseTuple[Int, Float]()) {
        case (a, b) => (b, a)
      }
    }
    "join" in {
      type TA = (Array[Int], Array[Int])

      import Arbitrary._
      val arbitraryTupleWith12ElementsInFirst =
        Arbitrary.arbTuple2(Arbitrary(Gen.containerOfN[Array, Int](12, arbitrary[Int])), implicitly[Arbitrary[Array[Int]]])

      val codec = Codecs.Join[Int](12)

      checkCodecWithChecker(codec)(Some{
        case (a: Array[Int], b: Array[Int]) => a ++ b
      }, (t: TA) => (t._1.toSeq, t._2.toSeq))(arbitraryTupleWith12ElementsInFirst)
    }
  }
}
