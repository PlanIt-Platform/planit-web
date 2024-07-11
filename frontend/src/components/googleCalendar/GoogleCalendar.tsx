import React from "react";
import { gapi } from "gapi-script";

const GOOGLE_CLIENT_ID = "265979614126-7o580sbdist0mh2ig8e7vaf4tuqt08hi.apps.googleusercontent.com"
const API_KEY = "AIzaSyAtqkocmk09tuLeKdXr1bjGfA6HQC9wDcI"
const DISCOVERY_DOCS = ["https://www.googleapis.com/discovery/v1/apis/calendar/v3/rest"]
const SCOPES = "openid email profile https://www.googleapis.com/auth/calendar"

export function GoogleCalendar({ mode, onClose, input, onEventsRetrieved}: {
    mode: string,
    onClose: () => void,
    input?: any,
    onEventsRetrieved?: (events: any[]) => void
}) {

    function addEvent() {
        const req = (input) => {
            const event = {
                'summary': input.title,
                'location': input.location,
                'description': input.description,
                'start': {
                    'dateTime': new Date(input.date).toISOString(),
                    'timeZone': Intl.DateTimeFormat().resolvedOptions().timeZone
                },
                'end': {
                    'dateTime': input.endDate ? new Date(input.endDate).toISOString() : new Date(input.date).toISOString(),
                    'timeZone': Intl.DateTimeFormat().resolvedOptions().timeZone
                },
            }

            let request = gapi.client.calendar.events.insert({
                'calendarId': 'primary',
                'resource': event
            });
            request.execute()
        }

        const events = Array.isArray(input) ? input : [input];
        events.forEach(event => req(event))
    }

    function getEvents() {
        gapi.client.calendar.events.list({
            'calendarId': 'primary',
            'timeMin': (new Date()).toISOString(),
            'showDeleted': false,
            'singleEvents': true,
            'orderBy': 'startTime'
        }).then((response) => {
            const events = response.result.items.map(item => ({
                title: item.summary,
                date: item.start.date,
                id: item.id,
            }));
            if (onEventsRetrieved) onEventsRetrieved(events)
        })
    }

    function googleAuth() {
        try {
            gapi.load("client:auth2", () => {
                gapi.client.init({
                    apiKey: API_KEY,
                    clientId: GOOGLE_CLIENT_ID,
                    discoveryDocs: DISCOVERY_DOCS,
                    scope: SCOPES
                });

                gapi.client.load("calendar", "v3")
                if (!gapi.auth2.getAuthInstance().isSignedIn.get()) {
                    gapi.auth2.getAuthInstance().signIn()
                        .then(() => {
                            if (mode === "addEvent") {
                                addEvent()
                            } else if (mode === "getEvents") {
                                getEvents()
                            }
                        })
                } else {
                    if (mode === "addEvent") {
                        addEvent()
                    } else if (mode === "getEvents") {
                        getEvents()
                    }
                }
            })
        }
        catch (e) {
            console.log(e)
        }
    }

    return (
        <>
            <div className={"overlay"} onClick={onClose}></div>
            <div className="google-popup">
                {mode == "addEvent" ?
                    <p>Do you wish to add the event(s) to your Google Calendar?</p>
                : mode == "getEvents" ?
                    <p>Do you wish to get the events from your Google Calendar?</p>
                : <> </>}
                <button onClick={() => {
                    googleAuth()
                    onClose()
                }}>Yes</button>
                <button onClick={() => {
                    onClose()
                }}>No</button>
            </div>
        </>
    )
}