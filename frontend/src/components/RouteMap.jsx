// page structure and layout generated with chatgpt chat.openai.com
// Used Claude to help implement feature, pass route data and display saved routes
import { MapContainer, TileLayer, FeatureGroup, GeoJSON } from "react-leaflet"
import { EditControl } from "react-leaflet-draw"
import "leaflet/dist/leaflet.css"
import "leaflet-draw/dist/leaflet.draw.css"

function RouteMap({ onRouteDrawn, viewRouteData }) {

    const onCreated = (e) => {
        const layer = e.layer
        const geoJSON = layer.toGeoJSON()
        if (onRouteDrawn) {
            onRouteDrawn(JSON.stringify(geoJSON))
        }
    }

    const onDeleted = () => {
        if (onRouteDrawn) {
            onRouteDrawn(null)
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