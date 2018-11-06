(ns order-tracking.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [order-tracking.dto.order-form :as order-form-dto]
            [order-tracking.dto.place-order-error :as place-order-error-dto]
            [order-tracking.dto.place-order-event :as place-order-event-dto]
            [order-tracking.impl.place-order :refer [place-order]]))

(s/defschema OrderFormDto
  {})

(declare check-product-exists)

(declare check-address-exists)

(declare get-pricing-function)

(declare calculate-shipping-cost)

(declare create-order-acknowledgment-letter)

(declare send-order-acknowledgment)

(defn workflow-result-to-http-reponse [{:keys [err events]}]
  (if err
    {:status 401 :headers {} :body (place-order-error-dto/from-domain err)}
    {:status 200 :headers {} :body (map place-order-event-dto/from-domain events)}))

(defn place-order-api [order-form]
  (let [unvalidated-order (order-form-dto/to-unvalidated-order order-form)
        workflow (partial place-order
                          check-product-exists
                          check-address-exists
                          get-pricing-function
                          calculate-shipping-cost
                          create-order-acknowledgment-letter
                          send-order-acknowledgment)
        result (workflow unvalidated-order)]
    (workflow-result-to-http-reponse result)))

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "Order-tracking"}}}}

   (context "/v1" []
            (POST "/place-order" []
                  :body [order-form OrderFormDto]
                  (place-order-api order-form)))))
