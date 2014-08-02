/*
 * Copyright 2001-2013 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalactic

import annotation.implicitNotFound
import scala.language.higherKinds

/**
 * Abstract class used to enforce type constraints for equality checks.
 *
 * <p>
 * For more information on how this class is used, see the documentation of <a href="TripleEqualsSupport.html"><code>TripleEqualsSupport</code></a>.
 * </p>
 */
@implicitNotFound(msg = "types ${A} and ${B} do not adhere to the type constraint selected for the === and !== operators; the missing implicit parameter is of type org.scalactic.EqualityConstraint[${A},${B}]")
abstract class EqualityConstraint[A, B] { thisConstraint =>

  /**
   * Indicates whether the objects passed as <code>a</code> and <code>b</code> are equal.
   *
   * @param a a left-hand-side object being compared with another (right-hand-side one) for equality (<em>e.g.</em>, <code>a == b</code>)
   * @param b a right-hand-side object being compared with another (left-hand-side one) for equality (<em>e.g.</em>, <code>a == b</code>)
   */
  def areEqual(a: A, b: B): Boolean
}

object EqualityConstraint extends LowPriorityEqualityConstraints {

  import TripleEqualsSupport.BasicConstraint

  implicit def seqEqualityConstraint[EA, CA[ea] <: collection.GenSeq[ea], EB, CB[eb] <: collection.GenSeq[eb]](implicit equalityOfA: Equality[CA[EA]], ev: InnerConstraint[EA, EB]): Constraint[CA[EA], CB[EB]] = new BasicConstraint[CA[EA], CB[EB]](equalityOfA)

  implicit def arrayOnLeftEqualityConstraint[EA, CA[ea] <: Array[ea], EB, CB[eb] <: collection.GenSeq[eb]](implicit equalityOfA: Equality[CA[EA]], ev: InnerConstraint[EA, EB]): Constraint[CA[EA], CB[EB]] = new BasicConstraint[CA[EA], CB[EB]](equalityOfA)

  implicit def arrayOnRightEqualityConstraint[EA, CA[ea] <: collection.GenSeq[ea], EB, CB[eb] <: Array[eb]](implicit equalityOfA: Equality[CA[EA]], ev: InnerConstraint[EA, EB]): Constraint[CA[EA], CB[EB]] = new BasicConstraint[CA[EA], CB[EB]](equalityOfA)

  implicit def arrayOnBothSidesConstraint[EA, EB](implicit equalityOfA: Equality[Array[EA]], ev: InnerConstraint[EA, EB]): Constraint[Array[EA], Array[EB]] = new BasicConstraint[Array[EA], Array[EB]](equalityOfA)

  implicit def setEqualityConstraint[EA, CA[ea] <: collection.GenSet[ea], EB, CB[eb] <: collection.GenSet[eb]](implicit equalityOfA: Equality[CA[EA]], ev: InnerConstraint[EA, EB]): Constraint[CA[EA], CB[EB]] = new BasicConstraint[CA[EA], CB[EB]](equalityOfA)

  implicit def mapEqualityConstraint[KA, VA, CA[ka, kb] <: collection.GenMap[ka, kb], KB, VB, CB[kb, vb] <: collection.GenMap[kb, vb]](implicit equalityOfA: Equality[CA[KA, VA]], evKey: InnerConstraint[KA, KB], evValue: InnerConstraint[VA, VB]): Constraint[CA[KA, VA], CB[KB, VB]] = new BasicConstraint[CA[KA, VA], CB[KB, VB]](equalityOfA)

  implicit def numericEqualityConstraint[A, B](implicit equalityOfA: Equality[A], numA: CooperatingNumeric[A], numB: CooperatingNumeric[B]): Constraint[A, B] = new BasicConstraint[A, B](equalityOfA)

  // 1. Every on left, can by subclass of Every on right
  // 2. Every on right, can be subclass of Every on left
  // 3. One on left, can be One or Every on right, but the latter will be provided by number 2
  // 4. One on right, can be One or Every on left, but the latter will be provided by number 1
  // 5. Many on left, can be Many or Every on right, but the latter will be provided by number 2
  // 6. Many on right, can be Many or Every on left, but the latter will be provided by number 1
  implicit def everyOnLeftEqualityConstraint[EA, EB, CB[eb] <: Every[eb]](implicit equalityOfA: Equality[Every[EA]], ev: InnerConstraint[EA, EB]): Constraint[Every[EA], CB[EB]] = new BasicConstraint[Every[EA], CB[EB]](equalityOfA)

