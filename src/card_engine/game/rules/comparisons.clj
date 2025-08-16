;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.comparisons
  "This namespace defines that functions that will be used to select and apply
  the correct comparison function based on the given operator in a rule.")

(defmulti comparison
  "Applies the given operator to the given values and returns the result.
  Returns true if no dispatcher is found.
  
  Args:
  * op: The operator to apply
  * args: The values to apply the operator to
  
  Dispatchers:
  * := - Checks if all values are equal
  * :!= - Checks if all values are not equal
  * :< - If two values are given, checks if the first value is less than the second value.
         Otherwise, check if all values are less than their preceding value
  * :<= - If two values are given, checks if the first value is less than or equal to the second value.
          Otherwise, check if all values are less than or equal to their preceding value
  * :> - If two values are given, checks if the first value is greater than the second value.
         Otherwise, check if all values are greater than their preceding value
  * :>= - If two values are given, checks if the first value is greater than or equal to the second value.
          Otherwise, check if all values are greater than or equal to their preceding value"
  (fn [op & _] op))

(defmethod comparison :=
  [_ & args]
  (apply = args))

(defmethod comparison :!=
  [_ & args]
  (apply not= args))

(defmethod comparison :<
  [_ & args]
  (apply < args))

(defmethod comparison :<=
  [_ & args]
  (apply <= args))

(defmethod comparison :>
  [_ & args]
  (apply > args))

(defmethod comparison :>=
  [_ & args]
  (apply >= args))

(defmethod comparison :default
  [_ _] true)
