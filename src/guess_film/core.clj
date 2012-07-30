(ns guess-film.core
    (:require
        [clj-http.client :as client]
        [net.cgrand.enlive-html :as enlive]
    )
)


(binding [clj-http.core/*cookie-store* (clj-http.cookies/cookie-store)])
(defn fetch-url
    "Fetch an url content using clj-http"
    [url]
    (client/get url {
        ;:debug true,
        :headers {
            "User-Agent" "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1",
            "Accept-Language" "ru-ru,ru;q=0.8,en-us;q=0.5,en;q=0.3"
        }
    })
)

(defn parse-html
    "Convert a html to Enlive format"
    [html]
    (enlive/html-resource (java.io.StringReader. html))
)

(defn load-popular-films
    "Loads list of popular films"
    ([] (load-popular-films ""))
    ([page] 
        (def base_url "http://www.kinopoisk.ru/popular/")
        (enlive/select
            (parse-html (get (fetch-url (str base_url, page)) :body))
            [:div.stat :div :a])
        ;(str body)
        ;(get (parse-string body) 3) ; body
    )
)

