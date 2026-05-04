// page structure and layout generated with chatgpt chat.openai.com
// Used Claude to help implement feature, pass route data, display saved routes and calculate distance
import { MapContainer, TileLayer, FeatureGroup, GeoJSON } from "react-leaflet"
import { EditControl } from "react-leaflet-draw"
import "leaflet/dist/leaflet.css"
import "leaflet-draw/dist/leaflet.draw.css"

// Calculates the distance in km between two GPS coordinate points using the Haversine formula
// Accounts for the curvature of the Earth — coord format is [longitude, latitude]
const haversineDistance = (coord1, coord2) => {
    const R = 6371 // Earth radius in km
    const dLat = (coord2[1] - coord1[1]) * Math.PI / 180
    const dLon = (coord2[0] - coord1[0]) * Math.PI / 180
    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(coord1[1] * Math.PI / 180) *
        Math.cos(coord2[1] * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
}

// Calculates the total distance of a drawn route by summing Haversine distances between each pair of points
// Returns the total distance in km rounded to 2 decimal places
const calculateRouteDistance = (geoJSON) => {
    try {
        const coords = geoJSON.geometry.coordinates
        if (!coords || coords.length < 2) return 0
        let total = 0
        for (let i = 0; i < coords.length - 1; i++) {
            total += haversineDistance(coords[i], coords[i + 1])
        }
        return Math.round(total * 100) / 100
    } catch (e) {
        console.error("Could not calculate distance:", e)
        return 0
    }
}

// RouteMap works in two modes depending on which props are passed:
// - Drawing mode: pass onRouteDrawn to allow the user to draw a route on the map
// - View mode: pass viewRouteData to display a saved route as a blue line on the map
function RouteMap({ onRouteDrawn, viewRouteData }) {

    // Called by Leaflet Draw when the user finishes drawing a polyline
    // Converts the drawn shape to GeoJSON, calculates its distance and passes both back to the parent
    const onCreated = (e) => {
        const layer = e.layer
        const geoJSON = layer.toGeoJSON()
        const distance = calculateRouteDistance(geoJSON)
        if (onRouteDrawn) {
            onRouteDrawn(JSON.stringify(geoJSON), distance)
        }
    }

    // Called when the user deletes their drawn route using the delete tool
    // Passes null and 0 back to the parent to clear the stored route data and distance
    const onDeleted = () => {
        if (onRouteDrawn) {
            onRouteDrawn(null, 0)
        }
    }

    // Parses the GeoJSON string back into an object so Leaflet can render it on the map
    // Uses try/catch to handle corrupted route data without crashing
    let parsedRoute = null
    if (viewRouteData) {
        try {
            parsedRoute = JSON.parse(viewRouteData)
        } catch (e) {
            console.error("Could not parse route data:", e)
        }
    }

    return (
        // key prop forces a full remount when viewRouteData changes to prevent Leaflet display bugs
        <MapContainer
            key={viewRouteData || "default"}
            center={[53.8008, -1.5491]}
            zoom={13}
            style={{ height: "300px", width: "100%" }}
        >
            {/*Loads OpenStreetMap tile imagery as the map background*/}
            <TileLayer
                attribution="&copy; OpenStreetMap contributors"
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            {/*Renders a saved route as a blue polyline — only shown in view mode*/}
            {parsedRoute && (
                <GeoJSON
                    data={parsedRoute}
                    style={{ color: "#3B82F6", weight: 4, opacity: 0.8 }}
                />
            )}

            {/*Adds drawing toolbar to the map — only shown in drawing mode*/}
            {/*All shape types disabled except polyline which is the only relevant shape for routes*/}
            {onRouteDrawn && (
                <FeatureGroup>
                    <EditControl
                        position="topright"
                        onCreated={onCreated}
                        onDeleted={onDeleted}
                        draw={{
                            rectangle: false,
                            circle: false,
                            marker: false,
                            circlemarker: false,
                            polygon: false
                        }}
                    />
                </FeatureGroup>
            )}
        </MapContainer>
    )
}

export default RouteMap