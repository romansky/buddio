package main

import (
	// "fmt"
	// "log"
	// "net/http"
	// "encoding/json"
	"labix.org/v2/mgo"
	"labix.org/v2/mgo/bson"
	"math/rand"
	"time"
)

type MusicSettings struct {
	On             bool     `json:"on"`
	SelectedGeners []string `json:"geners,omitempty"`
}

type PodcastsSettings struct {
	On     bool `json:"on"`
	Short  bool `json:"p_short"`
	Medium bool `json:"p_medium"`
	Long   bool `json:"p_long"`
}

type NewsRepeat int

const (
	Every30 = 30
	Every60 = 60
	Every90 = 90
)

type NewsSettings struct {
	On     bool       `json:"on"`
	Repeat NewsRepeat `json:"repeat"`
}

type Settings struct {
	Music    MusicSettings    `json:"music"`
	Podcasts PodcastsSettings `json:"podcasts"`
	News     NewsSettings     `json:"news"`
}

func GetSettings(db *mgo.Database) *Settings {
	// return &Settings{
	// 	Music: MusicSettings{
	// 		On:             true,
	// 		SelectedGeners: []string{"Rock", "Classic"}},
	// 	Podcasts: PodcastsSettings{},
	// }
	s := &Settings{}
	err := db.C("users").Find(bson.M{}).One(s)

	if err != nil {
		panic(err)
	}

	return s
}

func SetSettings(db *mgo.Database, s *Settings) error {
	return db.C("users").Update(bson.M{}, s)
}

const (
	Music   = 1
	Podcast = 2
	News    = 4
)

type Track struct {
	ID     bson.ObjectId `json:"id" bson:"_id"`
	Title  string        `json:"title"`
	Length int           `json:"length"`
	Url    string        `json:"url"`
	Type   int           `json:"type"`
}

type Playlist struct {
	Tracks []Track `json:"tracks"`
}

func calculatePlaylist(db *mgo.Database, s *Settings) {
	ps := db.C("playlist")
	ps.RemoveAll(bson.M{})

	avil := db.C("avilable_tracks")

	i := 0
	n := 5

	all := map[int][]Track{}

	loadType := func(t int) {
		q := avil.Find(bson.M{"type": t})

		c, _ := q.Count()

		all[t] = make([]Track, c)
		tmp := make([]Track, c)
		q.All(&tmp)
		copy(all[t], tmp)
	}
	if s.Music.On {
		loadType(Music)
	}
	if s.Podcasts.On {
		loadType(Podcast)
	}
	if s.News.On {
		loadType(News)
	}

	used := map[int]bool{}
	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	addTrack := func(t int) {
		var idx int = -1
		var isUsed bool = true
		iter := 0
		max := len(all[t])
		//fmt.Printf("here - %d\n", max)
		for max > 0 && iter < 10 && (idx < 0 || isUsed) {
			idx = r.Intn(max)
			_, isUsed = used[idx]
			iter += 1
		}

		if !isUsed {
			used[idx] = true
			ps.Insert(all[t][idx])
			i += 1
		}
	}

	iter := 0 // Use iter to
	for i < n && iter < 20 {
		iter += 1
		if s.Music.On && i < n {
			addTrack(Music)
		}
		if s.Podcasts.On && i < n {
			addTrack(Podcast)
		}
		if s.News.On && i < n {
			addTrack(News)
		}
	}
}

func GetNext(db *mgo.Database) (*Playlist, error) {
	// return &Playlist{
	//  Tracks: []Track{
	//    Track{"ID", "Title", "HTTPSOURCE"},
	//  },
	// }

	n := 5

	p := &Playlist{
		Tracks: make([]Track, n),
	}

	err := db.C("playlist").Find(bson.M{}).Limit(n).All(&p.Tracks)

	if err != nil {
		return nil, err
	}

	return p, nil
}
