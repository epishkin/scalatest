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
package org.scalatest.words

import org.scalatest.matchers._
import org.scalatest.enablers._
import scala.collection.GenTraversable
import org.scalautils._
import org.scalatest.FailureMessages
import org.scalatest.UnquotedString
import org.scalautils.Equality
import org.scalatest.Assertions.areEqualComparingArraysStructurally
import org.scalatest.Matchers.matchSymbolToPredicateMethod

/**
 * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="../Matchers.html"><code>Matchers</code></a> for an overview of
 * the matchers DSL.
 *
 * @author Bill Venners
 */
final class NotWord {

  /**
   * This method enables the following syntax, where <code>tempFile</code>, for example, refers to a <code>java.io.File</code>
   * and <code>exist</code> is a <code>Matcher[java.io.File]</code>: 
   *
   * <pre class="stHighlight">
   * tempFile should not (exist)
   *                     ^
   * </pre>
   */
  def apply[S <: Any](matcher: Matcher[S]): Matcher[S] =
    new Matcher[S] {
      def apply(left: S): MatchResult =
        matcher(left) match {
          case MatchResult(bool, s1, s2, s3, s4) => MatchResult(!bool, s2, s1, s4, s3)
        }
    }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * hasNoSize should not { have size (2) and equal (hasNoSize) }
   *                      ^
   * </pre>
   */
  def apply[S <: Any, TYPECLASS[_]](matcherGen1: MatcherFactory1[S, TYPECLASS]): MatcherFactory1[S, TYPECLASS] = {
    new MatcherFactory1[S, TYPECLASS] {
      def matcher[V <: S : TYPECLASS]: Matcher[V] = {
        val innerMatcher: Matcher[V] = matcherGen1.matcher
        new Matcher[V] {
          def apply(left: V): MatchResult = {
            innerMatcher(left) match {
              case MatchResult(bool, s1, s2, s3, s4) => MatchResult(!bool, s2, s1, s4, s3)
            }
          }
        }
      }
    }
  }

  def apply[S <: Any, TYPECLASS1[_], TYPECLASS2[_]](matcherGen2: MatcherFactory2[S, TYPECLASS1, TYPECLASS2]): MatcherFactory2[S, TYPECLASS1, TYPECLASS2] = {
    new MatcherFactory2[S, TYPECLASS1, TYPECLASS2] {
      def matcher[V <: S : TYPECLASS1 : TYPECLASS2]: Matcher[V] = {
        val innerMatcher: Matcher[V] = matcherGen2.matcher
        new Matcher[V] {
          def apply(left: V): MatchResult = {
            innerMatcher(left) match {
              case MatchResult(bool, s1, s2, s3, s4) => MatchResult(!bool, s2, s1, s4, s3)
            }
          }
        }
      }
    }
  }

  /**
   * This method enables any <code>BeMatcher</code> to be negated by passing it to <code>not</code>. 
   * For example, if you have a <code>BeMatcher[Int]</code> called <code>odd</code>, which matches
   * <code>Int</code>s that are odd, you can negate it to get a <code>BeMatcher[Int]</code> that matches
   * even <code>Int</code>s, like this:
   *
   * <pre class="stHighlight">
   * val even = not (odd)
   *                ^
   * </pre>
   *
   * <p>
   * In addition, this method enables you to negate a <code>BeMatcher</code> at its point of use, like this:
   * </p>
   *
   * <pre class="stHighlight">
   * num should be (not (odd))
   * </pre>
   *
   * <p>
   * Nevertheless, in such as case it would be more idiomatic to write:
   * </p>
   *
   * <pre class="stHighlight">
   * num should not be (odd)
   * </pre>
   */
  def apply[S <: Any](beMatcher: BeMatcher[S]): BeMatcher[S] =
    new BeMatcher[S] {
      def apply(left: S): MatchResult =
        beMatcher(left) match {
          case MatchResult(bool, s1, s2, s3, s4) => MatchResult(!bool, s2, s1, s4, s3)
        }
    }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * num should (not equal (7) and be &lt; (9))
   *                 ^
   * </pre>
   */
  def equal(right: Any): MatcherFactory1[Any, Equality] = apply(MatcherWords.equal(right))

