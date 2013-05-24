package main

import (
	"encoding/json"
	"fmt"
	// "html"
	// "io"
	// "labix.org/v2/mgo"
	// "labix.org/v2/mgo/bson"
	"log"
	"net/http"
)

// Generic JSON object
type GenericResponse map[string]interface{}

func (r GenericResponse) String() (s string) {
	b, err := json.Marshal(r)
	if err != nil {
		s = ""
		return
	}
	s = string(b)
	return
}

func registerHandler(path string, fn http.HandlerFunc) {
	http.HandleFunc(path, func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		log.Printf("Processing request for: %s\n", path)
		fn(w, r)
	})
}

// "/""
func home(w http.ResponseWriter, req *http.Request) {
	fmt.Fprint(w, GenericResponse{"current_version": "1"})
}

func v1_home(w http.ResponseWriter, req *http.Request) {
	fmt.Fprint(w, GenericResponse{"version": "1"})
}

func main() {
	registerHandler("/", home)
	registerHandler("/v1", v1_home)

	port := 9000

	log.Printf("Listening on port %d\n", port)
	err := http.ListenAndServe(fmt.Sprintf(":%d", port), nil)
	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}