  implicit def oneOnBothSidesqualityConstraint[EA, EB](implicit equalityOfA: Equality[One[EA]], ev: InnerConstraint[EA, EB]): Constraint[One[EA], One[EB]] = new BasicConstraint[One[EA], One[EB]](equalityOfA)

  implicit def manyOnBothSidesEqualityConstraint[EA, EB](implicit equalityOfA: Equality[Many[EA]], ev: InnerConstraint[EA, EB]): Constraint[Many[EA], Many[EB]] = new BasicConstraint[Many[EA], Many[EB]](equalityOfA)

  // ELG Element Left Good
  // ELB Element Left Bad
  // ERG Element Right Good
  // ERB Element Right Bad
  // This one will provide an equality constraint if the Good types have an inner constraint. It doesn't matter
  // in this case what the Bad type does. If there isn't one for the Good type, the lower priority implicit method
  // LowPriorityConstraints.lowPriorityOrEqualityConstraint will be checked will see
  // If there's an InnerConstraint for the Bad types.
  implicit def orEqualityConstraint[ELG, ELB, ERG, ERB](implicit equalityOfL: Equality[Or[ELG, ELB]], ev: InnerConstraint[ELG, ERG]): Constraint[Or[ELG, ELB], Or[ERG, ERB]] = new BasicConstraint[Or[ELG, ELB], Or[ERG, ERB]](equalityOfL)

  implicit def orOnBothSidesWithBadNothingConstraint[ELG, ERG](implicit equalityOfL: Equality[Or[ELG, Nothing]], ev: InnerConstraint[ELG, ERG]): Constraint[Or[ELG, Nothing], Or[ERG, Nothing]] = new BasicConstraint[Or[ELG, Nothing], Or[ERG, Nothing]](equalityOfL)

  implicit def goodOnLeftOrOnRightEqualityConstraint[ELG, ELB, ERG, ERB](implicit equalityOfL: Equality[Good[ELG, ELB]], ev: InnerConstraint[ELG, ERG]): Constraint[Good[ELG, ELB], Or[ERG, ERB]] = new BasicConstraint[Good[ELG, ELB], Or[ERG, ERB]](equalityOfL)

  implicit def goodOnLeftOrOnRightNothingConstraint[ELG, ERG](implicit equalityOfL: Equality[Good[ELG, Nothing]], ev: InnerConstraint[ELG, ERG]): Constraint[Good[ELG, Nothing], Or[ERG, Nothing]] = new BasicConstraint[Good[ELG, Nothing], Or[ERG, Nothing]](equalityOfL)

  implicit def orOnLeftGoodOnRightEqualityConstraint[ELG, ELB, ERG, ERB](implicit equalityOfL: Equality[Or[ELG, ELB]], ev: InnerConstraint[ELG, ERG]): Constraint[Or[ELG, ELB], Good[ERG, ERB]] = new BasicConstraint[Or[ELG, ELB], Good[ERG, ERB]](equalityOfL)

  implicit def orOnLeftGoodOnRightNothingConstraint[ELG, ERG](implicit equalityOfL: Equality[Or[ELG, Nothing]], ev: InnerConstraint[ELG, ERG]): Constraint[Or[ELG, Nothing], Good[ERG, Nothing]] = new BasicConstraint[Or[ELG, Nothing], Good[ERG, Nothing]](equalityOfL)

  implicit def goodOnLeftGoodOnRightEqualityConstraint[ELG, ELB, ERG, ERB](implicit equalityOfL: Equality[Good[ELG, ELB]], ev: InnerConstraint[ELG, ERG]): Constraint[Good[ELG, ELB], Good[ERG, ERB]] = new BasicConstraint[Good[ELG, ELB], Good[ERG, ERB]](equalityOfL)

  implicit def goodOnLeftGoodOnRightNothingConstraint[ELG, ERG](implicit equalityOfL: Equality[Good[ELG, Nothing]], ev: InnerConstraint[ELG, ERG]): Constraint[Good[ELG, Nothing], Good[ERG, Nothing]] = new BasicConstraint[Good[ELG, Nothing], Good[ERG, Nothing]](equalityOfL)

