(ns reagent-contextmenu.menu
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.dom :as dom]
            [goog.events :as events])
  (:import [goog.events EventType]))

;;; Make sure to create the context-menu element somewhere in the dom.
;;; Recommended: at the start of the document.

(def context-id "reagent-contextmenu")

(def context-menu-content (atom [["Action" #(prn "hello")]]))
;; init the context menu with some default action

(defn get-menu []
  (dom/getElement context-id))

(defn show-context [x y]
  (-> (js/jQuery (get-menu))
      (.css (clj->js
             {:display "block"
              :left (- x 10)
              :top (- y 10)}))
      (.show)))

(defn hide-context []
  (-> (js/jQuery (get-menu))
      (.hide)))

(defn context-menu []
  [:ul.dropdown-menu {:id context-id :role "menu"}
   (when-let [content @context-menu-content]
     (for [item content]
       (if (coll? item)
         (let [[name func] item]
           ^{:key name}
           [:li [:a {:on-click #(do (hide-context) (func %))
                     :style {:cursor "pointer"}} name]])
         ^{:key (str item)}[:li.divider])))])



;; remove the context menu if we click out of it or press `esc' (like the normal context menu)

(defonce click-out-or-esc ; <--- defonce so we can reload the code
  [(events/listen js/window EventType.CLICK hide-context)
   (events/listen js/window EventType.KEYUP
                  #(when (= (.-keyCode %) 27) ;; `esc' key
                     (hide-context)))])



;;;;; Main function below

;; Use with a :on-context-menu to activate on right-click

(defn context
  "Update the context menu with a collection of [name function] pairs.
   When passed a keyword instead of [name function], a divider is inserted.

  [[my-fn #(+ 1 2)]
   :divider
   [my-other-fn #(prn (str 1 2 3))]]"
  [evt name-fn-coll]
  (reset! context-menu-content name-fn-coll)
  (show-context (.-pageX evt) (.-pageY evt))
  (.preventDefault evt))
