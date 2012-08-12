(ns guess-film.core
    (:require
        [clj-http.client :as client]
        [net.cgrand.enlive-html :as enlive]
    )
)


(binding [clj-http.core/*cookie-store* (clj-http.cookies/cookie-store)])
(defn fetch-url
    "Fetch a http response (body, headers, cookies etc) using clj-http"
    [url]
    (client/get url {
        ;:debug true,
        :headers {
            "User-Agent" "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1",
            "Accept-Language" "ru-ru,ru;q=0.8,en-us;q=0.5,en;q=0.3"
        },
        :as :auto       ; decode content to UTF-8 automatically
    })
)

(defn parse-html
    "Convert a html to Enlive format"
    [html]
    (enlive/html-resource (java.io.StringReader. html))
)

(defn load-popular-films
    "Loads list of popular films"
    (
        []
        (println "Loading popular films")
        ; Loop that uses another branch of multimenthod is not so idiomatic I guess.
        ; But why creating another method or defining anon method will be better?
        (loop [pages_left 5 page "" films nil]
            (if (<= pages_left 0)
                films
                (do
                    (def result (load-popular-films page))
                    (recur (dec pages_left) (result :next_page) (concat films (result :films)))
                )
            )
        )
    )
    ([page]
        (def base_url "http://www.kinopoisk.ru/popular/")
        (def url (str base_url, page))
        (println (str "  parsing url ", url))
        (def parsed_html
            (parse-html (get (fetch-url url) :body))
        )
        ; get a list of vectors [film_name film_id]
        (def films (map
            #(vector
                (peek (re-find #"^(.*) \(\d+\)" (first (%1 :content)))) ; html/text here?
                (re-find #"\d+" ((%1 :attrs) :href))
            )
            (enlive/select parsed_html [:div.stat :div :> :a])
        ))
        ;(println (first films))
        ; get a link to next page
        (def next_page
            (((first
                (filter
                    #(if (= (first (%1 :content)) "»") true false)
                    (enlive/select parsed_html [:div.navigator :ul.list :li.arr :a])
                )
            ) :attrs) :href)
        )
        (def next_page (clojure.string/replace next_page "/popular/" ""))
        ; return map with results
        {:films films, :next_page next_page}
    )
)


