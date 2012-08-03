(ns guess-film.core
    (:require
        [clj-http.client :as client]
        [net.cgrand.enlive-html :as enlive]
    )
)


(defn decode-text
    "Decode text in a given encoding to UTF-8"
    [text, encoding]
    (apply str (map char (.getBytes text encoding)))
)

(binding [clj-http.core/*cookie-store* (clj-http.cookies/cookie-store)])
(defn fetch-url
    "Fetch a http response (body, headers, cookies etc) using clj-http"
    [url]
    (def response (client/get url {
        ;:debug true,
        :headers {
            "User-Agent" "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1",
            "Accept-Language" "ru-ru,ru;q=0.8,en-us;q=0.5,en;q=0.3"
        }
    }))
    (def content-type (get (get response :headers) "content-type"))
    (when (string? content-type)
        (def content-type (clojure.string/split content-type #";\s*"))
        (when (= 2 (count content-type))
            (def media (clojure.string/split (peek content-type) #"="))
            (when (= 2 (count media))
                (def encoding (peek media))
                (def response (assoc response
                    :body (decode-text (get response :body) encoding)
                ))
            )
        )
    )
    response
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
            [:div.stat :div :> :a])
        ;(str body)
        ;(get (parse-string body) 3) ; body
    )
)

