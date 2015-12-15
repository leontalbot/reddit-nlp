(ns reddit-nlp.stanford-nlp-wrapper
  (:import (java.util Properties)
           (edu.stanford.nlp.pipeline StanfordCoreNLP Annotation)
           (edu.stanford.nlp.util CoreMap)
           (edu.stanford.nlp.trees Tree)
           (edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation)
           (edu.stanford.nlp.ling CoreAnnotations$TokensAnnotation)
           (edu.stanford.nlp.ling CoreAnnotations$TextAnnotation)
           (edu.stanford.nlp.sentiment SentimentCoreAnnotations$SentimentAnnotatedTree)
           (edu.stanford.nlp.neural.rnn RNNCoreAnnotations)))

(def annotators "tokenize, ssplit, parse, sentiment")

(def stanford-nlp-pipeline (StanfordCoreNLP.
                            (doto (Properties.)
                              (.setProperty "annotators" annotators))))

(defn- annotate
  [^String text]
  (let [document (Annotation. text)]
    (.annotate stanford-nlp-pipeline document)
    document))

(defn- sentiment-description
  [score]
  ((keyword (str score)) {:0 "Very negative"
                          :1 "Negative"
                          :2 "Neutral"
                          :3 "Positive"
                          :4 "Very negative"}))

(defn- annotate-tokens
  [^CoreMap sentence]
  (for [token (.get sentence CoreAnnotations$TokensAnnotation)]
    {:word (.get token CoreAnnotations$TextAnnotation)}))

(defn analyze
  [^String text]
  (for [sentence (.get (annotate text) CoreAnnotations$SentencesAnnotation)]    
    (let [tree (.get sentence SentimentCoreAnnotations$SentimentAnnotatedTree)]
      {:text (.toString sentence)
       :words (annotate-tokens sentence)
       :sentiment (sentiment-description (Integer. (RNNCoreAnnotations/getPredictedClass tree)))})))
