package main

import (
	// "fmt"
	"log"
	// "net/http"
	"encoding/json"
	"labix.org/v2/mgo"
	"testing"
)

func doTest(fn func(*mgo.Database)) {
	session, err := mgo.Dial("localhost")
	if err != nil {
		panic(err)
	}
	defer session.Close()

	session.SetMode(mgo.Monotonic, true)

	db := session.DB("buddio")

	fn(db)
}

func printJson(obj interface{}) {
	j, err := json.Marshal(obj)
	if err != nil {
		log.Printf("Error: %s\n", err)
		return
	}
	log.Println(string(j))
}

func TestGetSettings(*testing.T) {
	doTest(func(db *mgo.Database) {
		printJson(GetSettings(db))
	})
}

func TestGetPlaylist(*testing.T) {
	doTest(func(db *mgo.Database) {
		p, _ := GetNext(db)
		printJson(p)
	})
}

func TestCalculatePlaylist(*testing.T) {
	doTest(func(db *mgo.Database) {
		calculatePlaylist(db, GetSettings(db))
	})
}

// func TestAvilableTrack(*testing.T) {
// 	doTest(func(db *mgo.Database) {
// 		t := CreateAvilableTrack()

// 		t.Title = "TEST"
// 		err := db.C("avilable_tracks").Insert(t)
// 		if err != nil {
// 			panic(err)
// 		}
// 	})
// }
