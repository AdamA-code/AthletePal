// Used Claude to help implement feature and pass route data
import { MapContainer, TileLayer, FeatureGroup } from "react-leaflet"
import { EditControl } from "react-leaflet-draw"
import "leaflet/dist/leaflet.css"
import "leaflet-draw/dist/leaflet.draw.css"

// onRouteDrawn is a callback so CardioPage can receive the drawn route data
function RouteMap({ onRouteDrawn }) {

    const onCreated = (e) => {
        const layer = e.layer
        const geoJSON = layer.toGeoJSON()
        // Pass the route data up to CardioPage as a JSON string
        if (onRouteDrawn) {
            onRouteDrawn(JSON.stringify(geoJSON))
        }
    }

    const onDeleted = () => {
        // If user deletes the drawn route, clear it
        if (onRouteDrawn) {
            onRouteDrawn(null)
        }
    }

    return (
        <MapContainer
            center={[53.8008, -1.5491]}
            zoom={13}
            style={{ height: "300px", width: "100%" }}
        >
            <TileLayer
                attribution="&copy; OpenStreetMap contributors"
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
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
        </MapContainer>
    )
}

export default RouteMap