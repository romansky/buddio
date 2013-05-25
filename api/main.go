package main

import (
	"encoding/json"
	"fmt"
	"labix.org/v2/mgo"
	// "labix.org/v2/mgo/bson"
	"log"
	"net/http"
)

// Generic JSON object
type GenericResponse map[string]interface{}

func GenericError(err error) interface{} {
	return GenericResponse{"error": err}
}

func GenericOK() interface{} {
	return GenericResponse{"ok": true}
}

// Http handler wrapper
type JsonHandlerFunc func(*mgo.Database, http.ResponseWriter, *http.Request) interface{}

func registerGetHandler(db *mgo.Database, path string, fn JsonHandlerFunc) {
	http.HandleFunc(path, func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		log.Printf("Processing GET request for: %s\n", path)

		res := fn(db, w, r)
		j, err := json.Marshal(res)
		if err != nil {
			fmt.Fprintf(w, "{ error: \"%s\"}", err)
			return
		}
		fmt.Fprint(w, string(j))
	})
}

func registerGetPutHandler(db *mgo.Database, path string, fn JsonHandlerFunc, fn_set JsonHandlerFunc) {
	http.HandleFunc(path, func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		log.Printf("Processing %s request for: %s\n", r.Method, path)

		var res interface{} = nil
		if r.Method == "GET" {
			res = fn(db, w, r)
		} else {
			res = fn_set(db, w, r)
		}
		j, err := json.Marshal(res)
		if err != nil {
			fmt.Fprintf(w, "{ error: \"%s\"}", err)
			return
		}
		fmt.Fprint(w, string(j))

	})
}

// HTTP Handlers
// '/'
func home(db *mgo.Database, w http.ResponseWriter, req *http.Request) interface{} {
	return GenericResponse{"current_version": "1"}
}

func main() {
	session, err := mgo.Dial("localhost")
	if err != nil {
		panic(err)
	}
	defer session.Close()

	session.SetMode(mgo.Monotonic, true)

	db := session.DB("buddio")

	registerGetHandler(db, "/", home)
	registerGetHandler(db, "/v1", v1_home)
	registerGetPutHandler(db, "/v1/settings", v1_settings, v1_settings_set)
	registerGetHandler(db, "/v1/next", v1_next)

	port := 9000

	log.Printf("Listening on port %d\n", port)
	err = http.ListenAndServe(fmt.Sprintf(":%d", port), nil)
	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}