  implicit def badOnLeftOrOnRightEqualityConstraint[ELG, ELB, ERG, ERB](implicit equalityOfL: Equality[Bad[ELG, ELB]], ev: InnerConstraint[ELB, ERB]): Constraint[Bad[ELG, ELB], Or[ERG, ERB]] = new BasicConstraint[Bad[ELG, ELB], Or[ERG, ERB]](equalityOfL)

  implicit def badOnLeftOrOnRightNothingConstraint[ELB, ERB](implicit equalityOfL: Equality[Bad[Nothing, ELB]], ev: InnerConstraint[ELB, ERB]): Constraint[Bad[Nothing, ELB], Or[Nothing, ERB]] = new BasicConstraint[Bad[Nothing, ELB], Or[Nothing, ERB]](equalityOfL)

  implicit def orOnLeftBadOnRightEqualityConstraint[ELG, ELB, ERG, ERB](implicit equalityOfL: Equality[Or[ELG, ELB]], ev: InnerConstraint[ELB, ERB]): Constraint[Or[ELG, ELB], Bad[ERG, ERB]] = new BasicConstraint[Or[ELG, ELB], Bad[ERG, ERB]](equalityOfL)

  implicit def orOnLeftBadOnRightNothingConstraint[ELB, ERB](implicit equalityOfL: Equality[Or[Nothing, ELB]], ev: InnerConstraint[ELB, ERB]): Constraint[Or[Nothing, ELB], Bad[Nothing, ERB]] = new BasicConstraint[Or[Nothing, ELB], Bad[Nothing, ERB]](equalityOfL)

  implicit def badOnLeftBadOnRightEqualityConstraint[ELG, ELB, ERG, ERB](implicit equalityOfL: Equality[Bad[ELG, ELB]], ev: InnerConstraint[ELB, ERB]): Constraint[Bad[ELG, ELB], Bad[ERG, ERB]] = new BasicConstraint[Bad[ELG, ELB], Bad[ERG, ERB]](equalityOfL)

  implicit def badOnLeftBadOnRightNothingConstraint[ELB, ERB](implicit equalityOfL: Equality[Bad[Nothing, ELB]], ev: InnerConstraint[ELB, ERB]): Constraint[Bad[Nothing, ELB], Bad[Nothing, ERB]] = new BasicConstraint[Bad[Nothing, ELB], Bad[Nothing, ERB]](equalityOfL)

  // Either (in x === y, x is the "target" of the === invocation, y is the "parameter")
  // ETL Element Target Left
  // ETR Element Target Right
  // EPL Element Parameter Left
  // EPR Element Parameter Right
  // This one will provide an equality constraint if the Left types have an inner constraint. It doesn't matter
  // in this case what the Right type does. If there isn't one for the Left type, the lower priority implicit method
  // LowPriorityConstraints.lowPriorityEitherEqualityConstraint will be checked will see
  // If there's an InnerConstraint for the Bad types.
  implicit def eitherEqualityConstraint[ETL, ETR, EPL, EPR](implicit equalityOfT: Equality[Either[ETL, ETR]], ev: InnerConstraint[ETL, EPL]): Constraint[Either[ETL, ETR], Either[EPL, EPR]] = new BasicConstraint[Either[ETL, ETR], Either[EPL, EPR]](equalityOfT)
  implicit def eitherNothingConstraint[ETL, EPL](implicit equalityOfT: Equality[Either[ETL, Nothing]], ev: InnerConstraint[ETL, EPL]): Constraint[Either[ETL, Nothing], Either[EPL, Nothing]] = new BasicConstraint[Either[ETL, Nothing], Either[EPL, Nothing]](equalityOfT)

  implicit def leftOnParamSideEitherOnTargetSideEqualityConstraint[ETL, ETR, EPL, EPR](implicit equalityOfT: Equality[Left[ETL, ETR]], ev: InnerConstraint[ETL, EPL]): Constraint[Left[ETL, ETR], Either[EPL, EPR]] = new BasicConstraint[Left[ETL, ETR], Either[EPL, EPR]](equalityOfT)
  implicit def leftOnParamSideEitherOnTargetSideNothingConstraint[ETL, EPL](implicit equalityOfT: Equality[Left[ETL, Nothing]], ev: InnerConstraint[ETL, EPL]): Constraint[Left[ETL, Nothing], Either[EPL, Nothing]] = new BasicConstraint[Left[ETL, Nothing], Either[EPL, Nothing]](equalityOfT)

