/*
* Copyright 2001-2013 Artima, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.scalatest

import org.scalactic.{Equality, Every, One, Many, Entry}
import org.scalactic.StringNormalizations._
import SharedHelpers._
import FailureMessages.decorateToStringValue
import Matchers._

class EveryShouldContainInOrderLogicalAndSpec extends Spec {

  //ADDITIONAL//

  val invertedListOfStringEquality =
    new Equality[Every[String]] {
      def areEqual(a: Every[String], b: Any): Boolean = a != b
    }

  val upperCaseStringEquality =
    new Equality[String] {
      def areEqual(a: String, b: Any): Boolean = upperCase(a) == upperCase(b)
    }

  private def upperCase(value: Any): Any =
    value match {
      case l: Every[_] => l.map(upperCase(_))
      case s: String => s.toUpperCase
      case c: Char => c.toString.toUpperCase.charAt(0)
      case (s1: String, s2: String) => (s1.toUpperCase, s2.toUpperCase)
      case e: java.util.Map.Entry[_, _] =>
        (e.getKey, e.getValue) match {
          case (k: String, v: String) => Entry(k.toUpperCase, v.toUpperCase)
          case _ => value
        }
      case _ => value
    }

  val upperCaseListOfStringEquality =
    new Equality[Every[String]] {
      def areEqual(a: Every[String], b: Any): Boolean = upperCase(a) == upperCase(b)
    }

  val fileName: String = "EveryShouldContainInOrderLogicalAndSpec.scala"

  object `an Every` {

    val fumList: Every[String] = Every("fex", "fum", "fum", "foe", "fie", "fie", "fee")
    val toList: Every[String] = Every("too", "you", "you", "to", "birthday", "happy", "happy")

    object `when used with (contain inOrder xx and contain inOrder xx)` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        fumList should (contain inOrder ("fum", "foe", "fie", "fee") and contain inOrder ("fum", "foe", "fie", "fee"))
        val e1 = intercept[TestFailedException] {
          fumList should (contain inOrder ("fee", "fie", "foe", "fum") and contain inOrder ("fum", "foe", "fie", "fee"))
        }
        checkMessageStackDepth(e1, Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"fee\", \"fie\", \"foe\", \"fum\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (contain inOrder ("fum", "foe", "fie", "fee") and contain inOrder ("fee", "fie", "foe", "fum"))
        }
        checkMessageStackDepth(e2, Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"fum\", \"foe\", \"fie\", \"fee\"") + ", but " + Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"fee\", \"fie\", \"foe\", \"fum\""), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality
        fumList should (contain inOrder ("FUM", "FOE", "FIE", "FEE") and contain inOrder ("FUM", "FOE", "FIE", "FEE"))
        val e1 = intercept[TestFailedException] {
          fumList should (contain inOrder ("FEE", "FIE", "FOE", "FUM") and contain inOrder ("FUM", "FOE", "FIE", "FEE"))
        }
        checkMessageStackDepth(e1, Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (contain inOrder ("FUM", "FOE", "FIE", "FEE") and (contain inOrder ("FEE", "FIE", "FOE", "FUM")))
        }
        checkMessageStackDepth(e2, Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\"") + ", but " + Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\""), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (fumList should (contain inOrder ("FUM", "FOE", "FIE", "FEE") and contain inOrder ("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (contain inOrder ("FEE", "FIE", "FOE", "FUM") and contain inOrder ("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (contain inOrder ("FUM", "FOE", "FIE", "FEE") and contain inOrder ("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\"") + ", but " + Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\""), fileName, thisLineNumber - 2)
        (fumList should (contain inOrder (" FUM ", " FOE ", " FIE ", " FEE ") and contain inOrder (" FUM ", " FOE ", " FIE ", " FEE "))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          fumList should (contain inOrder ("fee", "fie", "foe", "fie", "fum") and contain inOrder ("fum", "foe", "fie", "fee"))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))

        val e2 = intercept[exceptions.NotAllowedException] {
          fumList should (contain inOrder ("fum", "foe", "fie", "fee") and contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e2.failedCodeFileName.get should be (fileName)
        e2.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e2.message should be (Some(Resources("inOrderDuplicate")))
      }
    }

    object `when used with (equal xx and contain inOrder xx)` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        fumList should (equal (fumList) and contain inOrder ("fum", "foe", "fie", "fee"))
        val e1 = intercept[TestFailedException] {
          fumList should (equal (toList) and contain inOrder ("fum", "foe", "fie", "fee"))
        }
        checkMessageStackDepth(e1, Resources("didNotEqual", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (equal (fumList) and contain inOrder ("fee", "fie", "foe", "fum"))
        }
        checkMessageStackDepth(e2, Resources("equaled", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", but " + Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"fee\", \"fie\", \"foe\", \"fum\""), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality
        fumList should (equal (fumList) and contain inOrder ("FUM", "FOE", "FIE", "FEE"))
        val e1 = intercept[TestFailedException] {
          fumList should (equal (toList) and contain inOrder ("FUM", "FOE", "FIE", "FEE"))
        }
        checkMessageStackDepth(e1, Resources("didNotEqual", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (equal (fumList) and (contain inOrder ("FEE", "FIE", "FOE", "FAM")))
        }
        checkMessageStackDepth(e2, Resources("equaled", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", but " + Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FAM\""), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (fumList should (equal (toList) and contain inOrder ("FUM", "FOE", "FIE", "FEE"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (equal (toList) and contain inOrder ("FEE", "FIE", "FOE", "FUM"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("equaled", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", but " + Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (equal (fumList) and contain inOrder ("FUM", "FOE", "FIE", "FEE"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, Resources("didNotEqual", decorateToStringValue(fumList), decorateToStringValue(fumList)), fileName, thisLineNumber - 2)
        (fumList should (equal (toList) and contain inOrder (" FUM ", " FOE ", " FIE ", " FEE "))) (decided by invertedListOfStringEquality, after being lowerCased and trimmed)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          fumList should (equal (fumList) and contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))
      }
    }

    object `when used with (be xx and contain inOrder xx)` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        fumList should (be_== (fumList) and contain inOrder ("fum", "foe", "fie", "fee"))
        val e1 = intercept[TestFailedException] {
          fumList should (be_== (toList) and contain inOrder ("fum", "foe", "fie", "fee"))
        }
        checkMessageStackDepth(e1, Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (be_== (fumList) and contain inOrder ("fee", "fie", "foe", "fum"))
        }
        checkMessageStackDepth(e2, Resources("wasEqualTo", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", but " + Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"fee\", \"fie\", \"foe\", \"fum\""), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality
        fumList should (be_== (fumList) and contain inOrder ("FUM", "FOE", "FIE", "FEE"))
        val e1 = intercept[TestFailedException] {
          fumList should (be_== (toList) and contain inOrder ("FUM", "FOE", "FIE", "FEE"))
        }
        checkMessageStackDepth(e1, Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (be_== (fumList) and (contain inOrder ("FEE", "FIE", "FOE", "FUM")))
        }
        checkMessageStackDepth(e2, Resources("wasEqualTo", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", but " + Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\""), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (fumList should (be_== (fumList) and contain inOrder ("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (be_== (fumList) and contain inOrder ("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("wasEqualTo", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", but " + Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (be_== (toList) and contain inOrder ("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
        (fumList should (be_== (fumList) and contain inOrder (" FUM ", " FOE ", " FIE ", " FEE "))) (after being lowerCased and trimmed)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          fumList should (be_== (fumList) and contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))
      }
    }

    object `when used with (contain inOrder xx and be xx)` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        fumList should (contain inOrder ("fum", "foe", "fie", "fee") and be_== (fumList))
        val e1 = intercept[TestFailedException] {
          fumList should (contain inOrder ("fum", "foe", "fie", "fee") and be_== (toList))
        }
        checkMessageStackDepth(e1, Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"fum\", \"foe\", \"fie\", \"fee\"") + ", but " + Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (contain inOrder ("fee", "fie", "foe", "fum") and be_== (fumList))
        }
        checkMessageStackDepth(e2, Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"fee\", \"fie\", \"foe\", \"fum\""), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality
        fumList should (contain inOrder ("FUM", "FOE", "FIE", "FEE") and be_== (fumList))
        val e1 = intercept[TestFailedException] {
          fumList should (contain inOrder ("FEE", "FIE", "FOE", "FUM") and be_== (toList))
        }
        checkMessageStackDepth(e1, Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (contain inOrder ("HAPPY", "BIRTHDAY", "TO", "YOU") and (be_== (fumList)))
        }
        checkMessageStackDepth(e2, Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"HAPPY\", \"BIRTHDAY\", \"TO\", \"YOU\""), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (fumList should (contain inOrder ("FUM", "FOE", "FIE", "FEE") and be_== (fumList))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (contain inOrder ("FEE", "FIE", "FOE", "FUM") and be_== (fumList))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (contain inOrder ("FUM", "FOE", "FIE", "FEE") and be_== (toList))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\"") + ", but " + Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
        (fumList should (contain inOrder (" FUM ", " FOE ", " FIE ", " FEE ") and be_== (fumList))) (after being lowerCased and trimmed)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          fumList should (contain inOrder ("fee", "fie", "foe", "fie", "fum") and be_== (fumList))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))
      }
    }

    object `when used with (not contain inOrder xx and not contain inOrder xx)` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        fumList should (not contain inOrder ("fee", "fie", "foe", "fum") and not contain inOrder ("fee", "fie", "foe", "fum"))
        val e1 = intercept[TestFailedException] {
          fumList should (not contain inOrder ("fum", "foe", "fie", "fee") and not contain inOrder ("happy", "birthday", "to", "you"))
        }
        checkMessageStackDepth(e1, Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"fum\", \"foe\", \"fie\", \"fee\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not contain inOrder ("fee", "fie", "foe", "fum") and not contain inOrder ("fum", "foe", "fie", "fee"))
        }
        checkMessageStackDepth(e2, Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"fee\", \"fie\", \"foe\", \"fum\"") + ", but " + Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"fum\", \"foe\", \"fie\", \"fee\""), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality
        fumList should (not contain inOrder ("FEE", "FIE", "FOE", "FUM") and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))
        val e1 = intercept[TestFailedException] {
          fumList should (not contain inOrder ("FUM", "FOE", "FIE", "FEE") and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))
        }
        checkMessageStackDepth(e1, Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not contain inOrder ("FEE", "FIE", "FOE", "FUM") and (not contain inOrder ("FUM", "FOE", "FIE", "FEE")))
        }
        checkMessageStackDepth(e2, Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\"") + ", but " + Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\""), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (fumList should (not contain inOrder ("FEE", "FIE", "FOE", "FUM") and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not contain inOrder ("FEE", "FIE", "FOE", "FUM") and not contain inOrder ("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("didNotContainAllOfElementsInOrder", decorateToStringValue(fumList), "\"FEE\", \"FIE\", \"FOE\", \"FUM\"") + ", but " + Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (not contain inOrder ("FUM", "FOE", "FIE", "FEE") and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\""), fileName, thisLineNumber - 2)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          fumList should (not contain inOrder ("fee", "fie", "foe", "fie", "fum") and not contain inOrder ("fee", "fie", "foe", "fum"))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))

        val e2 = intercept[exceptions.NotAllowedException] {
          fumList should (not contain inOrder ("fee", "fie", "foe", "fum") and not contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e2.failedCodeFileName.get should be (fileName)
        e2.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e2.message should be (Some(Resources("inOrderDuplicate")))
      }
    }

    object `when used with (not equal xx and not contain inOrder xx)` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        fumList should (not equal (toList) and not contain inOrder ("fee", "fie", "foe", "fum"))
        val e1 = intercept[TestFailedException] {
          fumList should (not equal (fumList) and not contain inOrder ("fee", "fie", "foe", "fum"))
        }
        checkMessageStackDepth(e1, Resources("equaled", decorateToStringValue(fumList), decorateToStringValue(fumList)), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not equal (toList) and not contain inOrder ("fum", "foe", "fie", "fee"))
        }
        checkMessageStackDepth(e2, Resources("didNotEqual", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", but " + Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"fum\", \"foe\", \"fie\", \"fee\""), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality
        fumList should (not equal (toList) and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))
        val e1 = intercept[TestFailedException] {
          fumList should (not equal (fumList) and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))
        }
        checkMessageStackDepth(e1, Resources("equaled", decorateToStringValue(fumList), decorateToStringValue(fumList)), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not equal (toList) and (not contain inOrder ("FUM", "FOE", "FIE", "FEE")))
        }
        checkMessageStackDepth(e2, Resources("didNotEqual", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", but " + Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\""), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (fumList should (not equal (fumList) and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not equal (fumList) and not contain inOrder ("FUM", "FOE", "FIE", "FEE"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("didNotEqual", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", but " + Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (not equal (toList) and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, Resources("equaled", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          fumList should (not equal (toList) and not contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))
      }
    }

    object `when used with (not be xx and not contain inOrder xx)` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        fumList should (not be_== (toList) and not contain inOrder ("fee", "fie", "foe", "fum"))
        val e1 = intercept[TestFailedException] {
          fumList should (not be_== (fumList) and not contain inOrder ("fee", "fie", "foe", "fum"))
        }
        checkMessageStackDepth(e1, Resources("wasEqualTo", decorateToStringValue(fumList), decorateToStringValue(fumList)), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not be_== (toList) and not contain inOrder ("fum", "foe", "fie", "fee"))
        }
        checkMessageStackDepth(e2, Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", but " + Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"fum\", \"foe\", \"fie\", \"fee\""), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality
        fumList should (not be_== (toList) and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))
        val e1 = intercept[TestFailedException] {
          fumList should (not be_== (fumList) and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))
        }
        checkMessageStackDepth(e1, Resources("wasEqualTo", decorateToStringValue(fumList), decorateToStringValue(fumList)), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not be_== (toList) and (not contain inOrder ("FUM", "FOE", "FIE", "FEE")))
        }
        checkMessageStackDepth(e2, Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", but " + Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\""), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (fumList should (not be_== (toList) and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not be_== (toList) and not contain inOrder ("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", but " + Resources("containedAllOfElementsInOrder", decorateToStringValue(fumList), "\"FUM\", \"FOE\", \"FIE\", \"FEE\""), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (not be_== (fumList) and not contain inOrder ("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, Resources("wasEqualTo", decorateToStringValue(fumList), decorateToStringValue(fumList)), fileName, thisLineNumber - 2)
        (fumList should (not contain inOrder (" FEE ", " FIE ", " FOE ", " FUU ") and not contain inOrder (" FEE ", " FIE ", " FOE ", " FUU "))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          fumList should (not be_== (toList) and not contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))
      }
    }
  }

  object `every of Everys` {

    val list1s: Every[Every[Int]] = Every(Every(0, 1, 2, 2, 3), Every(0, 1, 2, 2, 3), Every(0, 1, 2, 2, 3))
    val lists: Every[Every[Int]] = Every(Every(0, 1, 2, 3, 3), Every(0, 1, 2, 3, 3), Every(8, 2, 3, 4))
    val hiLists: Every[Every[String]] = Every(Every("he", "hi", "hello"), Every("he", "hi", "hello"), Every("he", "hi", "hello"))

    def allErrMsg(index: Int, message: String, lineNumber: Int, left: Any): String =
      "'all' inspection failed, because: \n" +
        "  at index " + index + ", " + message + " (" + fileName + ":" + (lineNumber) + ") \n" +
        "in " + decorateToStringValue(left)

    object `used with contain inOrder xx and contain inOrder xx` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        all (list1s) should (contain inOrder (1, 2, 3) and contain inOrder (1, 2, 3))
        atLeast (2, lists) should (contain inOrder (1, 2, 3) and contain inOrder (1, 2, 3))
        atMost (2, lists) should (contain inOrder (1, 2, 3) and contain inOrder (1, 2, 3))
        no (lists) should (contain inOrder (3, 6, 9) and contain inOrder (3, 4, 5))

        val e1 = intercept[TestFailedException] {
          all (lists) should (contain inOrder (1, 2, 3) and contain inOrder (1, 2, 3))
        }
        checkMessageStackDepth(e1, allErrMsg(2, decorateToStringValue(lists(2)) + " did not contain all of " + "(1, 2, 3)" + " in order", thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (list1s) should (contain inOrder (1, 2, 3) and contain inOrder (1, 3, 4))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(list1s(0)) + " contained all of " + "(1, 2, 3)" + " in order" + ", but " + decorateToStringValue(list1s(0)) + " did not contain all of " + "(1, 3, 4)" + " in order", thisLineNumber - 2, list1s), fileName, thisLineNumber - 2)

        val e3 = intercept[TestFailedException] {
          all (hiLists) should (contain inOrder ("hi", "hello") and contain inOrder ("hello", "hi"))
        }
        checkMessageStackDepth(e3, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained all of " + "(\"hi\", \"hello\")" + " in order" + ", but " + decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"hello\", \"hi\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (contain inOrder ("HI", "HELLO") and contain inOrder ("HI", "HELLO"))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (contain inOrder ("HO", "HELLO") and contain inOrder ("HI", "HELLO"))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"HO\", \"HELLO\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (hiLists) should (contain inOrder ("HI", "HELLO") and contain inOrder ("HELLO", "HI"))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained all of " + "(\"HI\", \"HELLO\")" + " in order" + ", but " + decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"HELLO\", \"HI\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (all (hiLists) should (contain inOrder ("HI", "HELLO") and contain inOrder ("HI", "HELLO"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (contain inOrder ("HO", "HELLO") and contain inOrder ("HI", "HELLO"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"HO\", \"HELLO\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          (all (hiLists) should (contain inOrder ("HI", "HELLO") and contain inOrder ("HELLO", "HI"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained all of " + "(\"HI\", \"HELLO\")" + " in order" + ", but " + decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"HELLO\", \"HI\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          all (list1s) should (contain inOrder (1, 2, 2, 3) and contain inOrder (1, 2, 3))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))

        val e2 = intercept[exceptions.NotAllowedException] {
          all (list1s) should (contain inOrder (1, 2, 3) and contain inOrder (1, 2, 2, 3))
        }
        e2.failedCodeFileName.get should be (fileName)
        e2.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e2.message should be (Some(Resources("inOrderDuplicate")))
      }
    }

    object `when used with (be xx and contain inOrder xx)` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        all (list1s) should (be_== (Many(0, 1, 2, 2, 3)) and contain inOrder (1, 2, 3))
        atLeast (2, lists) should (be_== (Many(0, 1, 2, 3, 3)) and contain inOrder (1, 2, 3))
        atMost (2, lists) should (be_== (Many(3, 2, 1)) and contain inOrder (1, 2, 3))
        no (lists) should (be_== (Many(3, 6, 9)) and contain inOrder (3, 4, 5))

        val e1 = intercept[TestFailedException] {
          all (lists) should (be_== (Many(0, 1, 2, 3, 3)) and contain inOrder (1, 2, 3))
        }
        checkMessageStackDepth(e1, allErrMsg(2, decorateToStringValue(lists(2)) + " was not equal to " + decorateToStringValue(Many(0, 1, 2, 3, 3)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (list1s) should (be_== (Many(0, 1, 2, 2, 3)) and contain inOrder (2, 3, 8))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(list1s(0)) + " was equal to " + decorateToStringValue(Many(0, 1, 2, 2, 3)) + ", but " + decorateToStringValue(list1s(0)) + " did not contain all of " + "(2, 3, 8)" + " in order", thisLineNumber - 2, list1s), fileName, thisLineNumber - 2)

        val e3 = intercept[TestFailedException] {
          all (hiLists) should (be_== (Many("he", "hi", "hello")) and contain inOrder ("hello", "hi"))
        }
        checkMessageStackDepth(e3, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(Many("he", "hi", "hello")) + ", but " + decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"hello\", \"hi\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e4 = intercept[TestFailedException] {
          all (list1s) should (be_== (Many(0, 1, 2, 2, 3)) and contain inOrder (2, 3, 8))
        }
        checkMessageStackDepth(e4, allErrMsg(0, decorateToStringValue(list1s(0)) + " was equal to " + decorateToStringValue(Many(0, 1, 2, 2, 3)) + ", but " + decorateToStringValue(list1s(0)) + " did not contain all of " + "(2, 3, 8)" + " in order", thisLineNumber - 2, list1s), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (be_== (Many("he", "hi", "hello")) and contain inOrder ("HI", "HELLO"))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (be_== (Many("HI", "HELLO")) and contain inOrder ("HI", "HELLO"))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was not equal to " + decorateToStringValue(Many("HI", "HELLO")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (hiLists) should (be_== (Many("he", "hi", "hello")) and contain inOrder ("HELLO", "HI"))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(Many("he", "hi", "hello")) + ", but " + decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"HELLO\", \"HI\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (all (hiLists) should (be_== (Many("he", "hi", "hello")) and contain inOrder ("HI", "HELLO"))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (be_== (Many("HI", "HELLO")) and contain inOrder ("HI", "HELLO"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was not equal to " + decorateToStringValue(Many("HI", "HELLO")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          (all (hiLists) should (be_== (Many("he", "hi", "hello")) and contain inOrder ("HELLO", "HI"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(Many("he", "hi", "hello")) + ", but " + decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"HELLO\", \"HI\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          all (list1s) should (be_== (Many(0, 1, 2, 2, 3)) and contain inOrder (1, 2, 2, 3))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))
      }
    }

    object `when used with (not contain inOrder xx and not contain inOrder xx)` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        all (list1s) should (not contain inOrder (3, 2, 8) and not contain inOrder (8, 3, 4))
        atLeast (2, lists) should (not contain inOrder (3, 8, 5) and not contain inOrder (8, 3, 4))
        atMost (2, lists) should (not contain inOrder (2, 4, 3) and contain inOrder (2, 3, 4))
        no (list1s) should (not contain inOrder (1, 2, 3) and not contain inOrder (1, 2, 3))

        val e1 = intercept[TestFailedException] {
          all (lists) should (not contain inOrder (2, 3, 4) and not contain inOrder (8, 3, 4))
        }
        checkMessageStackDepth(e1, allErrMsg(2, decorateToStringValue(lists(2)) + " contained all of " + "(2, 3, 4)" + " in order", thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (lists) should (not contain inOrder (3, 6, 8) and not contain inOrder (2, 3, 4))
        }
        checkMessageStackDepth(e2, allErrMsg(2, decorateToStringValue(lists(2)) + " did not contain all of " + "(3, 6, 8)" + " in order" + ", but " + decorateToStringValue(lists(2)) + " contained all of " + "(2, 3, 4)" + " in order", thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e3 = intercept[TestFailedException] {
          all (hiLists) should (not contain inOrder ("hi", "hello") and not contain inOrder ("ho", "hey", "howdy"))
        }
        checkMessageStackDepth(e3, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained all of " + "(\"hi\", \"hello\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e4 = intercept[TestFailedException] {
          all (hiLists) should (not contain inOrder ("ho", "hey", "howdy") and not contain inOrder ("hi", "hello"))
        }
        checkMessageStackDepth(e4, allErrMsg(0, decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"ho\", \"hey\", \"howdy\")" + " in order" + ", but " + decorateToStringValue(hiLists(0)) + " contained all of " + "(\"hi\", \"hello\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (not contain inOrder ("HELLO", "HI") and not contain inOrder ("HELLO", "HO"))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (not contain inOrder ("HI", "HELLO") and not contain inOrder ("HO", "HE"))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained all of " + "(\"HI\", \"HELLO\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (hiLists) should (not contain inOrder ("HELLO", "HI") and not contain inOrder ("HI", "HELLO"))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"HELLO\", \"HI\")" + " in order" + ", but " + decorateToStringValue(hiLists(0)) + " contained all of " + "(\"HI\", \"HELLO\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (all (hiLists) should (not contain inOrder ("HELLO", "HI") and not contain inOrder ("HELLO", "HO"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (not contain inOrder ("HI", "HELLO") and not contain inOrder ("HO", "HE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained all of " + "(\"HI\", \"HELLO\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          (all (hiLists) should (not contain inOrder ("HELLO", "HI") and not contain inOrder ("HI", "HELLO"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " did not contain all of " + "(\"HELLO\", \"HI\")" + " in order" + ", but " + decorateToStringValue(hiLists(0)) + " contained all of " + "(\"HI\", \"HELLO\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          all (list1s) should (not contain inOrder (1, 2, 2, 3) and not contain inOrder (8, 3, 4))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))

        val e2 = intercept[exceptions.NotAllowedException] {
          all (list1s) should (not contain inOrder (1, 2, 2, 3) and not contain inOrder (8, 3, 4))
        }
        e2.failedCodeFileName.get should be (fileName)
        e2.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e2.message should be (Some(Resources("inOrderDuplicate")))
      }
    }

    object `when used with (not be xx and not contain inOrder xx)` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        all (list1s) should (not be_== (One(2)) and not contain inOrder (8, 3, 4))
        atLeast (2, lists) should (not be_== (One(3)) and not contain inOrder (8, 3, 4))
        atMost (2, lists) should (not be_== (Many(8, 2, 3, 4)) and not contain inOrder (2, 3, 4))
        no (list1s) should (not be_== (Many(0, 1, 2, 2, 3)) and not contain inOrder (1, 2, 3))

        val e1 = intercept[TestFailedException] {
          all (lists) should (not be_== (Many(8, 2, 3, 4)) and not contain inOrder (8, 3, 4))
        }
        checkMessageStackDepth(e1, allErrMsg(2, decorateToStringValue(lists(2)) + " was equal to " + decorateToStringValue(Many(8, 2, 3, 4)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (lists) should (not be_== (One(3)) and not contain inOrder (2, 3, 4))
        }
        checkMessageStackDepth(e2, allErrMsg(2, decorateToStringValue(lists(2)) + " was not equal to " + decorateToStringValue(One(3)) + ", but " + decorateToStringValue(lists(2)) + " contained all of " + "(2, 3, 4)" + " in order", thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e3 = intercept[TestFailedException] {
          all (hiLists) should (not be_== (Many("he", "hi", "hello")) and not contain inOrder ("ho", "hey", "howdy"))
        }
        checkMessageStackDepth(e3, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(Many("he", "hi", "hello")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e4 = intercept[TestFailedException] {
          all (hiLists) should (not be_== (One("ho")) and not contain inOrder ("hi", "hello"))
        }
        checkMessageStackDepth(e4, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was not equal to " + decorateToStringValue(One("ho")) + ", but " + decorateToStringValue(hiLists(0)) + " contained all of " + "(\"hi\", \"hello\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should use the implicit Equality in scope` {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (not be_== (One("ho")) and not contain inOrder ("HO", "HELLO"))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (not be_== (Many("he", "hi", "hello")) and not contain inOrder ("HELLO", "HI"))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(Many("he", "hi", "hello")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (hiLists) should (not be_== (One("ho")) and not contain inOrder ("HI", "HELLO"))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was not equal to " + decorateToStringValue(One("ho")) + ", but " + decorateToStringValue(hiLists(0)) + " contained all of " + "(\"HI\", \"HELLO\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should use an explicitly provided Equality` {
        (all (hiLists) should (not be_== (One("ho")) and not contain inOrder ("HO", "HELLO"))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (not be_== (Many("he", "hi", "hello")) and not contain inOrder ("HELLO", "HI"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(Many("he", "hi", "hello")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          (all (hiLists) should (not be_== (One("ho")) and not contain inOrder ("HI", "HELLO"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was not equal to " + decorateToStringValue(One("ho")) + ", but " + decorateToStringValue(hiLists(0)) + " contained all of " + "(\"HI\", \"HELLO\")" + " in order", thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      def `should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value` {
        val e1 = intercept[exceptions.NotAllowedException] {
          all (list1s) should (not be_== (One(2)) and not contain inOrder (1, 2, 2, 3))
        }
        e1.failedCodeFileName.get should be (fileName)
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources("inOrderDuplicate")))
      }
    }
  }
}
