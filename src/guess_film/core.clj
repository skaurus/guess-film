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
    ([] (load-popular-films "" 5))
    ([page pages_left]
        (def base_url "http://www.kinopoisk.ru/popular/")
        (def parsed_html
            (parse-html (get (fetch-url (str base_url, page)) :body))
        )
        ; get a list of vectors [film_name film_id]
        (def film_list (map
            #(vector
                (peek (re-find #"^(.*) \(\d\d\d\d\)" (first (%1 :content))))
                (re-find #"\d+" ((%1 :attrs) :href))
            )
            (enlive/select parsed_html [:div.stat :div :> :a])
        ))
        (def next_page
            (((first
                (filter
                    #(if (= (first (%1 :content)) "»") true false)
                    (enlive/select parsed_html [:div.navigator :ul.list :li.arr :a])
                )
            ) :attrs) :href)
        )
        next_page
        ;(str body)
        ;(get (parse-string body) 3) ; body
    )
)