  implicit def eitherOnParamSideLeftOnTargetSideEqualityConstraint[ETL, ETR, EPL, EPR](implicit equalityOfT: Equality[Either[ETL, ETR]], ev: InnerConstraint[ETL, EPL]): Constraint[Either[ETL, ETR], Left[EPL, EPR]] = new BasicConstraint[Either[ETL, ETR], Left[EPL, EPR]](equalityOfT)
  implicit def eitherOnParamSideLeftOnTargetSideNothingConstraint[ETL, EPL](implicit equalityOfT: Equality[Either[ETL, Nothing]], ev: InnerConstraint[ETL, EPL]): Constraint[Either[ETL, Nothing], Left[EPL, Nothing]] = new BasicConstraint[Either[ETL, Nothing], Left[EPL, Nothing]](equalityOfT)

  implicit def leftOnParamSideLeftOnTargetSideEqualityConstraint[ETL, ETR, EPL, EPR](implicit equalityOfT: Equality[Left[ETL, ETR]], ev: InnerConstraint[ETL, EPL]): Constraint[Left[ETL, ETR], Left[EPL, EPR]] = new BasicConstraint[Left[ETL, ETR], Left[EPL, EPR]](equalityOfT)
  implicit def leftOnParamSideLeftOnTargetSideNothingConstraint[ETL, EPL](implicit equalityOfT: Equality[Left[ETL, Nothing]], ev: InnerConstraint[ETL, EPL]): Constraint[Left[ETL, Nothing], Left[EPL, Nothing]] = new BasicConstraint[Left[ETL, Nothing], Left[EPL, Nothing]](equalityOfT)

  implicit def rightOnParamSideEitherOnTargetSideEqualityConstraint[ETL, ETR, EPL, EPR](implicit equalityOfT: Equality[Right[ETL, ETR]], ev: InnerConstraint[ETR, EPR]): Constraint[Right[ETL, ETR], Either[EPL, EPR]] = new BasicConstraint[Right[ETL, ETR], Either[EPL, EPR]](equalityOfT)
  implicit def rightOnParamSideEitherOnTargetSideNothingConstraint[ETR, EPR](implicit equalityOfT: Equality[Right[Nothing, ETR]], ev: InnerConstraint[ETR, EPR]): Constraint[Right[Nothing, ETR], Either[Nothing, EPR]] = new BasicConstraint[Right[Nothing, ETR], Either[Nothing, EPR]](equalityOfT)

  implicit def eitherOnParamSideRightOnTargetSideEqualityConstraint[ETL, ETR, EPL, EPR](implicit equalityOfT: Equality[Either[ETL, ETR]], ev: InnerConstraint[ETR, EPR]): Constraint[Either[ETL, ETR], Right[EPL, EPR]] = new BasicConstraint[Either[ETL, ETR], Right[EPL, EPR]](equalityOfT)
  implicit def eitherOnParamSideRightOnTargetSideNothingConstraint[ETR, EPR](implicit equalityOfT: Equality[Either[Nothing, ETR]], ev: InnerConstraint[ETR, EPR]): Constraint[Either[Nothing, ETR], Right[Nothing, EPR]] = new BasicConstraint[Either[Nothing, ETR], Right[Nothing, EPR]](equalityOfT)

  implicit def rightOnParamSideRightOnTargetSideEqualityConstraint[ETL, ETR, EPL, EPR](implicit equalityOfT: Equality[Right[ETL, ETR]], ev: InnerConstraint[ETR, EPR]): Constraint[Right[ETL, ETR], Right[EPL, EPR]] = new BasicConstraint[Right[ETL, ETR], Right[EPL, EPR]](equalityOfT)
  implicit def rightOnParamSideRightOnTargetSideNothingConstraint[ETR, EPR](implicit equalityOfT: Equality[Right[Nothing, ETR]], ev: InnerConstraint[ETR, EPR]): Constraint[Right[Nothing, ETR], Right[Nothing, EPR]] = new BasicConstraint[Right[Nothing, ETR], Right[Nothing, EPR]](equalityOfT)
}