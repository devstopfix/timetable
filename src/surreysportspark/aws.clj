(ns surreysportspark.aws
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [surreysportspark.pool :as ssp]))

(deflambdafn example.lambda.RunLambdaFn
             [_in _out _ctx]
             (ssp/make-calendar))
