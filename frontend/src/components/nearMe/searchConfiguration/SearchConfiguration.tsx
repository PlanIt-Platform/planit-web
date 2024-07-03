import React from "react";

export function SearchConfiguration(
    {
        userLocation,
        selectedRadius,
        setSelectedRadius,
        setNumEvents,
        numEvents,
        handleSearch
    }) {

    return ( <div className={"near-left"}>
        <h2>Select Radius</h2>
        <div>
            <input type="radio" id="radius25" name="radius" value="25"
                   checked={selectedRadius === 25000}
                   onChange={() => setSelectedRadius(25000)} />
            <label htmlFor="radius25">25 KM</label>
        </div>
        <div>
            <input type="radio" id="radius50" name="radius" value="50"
                   checked={selectedRadius === 50000}
                   onChange={() => setSelectedRadius(50000)} />
            <label htmlFor="radius50">50 KM</label>
        </div>
        <div>
            <input type="radio" id="radius100" name="radius" value="100"
                   checked={selectedRadius === 100000}
                   onChange={() => setSelectedRadius(100000)} />
            <label htmlFor="radius100">100 KM</label>
        </div>

        <h2>Select Number of Events</h2>
        <div>
            <input type="range" id="numEvents" name="numEvents" min="1" max="100"
                   value={numEvents}
                   onChange={(e) => setNumEvents(Number(e.target.value))} />
            <label htmlFor="numEvents">{numEvents}</label>
        </div>
        <button type="button" onClick={() => handleSearch(userLocation)} className="">Search</button>
    </div>)
}