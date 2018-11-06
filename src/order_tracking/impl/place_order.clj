(ns order-tracking.impl.place-order)

(defn place-order [check-product-exists
                   check-address-exists
                   get-product-price
                   calculate-shipping-cost
                   create-order-acknowledgment-letter
                   send-order-acknowledgment
                   unvalidated-order]
  (let [validated-order (->> (validate-order check-product-exists check-address-exists unvalidated-order)
                             (map-error :place-order-error/validation))
        priced-order (->> (price-order get-product-price validated-order)
                          (map-error :place-order-error/pricing))
        priced-order-with-shipping (->> priced-order
                                        (add-shipping-info-to-order calculate-shipping-cost)
                                        free-vip-shipping)
        acknowledgement-option (acknowledge-order create-order-acknowledgment-letter
                                                  send-order-acknowledgment
                                                  priced-order-with-shipping)]
    (create-events priced-order acknowledgement-option)))
