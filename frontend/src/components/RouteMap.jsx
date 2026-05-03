// page structure and layout generated with chatgpt chat.openai.com
// Used Claude to help implement feature, pass route data, display saved routes and calculate distance
import { MapContainer, TileLayer, FeatureGroup, GeoJSON } from "react-leaflet"
import { EditControl } from "react-leaflet-draw"
import "leaflet/dist/leaflet.css"
import "leaflet-draw/dist/leaflet.draw.css"

// Haversine formula — calculates distance in km between two lat/lng points
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

const calculateRouteDistance = (geoJSON) => {
    try {
        const coords = geoJSON.geometry.coordinates
        if (!coords || coords.length < 2) return 0
        let total = 0
        for (let i = 0; i < coords.length - 1; i++) {
            total += haversineDistance(coords[i], coords[i + 1])
        }
        return Math.round(total * 100) / 100 // round to 2 decimal places
    } catch (e) {
        console.error("Could not calculate distance:", e)
        return 0
    }
}

function RouteMap({ onRouteDrawn, viewRouteData }) {

    const onCreated = (e) => {
        const layer = e.layer
        const geoJSON = layer.toGeoJSON()
        const distance = calculateRouteDistance(geoJSON)
        if (onRouteDrawn) {
            onRouteDrawn(JSON.stringify(geoJSON), distance)
        }
    }

    const onDeleted = () => {
        if (onRouteDrawn) {
            onRouteDrawn(null, 0)
        }
    }

    let parsedRoute = null
    if (viewRouteData) {
        try {
            parsedRoute = JSON.parse(viewRouteData)
        } catch (e) {
            console.error("Could not parse route data:", e)
        }
    }

    return (
        <MapContainer
            key={viewRouteData || "default"}
            center={[53.8008, -1.5491]}
            zoom={13}
            style={{ height: "300px", width: "100%" }}
        >
            <TileLayer
                attribution="&copy; OpenStreetMap contributors"
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            {parsedRoute && (
                <GeoJSON
                    data={parsedRoute}
                    style={{ color: "#3B82F6", weight: 4, opacity: 0.8 }}
                />
            )}

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