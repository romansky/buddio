package main

import (
	// "encoding/json"
	// "fmt"
	"github.com/SlyMarbo/rss"
	// "io/ioutil"
	// "net/http"
	"labix.org/v2/mgo"
	"labix.org/v2/mgo/bson"
	"strconv"
	"strings"
)

// type GTrack struct {
// 	ID     string `json:"id"`
// 	Title  string `json:"title"`
// 	Artist string `json:"artist"`
// }

// type GTracksData struct {
// 	Items []GTrack `json: "items"`
// }

// type GTracks struct {
// 	Data GTracksData `json: "data"`
// }

func parseLength(str string) int {
	splt := strings.Split(str, ":")

	h := 0
	m := 0
	n := 0
	var err error = nil

	if len(splt) == 3 {
		h, err = strconv.Atoi(splt[0])
		if err != nil {
			return 0
		}
		n += 1
	}

	if len(splt) >= 2 {
		m, err = strconv.Atoi(splt[n])
		if err != nil {
			return 0
		}
		n += 1
	}

	s, err := strconv.Atoi(splt[n])
	if err != nil {
		return 0
	}

	return h*60*60 + m*60 + s
}

func main() {
	// Connect to DB
	session, err := mgo.Dial("localhost")
	if err != nil {
		panic(err)
	}
	defer session.Close()

	session.SetMode(mgo.Monotonic, true)

	db := session.DB("buddio")

	avil := db.C("avilable_tracks")

	// Get RSS Feed
	//feed, err := rss.Fetch("http://feeds.5by5.tv/changelog")
	//feed, err := rss.Fetch("http://rss.cnn.com/services/podcasting/newscast/rss.xml")
	feed, err := rss.Fetch("http://feeds.kexp.org/kexp/songoftheday")
	if err != nil {
		panic(err)
	}

	for _, item := range feed.Items {
		err := avil.Insert(&Track{
			ID:     bson.NewObjectId(),
			Title:  item.Title,
			Length: parseLength(item.Length),
			Url:    item.Link,
			Type:   Music,
		})
		if err != nil {
			panic(err)
		}
		// fmt.Printf("%s - %d\n", item.Length, parseLength(item.Length))
	}

	// // Get google music tracks list
	// client := &http.Client{}

	// res, err := client.Do(req)
	// if err != nil {
	// 	panic(err)
	// }

	// defer res.Body.Close()

	// body, err := ioutil.ReadAll(res.Body)
	// if err != nil {
	// 	panic(err)
	// }

	// ts := &GTracks{}
	// err = json.Unmarshal(body, &ts)
	// if err != nil {
	// 	panic(err)
	// }

	// // Insert to DB
	// for _, gtrack := range ts.Data.Items {
	// 	t := &Track{
	// 		ID: gtrack.ID,
	// 		Title: gtrack.Artist + " - " + gtrack.Title,
	// 		Source: Source{
	// 			Url:
	// 		}
	// 	}
	// }
}

