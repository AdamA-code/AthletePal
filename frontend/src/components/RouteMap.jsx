import { MapContainer, TileLayer, FeatureGroup } from "react-leaflet"
import { EditControl } from "react-leaflet-draw"
import "leaflet/dist/leaflet.css"
import "leaflet-draw/dist/leaflet.draw.css"

function RouteMap() {

    const onCreated = (e) => {
        const layer = e.layer
        console.log("Route drawn:", layer)
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