  /**
   * This method enables the following syntax for the "primitive" numeric types: 
   *
   * <pre class="stHighlight">
   * sevenDotOh should ((not equal (17.1 plusOrMinus 0.2)) and (not equal (27.1 plusOrMinus 0.2)))
   *                         ^
   * </pre>
   */
  def equal[U](interval: Interval[U]): Matcher[U] = {
    new Matcher[U] {
      def apply(left: U): MatchResult = {
        MatchResult(
          !(interval.isWithin(left)),
          FailureMessages("equaledPlusOrMinus", left, interval.pivot, interval.tolerance),
          FailureMessages("didNotEqualPlusOrMinus", left, interval.pivot, interval.tolerance)
        )
      }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * map should (not equal (null))
   *                 ^
   * </pre>
   */
  def equal(o: Null): Matcher[AnyRef] =
    new Matcher[AnyRef] {
      def apply(left: AnyRef): MatchResult = {
        MatchResult(
          left != null,
          FailureMessages("equaledNull"),
          FailureMessages("didNotEqualNull", left),
          FailureMessages("midSentenceEqualedNull"),
          FailureMessages("didNotEqualNull", left)
        )
      }
    }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * Array(1, 2) should (not have length (5) and not have length (3))
   *                         ^
   * </pre>
   */
  def have(resultOfLengthWordApplication: ResultOfLengthWordApplication): MatcherFactory1[Any, Length] =
    apply(MatcherWords.have.length(resultOfLengthWordApplication.expectedLength))

  // This looks similar to the AndNotWord one, but not quite the same because no and
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * Array(1, 2) should (not have size (5) and not have size (3))
   *                         ^
   * </pre>
   */
  def have(resultOfSizeWordApplication: ResultOfSizeWordApplication): MatcherFactory1[Any, Size] =
    apply(MatcherWords.have.size(resultOfSizeWordApplication.expectedSize))

  /**
   * This method enables the following syntax, where, for example, <code>book</code> is of type <code>Book</code> and <code>title</code> and <code>author</code>
   * are both of type <code>HavePropertyMatcher[Book, String]</code>:
   *
   * <pre class="stHighlight">
   * book should (not have (title ("Moby Dick")) and (not have (author ("Melville"))))
   *                  ^
   * </pre>
   */
  def have[T](firstPropertyMatcher: HavePropertyMatcher[T, _], propertyMatchers: HavePropertyMatcher[T, _]*): Matcher[T] =
    apply(MatcherWords.have(firstPropertyMatcher, propertyMatchers: _*))

  /**
   * This method enables the following syntax, where, for example, <code>num</code> is an <code>Int</code> and <code>odd</code>
   * of type <code>BeMatcher[Int]</code>:
   *
   * <pre class="stHighlight">
   * num should (not be (odd) and be <= (8))
   *                 ^
   * </pre>
   */
  def be[T](beMatcher: BeMatcher[T]): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult =
        beMatcher(left) match {
          case MatchResult(bool, s1, s2, s3, s4) => MatchResult(!bool, s2, s1, s4, s3)
        }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * map should (not be (null))
   *                 ^
   * </pre>
   */
  def be(o: Null): Matcher[AnyRef] =
    new Matcher[AnyRef] {
      def apply(left: AnyRef): MatchResult = {
        MatchResult(
          left != null,
          FailureMessages("wasNull"),
          FailureMessages("wasNotNull", left),
          FailureMessages("midSentenceWasNull"),
          FailureMessages("wasNotNull", left)
        )
      }
    }

  // These next four are for things like not be </>/<=/>=:
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * num should (not be < (7) and not be > (10))
   *                 ^
   * </pre>
   */
  def be[T](resultOfLessThanComparison: ResultOfLessThanComparison[T]): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult =
        MatchResult(
          !resultOfLessThanComparison(left),
          FailureMessages("wasLessThan", left, resultOfLessThanComparison.right),
          FailureMessages("wasNotLessThan", left, resultOfLessThanComparison.right)
        )
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * num should (not be > (10) and not be < (7))
   *                 ^
   * </pre>
   */
  def be[T](resultOfGreaterThanComparison: ResultOfGreaterThanComparison[T]): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult =
        MatchResult(
          !resultOfGreaterThanComparison(left),
          FailureMessages("wasGreaterThan", left, resultOfGreaterThanComparison.right),
          FailureMessages("wasNotGreaterThan", left, resultOfGreaterThanComparison.right)
        )
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * num should (not be <= (7) and not be > (10))
   *                 ^
   * </pre>
   */
  def be[T](resultOfLessThanOrEqualToComparison: ResultOfLessThanOrEqualToComparison[T]): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult =
        MatchResult(
          !resultOfLessThanOrEqualToComparison(left),
          FailureMessages("wasLessThanOrEqualTo", left, resultOfLessThanOrEqualToComparison.right),
          FailureMessages("wasNotLessThanOrEqualTo", left, resultOfLessThanOrEqualToComparison.right)
        )
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * num should (not be >= (10) and not be < (7))
   *                 ^
   * </pre>
   */
  def be[T](resultOfGreaterThanOrEqualToComparison: ResultOfGreaterThanOrEqualToComparison[T]): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult =
        MatchResult(
          !resultOfGreaterThanOrEqualToComparison(left),
          FailureMessages("wasGreaterThanOrEqualTo", left, resultOfGreaterThanOrEqualToComparison.right),
          FailureMessages("wasNotGreaterThanOrEqualTo", left, resultOfGreaterThanOrEqualToComparison.right)
        )
    }
  }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * num should (not be === (7) and not be === (10))
   *                 ^
   * </pre>
   */
  def be(tripleEqualsInvocation: TripleEqualsInvocation[_]): Matcher[Any] = {
    new Matcher[Any] {
      def apply(left: Any): MatchResult =
        MatchResult(
          !(left == tripleEqualsInvocation.right),
          FailureMessages("wasEqualTo", left, tripleEqualsInvocation.right),
          FailureMessages("wasNotEqualTo", left, tripleEqualsInvocation.right)
        )
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * myFile should (not be ('hidden) and have (name ("temp.txt")))
   *                    ^
   * </pre>
   */
  def be[T <: AnyRef](symbol: Symbol): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult = {
        val positiveMatchResult = matchSymbolToPredicateMethod(left, symbol, false, false)
        MatchResult(
          !positiveMatchResult.matches,
          positiveMatchResult.negatedFailureMessage,
          positiveMatchResult.failureMessage
        )
      }
    }
  }

  /**
   * This method enables the following syntax, where <code>tempFile</code>, for example, refers to a <code>java.io.File</code>
   * and <code>hidden</code> is a <code>BePropertyMatcher[java.io.File]</code>: 
   *
   * <pre class="stHighlight">
   * tempFile should (not be (hidden) and have ('name ("temp.txt")))
   *                    ^
   * </pre>
   */
  def be[T <: AnyRef](bePropertyMatcher: BePropertyMatcher[T]): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult = {
        val result = bePropertyMatcher(left)
        MatchResult(
          !result.matches,
          FailureMessages("was", left, UnquotedString(result.propertyName)),
          FailureMessages("wasNot", left, UnquotedString(result.propertyName))
        )
      }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * isNotFileMock should (not be a ('file) and have ('name ("temp.txt"))))
   *                           ^
   * </pre>
   */
  def be[T <: AnyRef](resultOfAWordApplication: ResultOfAWordToSymbolApplication): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult = {
        val positiveMatchResult = matchSymbolToPredicateMethod(left, resultOfAWordApplication.symbol, true, true)
        MatchResult(
          !positiveMatchResult.matches,
          positiveMatchResult.negatedFailureMessage,
          positiveMatchResult.failureMessage
        )
      }
    }
  }

  /**
   * This method enables the following syntax, where <code>notSoSecretFile</code>, for example, refers to a <code>java.io.File</code>
   * and <code>directory</code> is a <code>BePropertyMatcher[java.io.File]</code>: 
   *
   * <pre class="stHighlight">
   * notSoSecretFile should (not be a (directory) and have ('name ("passwords.txt")))
   *                             ^
   * </pre>
   */
  def be[T <: AnyRef](resultOfAWordApplication: ResultOfAWordToBePropertyMatcherApplication[T]): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult = {
        val result = resultOfAWordApplication.bePropertyMatcher(left)
        MatchResult(
          !result.matches,
          FailureMessages("wasA", left, UnquotedString(result.propertyName)),
          FailureMessages("wasNotA", left, UnquotedString(result.propertyName))
        )
      }
    }
  }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * result should (not be a (passedMarks) and be a (validMarks)))
   *                    ^
   * </pre>
   */
  def be[T](resultOfAWordApplication: ResultOfAWordToAMatcherApplication[T]): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult = {
        val result = resultOfAWordApplication.aMatcher(left)
        MatchResult(
          !result.matches,
          result.negatedFailureMessage,
          result.failureMessage
        )
      }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * isNotAppleMock should (not be an ('apple) and not be ('rotten))
   *                            ^
   * </pre>
   */
  def be[T <: AnyRef](resultOfAnWordApplication: ResultOfAnWordToSymbolApplication): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult = {
        val positiveMatchResult = matchSymbolToPredicateMethod(left, resultOfAnWordApplication.symbol, true, false)
        MatchResult(
          !positiveMatchResult.matches,
          positiveMatchResult.negatedFailureMessage,
          positiveMatchResult.failureMessage
        )
      }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * myFile should (not be an (directory) and not be an (directory))
   *                    ^
   * </pre>
   */
  def be[T <: AnyRef](resultOfAnWordApplication: ResultOfAnWordToBePropertyMatcherApplication[T]): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult = {
        val result = resultOfAnWordApplication.bePropertyMatcher(left)
        MatchResult(
          !result.matches,
          FailureMessages("wasAn", left, UnquotedString(result.propertyName)),
          FailureMessages("wasNotAn", left, UnquotedString(result.propertyName))
        )
      }
    }
  }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * result should (not be a (passedMarks) and be a (validMarks)))
   *                    ^
   * </pre>
   */
  def be[T](resultOfAnWordApplication: ResultOfAnWordToAnMatcherApplication[T]): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult = {
        val result = resultOfAnWordApplication.anMatcher(left)
        MatchResult(
          !result.matches,
          result.negatedFailureMessage,
          result.failureMessage
        )
      }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * myFish should (not be theSameInstanceAs (redFish) and not be theSameInstanceAs (blueFish))
   *                    ^
   * </pre>
   */
  def be[T <: AnyRef](resultOfTheSameInstanceAsApplication: ResultOfTheSameInstanceAsApplication): Matcher[T] = {
    new Matcher[T] {
      def apply(left: T): MatchResult = {
        MatchResult(
          resultOfTheSameInstanceAsApplication.right ne left,
          FailureMessages("wasSameInstanceAs", left, resultOfTheSameInstanceAsApplication.right),
          FailureMessages("wasNotSameInstanceAs", left, resultOfTheSameInstanceAsApplication.right)
        )
      }
    }
  }

  /**
   * This method enables the following syntax for the "primitive" numeric types: 
   *
   * <pre class="stHighlight">
   * sevenDotOh should ((not be (17.1 plusOrMinus 0.2)) and (not be (27.1 plusOrMinus 0.2)))
   *                         ^
   * </pre>
   */
  def be[U](interval: Interval[U]): Matcher[U] = {
    new Matcher[U] {
      def apply(left: U): MatchResult = {
        MatchResult(
          // !(left <= right + tolerance && left >= right - tolerance),
          !(interval.isWithin(left)),
          FailureMessages("wasPlusOrMinus", left, interval.pivot, interval.tolerance),
          FailureMessages("wasNotPlusOrMinus", left, interval.pivot, interval.tolerance)
        )
      }
    }
  }

  /**
   * This method enables <code>be</code> to be used for inequality comparison. Here are some examples:
   *
   * <pre class="stHighlight">
   * result should not be (None)
   *                      ^
   * result should not be (Some(1))
   *                      ^
   * result should not be (true)
   *                      ^
   * result should not be (false)
   *                      ^
   * sum should not be (19)
   *                   ^
   * </pre>
   */
  def be(right: Any): Matcher[Any] = {
    new Matcher[Any] {
      def apply(left: Any): MatchResult = {
        left match {
          case null =>
            MatchResult(
              right != null, 
              FailureMessages("wasNull"),
              FailureMessages("wasNotNull", right),
              FailureMessages("midSentenceWasNull"),
              FailureMessages("wasNotNull", right)
            )
          case _ => 
            MatchResult(
              !areEqualComparingArraysStructurally(left, right),
              FailureMessages("wasEqualTo", left, right),
              FailureMessages("wasNotEqualTo", left, right)
            )
        }
      }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should (not fullyMatch regex ("Hel*o") and not include ("orld"))
   *                    ^
   * </pre>
   */
  def fullyMatch(resultOfRegexWordApplication: ResultOfRegexWordApplication): Matcher[String] = {
    val rightRegexString = resultOfRegexWordApplication.regex.toString
    new Matcher[String] {
      def apply(left: String): MatchResult =
        MatchResult(
          !java.util.regex.Pattern.matches(rightRegexString, left),
          FailureMessages("fullyMatchedRegex", left, UnquotedString(rightRegexString)),
          FailureMessages("didNotFullyMatchRegex", left, UnquotedString(rightRegexString))
        )
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should (not include regex ("Hel.o") and not include regex ("""(-)?(\d+)(\.\d*)?"""))
   *                    ^
   * </pre>
   */
  def include(resultOfRegexWordApplication: ResultOfRegexWordApplication): Matcher[String] = {
    val rightRegex = resultOfRegexWordApplication.regex
    new Matcher[String] {
      def apply(left: String): MatchResult =
        MatchResult(
          !rightRegex.findFirstIn(left).isDefined,
          FailureMessages("includedRegex", left, rightRegex),
          FailureMessages("didNotIncludeRegex", left, rightRegex)
        )
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should (not include ("cat") and not include ("1.7"))
   *                    ^
   * </pre>
   */
  def include(expectedSubstring: String): Matcher[String] = {
    new Matcher[String] {
      def apply(left: String): MatchResult =
        MatchResult(
          !(left.indexOf(expectedSubstring) >= 0), 
          FailureMessages("includedSubstring", left, expectedSubstring),
          FailureMessages("didNotIncludeSubstring", left, expectedSubstring)
        )
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should (not startWith regex ("hel*o") and not endWith regex ("wor.d"))
   *                    ^
   * </pre>
   */
  def startWith(resultOfRegexWordApplication: ResultOfRegexWordApplication): Matcher[String] = {
    val rightRegex = resultOfRegexWordApplication.regex
    new Matcher[String] {
      def apply(left: String): MatchResult =
        MatchResult(
          !rightRegex.pattern.matcher(left).lookingAt,
          FailureMessages("startedWithRegex", left, rightRegex),
          FailureMessages("didNotStartWithRegex", left, rightRegex)
        )
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should ((not startWith ("red")) and (not startWith ("1.7")))
   *                     ^
   * </pre>
   */
  def startWith(expectedSubstring: String): Matcher[String] = {
    new Matcher[String] {
      def apply(left: String): MatchResult =
        MatchResult(
          left.indexOf(expectedSubstring) != 0,
          FailureMessages("startedWith", left, expectedSubstring),
          FailureMessages("didNotStartWith", left, expectedSubstring)
        )
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should (not endWith regex ("wor.d") and not startWith regex ("Hel*o"))
   *                    ^
   * </pre>
   */
  def endWith(resultOfRegexWordApplication: ResultOfRegexWordApplication): Matcher[String] = {
    val rightRegex = resultOfRegexWordApplication.regex
    new Matcher[String] {
      def apply(left: String): MatchResult = {
        val allMatches = rightRegex.findAllIn(left)
        MatchResult(
          !(allMatches.hasNext && (allMatches.end == left.length)),
          FailureMessages("endedWithRegex", left, rightRegex),
          FailureMessages("didNotEndWithRegex", left, rightRegex)
        )
      }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should (not endWith ("blue") and not endWith ("1.7"))
   *                    ^
   * </pre>
   */
  def endWith(expectedSubstring: String): Matcher[String] = {
    new Matcher[String] {
      def apply(left: String): MatchResult = {
        MatchResult(
          !(left endsWith expectedSubstring),
          FailureMessages("endedWith", left, expectedSubstring),
          FailureMessages("didNotEndWith", left, expectedSubstring)
        )
      }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * Array(1, 2) should (not contain (5) and not contain (3))
   *                         ^
   * </pre>
   */
  def contain[T](expectedElement: T): Matcher[GenTraversable[T]] = {
    new Matcher[GenTraversable[T]] {
      def apply(left: GenTraversable[T]): MatchResult = {
        MatchResult(
          !(left.exists(_ == expectedElement)),
          FailureMessages("containedExpectedElement", left, expectedElement),
          FailureMessages("didNotContainExpectedElement", left, expectedElement)
        )
      }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * Map("one" -> 1, "two" -> 2) should (not contain key ("three"))
   *                                         ^
   * </pre>
   */
  def contain[K](resultOfKeyWordApplication: ResultOfKeyWordApplication[K]): Matcher[scala.collection.GenMap[K, Any]] = {
    val expectedKey = resultOfKeyWordApplication.expectedKey
    new Matcher[scala.collection.GenMap[K, Any]] {
      def apply(left: scala.collection.GenMap[K, Any]): MatchResult = {
        MatchResult(
          !(left.exists(_._1 == expectedKey)),
          FailureMessages("containedKey", left, expectedKey),
          FailureMessages("didNotContainKey", left, expectedKey)
        )
      }
    }
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * Map("one" -> 1, "two" -> 2) should (not contain value (3))
   *                                         ^
   * </pre>
   */
  def contain[K, V](resultOfValueWordApplication: ResultOfValueWordApplication[V]): Matcher[scala.collection.GenMap[K, V] forSome { type K }] = {
    val expectedValue = resultOfValueWordApplication.expectedValue
    new Matcher[scala.collection.GenMap[K, V] forSome { type K }] {
      def apply(left: scala.collection.GenMap[K, V] forSome { type K }): MatchResult = {
        MatchResult(
          !(left.exists(_._2 == expectedValue)),
          FailureMessages("containedValue", left, expectedValue),
          FailureMessages("didNotContainValue", left, expectedValue)
        )
      }
    }
  }
  
  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * collection should (not contain theSameIteratedElementsAs List(8, 1, 2) and contain theSameElementsAs List(3, 2, 1))
   *                        ^
   * </pre>
   */
  def contain[E](right: ContainMatcher[E]): Matcher[GenTraversable[E]] = 
    new Matcher[GenTraversable[E]] {
      def apply(left: GenTraversable[E]): MatchResult = {
        val result = right(left)
        MatchResult(!result.matches, result.negatedFailureMessage, result.failureMessage)
      }
    }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * result should (not contain a (passedMarks) and contain a (validMarks)))
   *                    ^
   * </pre>
   */
  def contain[T](resultOfAWordApplication: ResultOfAWordToAMatcherApplication[T]): Matcher[GenTraversable[T]] = {
    new Matcher[GenTraversable[T]] {
      def apply(left: GenTraversable[T]): MatchResult = {
        val aMatcher = resultOfAWordApplication.aMatcher
        val matched = left.find(aMatcher(_).matches)
        MatchResult(
          !matched.isDefined, 
          FailureMessages("containedA", left, UnquotedString(aMatcher.nounName), UnquotedString(if (matched.isDefined) aMatcher(matched.get).negatedFailureMessage else "-")), 
          FailureMessages("didNotContainA", left, UnquotedString(aMatcher.nounName))
        )
      }
    }
  }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * result should (not contain an (passedMarks) and contain an (validMarks)))
   *                    ^
   * </pre>
   */
  def contain[T](resultOfAnWordApplication: ResultOfAnWordToAnMatcherApplication[T]): Matcher[GenTraversable[T]] = {
    new Matcher[GenTraversable[T]] {
      def apply(left: GenTraversable[T]): MatchResult = {
        val anMatcher = resultOfAnWordApplication.anMatcher
        val matched = left.find(anMatcher(_).matches)
        MatchResult(
          !matched.isDefined, 
          FailureMessages("containedAn", left, UnquotedString(anMatcher.nounName), UnquotedString(if (matched.isDefined) anMatcher(matched.get).negatedFailureMessage else "-")), 
          FailureMessages("didNotContainAn", left, UnquotedString(anMatcher.nounName))
        )
      }
    }
  }
}

