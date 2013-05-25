package main

import (
	"encoding/json"
	"errors"
	"io"
	"labix.org/v2/mgo"
	"net/http"
)

const (
	PAYLOAD_MAX_LENGTH int64 = 1024 * 1024 // 1MB
)

func readBody(req *http.Request) ([]byte, error) {
	if req.ContentLength > PAYLOAD_MAX_LENGTH {
		return nil, errors.New("StatusRequestEntityTooLarge")
	}

	body := make([]byte, req.ContentLength)

	length, err := io.ReadFull(req.Body, body)
	if err != nil || int64(length) != req.ContentLength {
		return nil, errors.New("StatusInternalServerError")
	}

	return body, nil
}

// '/v1'
func v1_home(db *mgo.Database, w http.ResponseWriter, req *http.Request) interface{} {
	return GenericResponse{"version": "1"}
}

// '/v1/settings'
func v1_settings(db *mgo.Database, w http.ResponseWriter, req *http.Request) interface{} {
	return GetSettings(db)
}

func v1_settings_set(db *mgo.Database, w http.ResponseWriter, req *http.Request) interface{} {
	body, err := readBody(req)
	if err != nil {
		return GenericError(err)
	}

	s := &Settings{}
	err = json.Unmarshal(body, &s)
	if err != nil {
		return GenericError(err)
	}

	err = SetSettings(db, s)
	if err != nil {
		return GenericError(err)
	}

	// TODO: Recalculate next here
	calculatePlaylist(db, s)

	return GenericOK()
}

// '/v1/next'
func v1_next(db *mgo.Database, w http.ResponseWriter, req *http.Request) interface{} {
	var p *Playlist = nil
	var err error = nil

	//calculatePlaylist(db, GetSettings(db))
	p, err = GetNext(db)

	if err != nil {
		return GenericError(err)
	}

	return p
}
